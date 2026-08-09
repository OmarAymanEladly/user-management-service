package com.user.management.audit.annotation;

import com.user.management.audit.enumeration.ActionType;
import com.user.management.audit.enumeration.ResourceType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublishAuditEvent {

    /**
     * Static action type: used when the action is always the same regardless of runtime state.
     * Ignored when actionTypeSpEL is set.
     */
    ActionType actionType() default ActionType.NONE;

    /**
     * Dynamic action type via SpEL: takes precedence over actionType when set.
     * Use a map literal to branch on runtime values:
     *   "{'LDAP': 'USER_LDAP_PROVISION', 'ADMIN': 'USER_ADMIN_PROVISION'}[#event.source().name()]"
     */
    String actionTypeSpEL() default "";

    ResourceType resourceType();

    /**
     * SpEL to extract the resource ID.
     * Available: #result (return value), #<paramName> (each method parameter by name)
     * Example: "#result?.id", "#event.keycloakId()"
     */
    String resourceIdSpEL() default "";

    /**
     * SpEL returning a map literal for the metadata field.
     * Example: "{'userType': #result.userType, 'source': #event.source().name()}"
     */
    String metadataSpEL() default "";

    /**
     * Static actor: use for system/scheduler/consumer triggered actions.
     * Leave blank to fall back to the JWT principal from SecurityContextHolder.
     */
    String actorId()       default "";
    String actorUsername() default "";

    /**
     * Whether to publish a FAILURE event when the method throws.
     */
    boolean publishOnFailure() default true;
}
