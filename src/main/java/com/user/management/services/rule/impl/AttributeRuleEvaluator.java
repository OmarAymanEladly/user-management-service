package com.user.management.services.rule.impl;

import com.user.management.model.entity.UserTypeRule;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.services.rule.UserTypeRuleEvaluator;
import org.springframework.stereotype.Component;

@Component
public class AttributeRuleEvaluator implements UserTypeRuleEvaluator {
    private final String EVALUATOR_TYPE = "ATTRIBUTE";

    @Override
    public String getEvaluatorType() {
        return EVALUATOR_TYPE;
    }

    @Override
    public boolean evaluate(UserTypeRule rule, UserProvisioningEvent event) {
        String actualValue = event.attributes().get(rule.getMatchKey());

        if (actualValue == null) {
            return false;
        }

        return rule.getMatchValue().equalsIgnoreCase(actualValue);
    }
}
