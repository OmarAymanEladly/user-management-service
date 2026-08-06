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

import java.util.Arrays;
import java.util.List;

@Component
public class RdnRuleEvaluator implements UserTypeRuleEvaluator {

    @Override
    public RuleEvaluatorType getEvaluatorType() {
        return RuleEvaluatorType.RDN;
    }

    @Override
    public boolean evaluate(UserTypeRule rule, UserProvisioningEvent event) {
        List<String> dns = event.attributes().get("dn");

        if (dns == null || dns.isEmpty()) {
            return false;
        }

        String dn = dns.get(0);
        String rdnAttr = rule.getMatchKey();     // e.g. "ou", "uid", "cn"
        String expected = rule.getMatchValue();  // e.g. "Contractors"

        return Arrays.stream(dn.split(","))
                .map(String::trim)
                .filter(part -> part.toLowerCase().startsWith(rdnAttr.toLowerCase() + "="))
                .map(part -> part.substring(rdnAttr.length() + 1))
                .anyMatch(value -> value.equalsIgnoreCase(expected));
    }
}
