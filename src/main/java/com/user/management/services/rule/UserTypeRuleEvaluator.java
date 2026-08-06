package com.user.management.services.rule;

import com.user.management.model.entity.UserTypeRule;

import com.user.management.model.enumeration.RuleEvaluatorType;
import com.user.management.model.event.UserProvisioningEvent;

public interface UserTypeRuleEvaluator {
    RuleEvaluatorType getEvaluatorType();
    boolean evaluate(UserTypeRule rule, UserProvisioningEvent event);
}
