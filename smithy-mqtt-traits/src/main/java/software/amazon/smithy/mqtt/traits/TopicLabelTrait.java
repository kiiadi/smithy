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

package software.amazon.smithy.mqtt.traits;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.traits.BooleanTrait;

/**
 * Binds a member to an MQTT label using the member name.
 */
public final class TopicLabelTrait extends BooleanTrait {
    public static final String NAME = "smithy.api#mqttTopicLabel";

    public TopicLabelTrait(SourceLocation sourceLocation) {
        super(NAME, sourceLocation);
    }

    public static final class Provider extends BooleanTrait.Provider<TopicLabelTrait> {
        public Provider() {
            super(NAME, TopicLabelTrait::new);
        }
    }
}
