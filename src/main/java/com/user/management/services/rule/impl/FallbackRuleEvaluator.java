package com.user.management.services.rule.impl;

import com.user.management.model.entity.UserTypeRule;
import com.user.management.model.enumeration.RuleEvaluatorType;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.services.rule.UserTypeRuleEvaluator;
import org.springframework.stereotype.Component;

@Component
public class FallbackRuleEvaluator implements UserTypeRuleEvaluator {

    @Override
    public RuleEvaluatorType getEvaluatorType() {
        return RuleEvaluatorType.FALLBACK;
    }

    @Override
    public boolean evaluate(UserTypeRule rule, UserProvisioningEvent event) {
        return true;
    }
}
