package com.user.management.services.rule.impl;

import com.user.management.model.entity.UserTypeRule;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.services.rule.UserTypeRuleEvaluator;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RdnRuleEvaluator implements UserTypeRuleEvaluator {
    private final String EVALUATOR_TYPE = "RDN";

    @Override
    public String getEvaluatorType() {
        return EVALUATOR_TYPE;
    }

    @Override
    public boolean evaluate(UserTypeRule rule, UserProvisioningEvent event) {
        String dn = event.attributes().get("dn");
        if (dn == null || dn.isBlank()) return false;

        String rdnAttr = rule.getMatchKey();     // e.g. "ou", "uid", "cn"
        String expected = rule.getMatchValue();  // e.g. "Contractors"

        return Arrays.stream(dn.split(","))
                .map(String::trim)
                .filter(part -> part.toLowerCase().startsWith(rdnAttr.toLowerCase() + "="))
                .map(part -> part.substring(rdnAttr.length() + 1))
                .anyMatch(value -> value.equalsIgnoreCase(expected));
    }
}
