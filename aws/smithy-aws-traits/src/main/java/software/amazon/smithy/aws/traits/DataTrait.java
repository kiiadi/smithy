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

package software.amazon.smithy.aws.traits;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.traits.StringTrait;

public final class DataTrait extends StringTrait {
    public static final String NAME = "aws.api#data";

    public DataTrait(String value, SourceLocation sourceLocation) {
        super(NAME, value, sourceLocation);
    }

    public static final class Provider extends StringTrait.Provider<DataTrait> {
        public Provider() {
            super(NAME, DataTrait::new);
        }
    }
}
