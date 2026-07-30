package com.user.management.services.rule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserTypeRuleEvaluatorRegistry {
    private final Map<String, UserTypeRuleEvaluator> evaluators;

    public UserTypeRuleEvaluatorRegistry(List<UserTypeRuleEvaluator> evaluators) {
        this.evaluators = new HashMap<>();

        for (UserTypeRuleEvaluator evaluator : evaluators) {
            UserTypeRuleEvaluator previous =
                    this.evaluators.putIfAbsent(
                            evaluator.getEvaluatorType(),
                            evaluator);

            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate RuleEvaluator registered for type: "
                                + evaluator.getEvaluatorType());
            }
        }

        log.info("RuleEvaluatorRegistry initialized with evaluators: {}",
                this.evaluators.keySet());
    }

    public Optional<UserTypeRuleEvaluator> find(String evaluatorType) {
        return Optional.ofNullable(evaluators.get(evaluatorType));
    }
}
