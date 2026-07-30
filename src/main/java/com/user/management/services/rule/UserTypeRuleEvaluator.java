package com.user.management.services.rule;

import com.user.management.model.entity.UserTypeRule;
import com.user.management.model.event.UserProvisioningEvent;

public interface UserTypeRuleEvaluator {
    String getEvaluatorType();
    boolean evaluate(UserTypeRule rule, UserProvisioningEvent event);
}
