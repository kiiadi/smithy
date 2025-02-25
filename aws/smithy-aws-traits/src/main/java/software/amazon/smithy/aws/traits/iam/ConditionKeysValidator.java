/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.aws.traits.iam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import software.amazon.smithy.aws.traits.ServiceTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.TopDownIndex;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.ValidationUtils;

/**
 * Ensures that condition keys referenced by operations bound within the
 * closure of a service are defined either explicitly using the
 * {@code defineConditionKeys} trait or through an inferred resource
 * identifier condition key.
 *
 * <p>Condition keys that refer to global "aws:*" keys are allowed to not
 * be defined on the service.
 */
public class ConditionKeysValidator extends AbstractValidator {
    @Override
    public List<ValidationEvent> validate(Model model) {
        ConditionKeysIndex conditionIndex = model.getKnowledge(ConditionKeysIndex.class);
        TopDownIndex topDownIndex = model.getKnowledge(TopDownIndex.class);

        return model.getShapeIndex().shapes(ServiceShape.class)
                .filter(service -> service.hasTrait(ServiceTrait.class))
                .flatMap(service -> {
                    List<ValidationEvent> results = new ArrayList<>();
                    Set<String> knownKeys = conditionIndex.getDefinedConditionKeys(service).keySet();

                    for (OperationShape operation : topDownIndex.getContainedOperations(service)) {
                        for (String name : conditionIndex.getConditionKeyNames(service, operation)) {
                            if (!knownKeys.contains(name) && !name.startsWith("aws:")) {
                                results.add(error(operation, String.format(
                                        "This operation scoped within the `%s` service refers to an undefined "
                                        + "condition key `%s`. Expected one of the following defined condition "
                                        + "keys: [%s]",
                                        service.getId(), name, ValidationUtils.tickedList(knownKeys))));
                            }
                        }
                    }

                    return results.stream();
                })
                .collect(Collectors.toList());
    }
}
