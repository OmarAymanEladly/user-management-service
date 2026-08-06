package com.user.management.services.rule.impl;

import com.user.management.model.entity.UserTypeRule;
<<<<<<< HEAD
import com.user.management.model.event.RuleEvaluatorType;
=======
import com.user.management.model.enumeration.RuleEvaluatorType;
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.services.rule.UserTypeRuleEvaluator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttributeRuleEvaluator implements UserTypeRuleEvaluator {

    @Override
    public RuleEvaluatorType getEvaluatorType() {
        return RuleEvaluatorType.ATTRIBUTE;
    }

    @Override
    public boolean evaluate(UserTypeRule rule, UserProvisioningEvent event) {
        List<String> actualValues = event.attributes().get(rule.getMatchKey());

        if (actualValues == null || actualValues.isEmpty()) {
            return false;
        }

        return actualValues.stream().anyMatch(value -> rule.getMatchValue().equalsIgnoreCase(value));
    }
}
