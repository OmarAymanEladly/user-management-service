package com.user.management.services.rule;

<<<<<<< HEAD
import com.user.management.model.event.RuleEvaluatorType;
=======
import com.user.management.model.enumeration.RuleEvaluatorType;
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
<<<<<<< HEAD
import java.util.function.Function;
import java.util.stream.Collectors;
=======
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2

@Slf4j
@Component
public class UserTypeRuleEvaluatorRegistry {
    private final Map<RuleEvaluatorType, UserTypeRuleEvaluator> evaluators;

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

    public Optional<UserTypeRuleEvaluator> find(RuleEvaluatorType evaluatorType) {
        return Optional.ofNullable(evaluators.get(evaluatorType));
    }
}
