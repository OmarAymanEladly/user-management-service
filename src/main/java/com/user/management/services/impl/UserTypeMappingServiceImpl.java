package com.user.management.services.impl;

import com.user.management.model.entity.UserType;
import com.user.management.model.entity.UserTypeRule;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.repository.UserTypeRepository;
import com.user.management.repository.UserTypeRuleRepository;
import com.user.management.services.UserTypeMappingService;
import com.user.management.services.rule.UserTypeRuleEvaluator;
import com.user.management.services.rule.UserTypeRuleEvaluatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserTypeMappingServiceImpl implements UserTypeMappingService {

    private final UserTypeRuleRepository ruleRepository;
    private final UserTypeRuleEvaluatorRegistry registry;
    private final UserTypeRepository userTypeRepository;
    private static final String DEFAULT_USER_TYPE = "Provisioned";

    @Override
    public UserType mapUserType(UserProvisioningEvent event) {
        List<UserTypeRule> applicableRules =
                ruleRepository.findApplicableRules(event.source());

        log.debug("Evaluating {} rules for user [{}] from source [{}]",
                applicableRules.size(), event.username(), event.source());

        for (UserTypeRule rule : applicableRules) {
            Optional<UserTypeRuleEvaluator> evaluator = registry.find(rule.getEvaluatorType());

            if (evaluator.isEmpty()) {
                log.warn("No evaluator registered for type '{}', skipping rule [id={}]",
                        rule.getEvaluatorType(), rule.getId());
                continue;
            }

            if (evaluator.get().evaluate(rule, event)) {
                log.info("Rule [id={}, type={}, key={}, value={}] matched for user [{}] → UserType: {}",
                        rule.getId(), rule.getEvaluatorType(),
                        rule.getMatchKey(), rule.getMatchValue(),
                        event.username(), rule.getUserType().getType());
                return rule.getUserType();
            }
        }

        log.warn("No rule matched for user [{}] from source [{}], falling back to '{}'",
                event.username(), event.source(), DEFAULT_USER_TYPE);

        return userTypeRepository.findByType(DEFAULT_USER_TYPE)
                .orElseThrow(() -> new IllegalStateException(
                        "'" + DEFAULT_USER_TYPE + "' UserType not found — check your seed data"));
    }
}