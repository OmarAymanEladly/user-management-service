package com.user.management.audit.aspect;

import com.user.management.audit.annotation.AuditActor;
import com.user.management.audit.annotation.AuditResource;
import com.user.management.audit.annotation.PublishAuditEvent;
import com.user.management.audit.dto.Actor;
import com.user.management.audit.dto.AuditEvent;
import com.user.management.audit.dto.AuditEventData;
import com.user.management.audit.dto.Resource;
import com.user.management.audit.enumeration.ResourceType;
import com.user.management.audit.publisher.AuditEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class AuditEventAspect {

    private final AuditEventPublisher publisher;
    private final SpelExpressionParser    spelParser     = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Value("${audit.source-service:user-management-service}")
    private String sourceService;

    public AuditEventAspect(AuditEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Around("@annotation(annotation)")
    public Object intercept(ProceedingJoinPoint pjp, PublishAuditEvent annotation) throws Throwable {
        log.info("Audit aspect triggered for method [{}]", pjp.getSignature().getName());

        String outcome = "SUCCESS";
        String reason  = null;
        Object result  = null;

        try {
            result = pjp.proceed();
            return result;
        }
        catch (Throwable ex) {
            outcome = "FAILURE";
            reason  = ex.getMessage();
            throw ex;
        }
        finally {
            if (!"FAILURE".equals(outcome) || annotation.publishOnFailure()) {
                tryPublish(pjp, annotation, result, outcome, reason);
            }
        }
    }

    private void tryPublish(ProceedingJoinPoint pjp, PublishAuditEvent annotation,
                            Object result, String outcome, String reason) {
        try {
            EvaluationContext ctx         = buildSpelContext(pjp, result);
            Class<?>          targetClass = pjp.getTarget().getClass();

            String              actionType   = resolveActionType(annotation, ctx);
            Resource resource = resolveResource(annotation, targetClass, pjp, ctx);
            Map<String, Object> metadata     = resolveMetadata(annotation, ctx);
            Actor               actor        = resolveActor(annotation, targetClass);
            String              corrId       = Optional.ofNullable(MDC.get("correlationId"))
                    .orElseGet(() -> UUID.randomUUID().toString());

            AuditEvent event = AuditEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("AUDIT")
                    .occurredAt(Instant.now())
                    .sourceService(sourceService)
                    .data(AuditEventData.builder()
                            .actionType(actionType)
                            .actor(actor)
                            .resource(resource)
                            .outcome(outcome)
                            .reason(reason)
                            .correlationId(corrId)
                            .metadata(metadata)
                            .build())
                    .build();

            publisher.publish(event);

        }
        catch (Exception e) {
            log.error("Failed to publish audit event for action [{}]: {}",
                    annotation.actionType(), e.getMessage(), e);
        }
    }


    private String resolveActionType(PublishAuditEvent annotation, EvaluationContext ctx) {
        if (!annotation.actionTypeSpEL().isBlank()) {
            return evalSpel(annotation.actionTypeSpEL(), ctx, String.class);
        }
        return annotation.actionType().name();
    }

    private Resource resolveResource(PublishAuditEvent annotation, Class<?> targetClass,
                                     ProceedingJoinPoint pjp, EvaluationContext context) {

        // 1. @PublishAuditEvent on method
        if (annotation.resourceType() != ResourceType.NONE) {
            String resourceId = evalSpel(annotation.resourceIdSpEL(), context, String.class);
            return new Resource(resourceId,annotation.resourceType().name());
        }

        // 2. @AuditResource on class
        AuditResource auditResource =
                targetClass.getAnnotation(AuditResource.class);

        if (auditResource != null) {
            String resourceId = evalSpel(auditResource.idSpEL(), context, String.class);
            return new Resource(auditResource.type().name(), resourceId);
        }

        // Missing configuration: neither @PublishAuditEvent nor @AuditResource provided resource info
        throw new IllegalStateException(
                "No resource configuration found for method [" +
                        targetClass.getName() + "." +
                        pjp.getSignature().getName() +
                        "]. Configure resourceType/resourceIdSpEL on " +
                        "@PublishAuditEvent or @AuditResource on the class."
        );
    }

    private Actor resolveActor(PublishAuditEvent annotation, Class<?> targetClass) {

        // 1. @PublishAuditEvent on method
        if (!annotation.actorId().isBlank()) {
            return new Actor(annotation.actorId(), annotation.actorUsername());
        }

        // 2. @AuditResource on class
        AuditActor classActor = targetClass.getAnnotation(AuditActor.class);
        if (classActor != null) {
            return new Actor(classActor.id(), classActor.name());
        }

        // 3. JWT from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return new Actor(jwt.getSubject(), jwt.getClaimAsString("preferred_username"));
        }

        // 4. SYSTEM fallback
        return new Actor("SYSTEM", "system");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveMetadata(PublishAuditEvent annotation,
                                                EvaluationContext ctx) {
        Map<?, ?> raw = evalSpel(annotation.metadataSpEL(), ctx, Map.class);
        if (raw == null) return Map.of();
        Map<String, Object> result = new LinkedHashMap<>();
        raw.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }

    // SpEL helpers

    private EvaluationContext buildSpelContext(ProceedingJoinPoint pjp, Object result) {
        MethodSignature sig    = (MethodSignature) pjp.getSignature();
        Method          method = sig.getMethod();
        Object[]        args   = pjp.getArgs();

        MethodBasedEvaluationContext ctx = new MethodBasedEvaluationContext(
                result, method, args, nameDiscoverer
        );
        ctx.setVariable("result", result);
        return ctx;
    }

    private <T> T evalSpel(String expression, EvaluationContext ctx, Class<T> type) {
        if (expression == null || expression.isBlank()) return null;
        try {
            return spelParser.parseExpression(expression).getValue(ctx, type);
        } catch (Exception e) {
            log.warn("SpEL evaluation failed for [{}]: {}", expression, e.getMessage());
            return null;
        }
    }
}
