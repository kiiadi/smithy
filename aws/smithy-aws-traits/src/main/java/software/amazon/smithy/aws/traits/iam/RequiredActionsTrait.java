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

import java.util.List;
import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.traits.StringListTrait;
import software.amazon.smithy.utils.ToSmithyBuilder;

public final class RequiredActionsTrait extends StringListTrait implements ToSmithyBuilder<RequiredActionsTrait> {
    public static final String NAME = "aws.iam#requiredActions";

    public RequiredActionsTrait(List<String> actions, FromSourceLocation sourceLocation) {
        super(NAME, actions, sourceLocation);
    }

    public RequiredActionsTrait(List<String> actions) {
        this(actions, SourceLocation.NONE);
    }

    public static final class Provider extends StringListTrait.Provider<RequiredActionsTrait> {
        public Provider() {
            super(NAME, RequiredActionsTrait::new);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Builder toBuilder() {
        Builder builder = builder().sourceLocation(getSourceLocation());
        getValues().forEach(builder::addValue);
        return builder;
    }

    public static final class Builder extends StringListTrait.Builder<RequiredActionsTrait, Builder> {
        @Override
        public RequiredActionsTrait build() {
            return new RequiredActionsTrait(getValues(), getSourceLocation());
        }
    }
}
