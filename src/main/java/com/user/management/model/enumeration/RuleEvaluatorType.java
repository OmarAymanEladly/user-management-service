package com.user.management.model.enumeration;

public enum RuleEvaluatorType {
    ATTRIBUTE,
    RDN,
    FALLBACK // if no rules match, fallback to default rule
}
