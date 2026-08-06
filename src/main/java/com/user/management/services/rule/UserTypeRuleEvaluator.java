package com.user.management.services.rule;

import com.user.management.model.entity.UserTypeRule;
<<<<<<< HEAD
import com.user.management.model.event.RuleEvaluatorType;
=======
import com.user.management.model.enumeration.RuleEvaluatorType;
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import com.user.management.model.event.UserProvisioningEvent;

public interface UserTypeRuleEvaluator {
    RuleEvaluatorType getEvaluatorType();
    boolean evaluate(UserTypeRule rule, UserProvisioningEvent event);
}
