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

package software.amazon.smithy.model.traits;

import java.util.Optional;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.utils.MapUtils;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Marks a shape as deprecated.
 */
public final class DeprecatedTrait extends AbstractTrait implements ToSmithyBuilder<DeprecatedTrait> {
    public static final String NAME = "smithy.api#deprecated";

    private final String since;
    private final String message;

    private DeprecatedTrait(DeprecatedTrait.Builder builder) {
        super(NAME, builder.sourceLocation);
        this.since = builder.since;
        this.message = builder.message;
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(NAME);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            DeprecatedTrait.Builder builder = builder().sourceLocation(value);
            ObjectNode objectNode = value.expectObjectNode();
            String sinceValue = objectNode.getMember("since")
                    .map(v -> v.expectStringNode().getValue()).orElse(null);
            String messageValue = objectNode.getMember("message")
                    .map(v -> v.expectStringNode().getValue()).orElse(null);
            builder.since(sinceValue).message(messageValue);
            return builder.build();
        }
    }

    /**
     * Gets the deprecated since value.
     *
     * @return returns the optional deprecated since value.
     */
    public Optional<String> getSince() {
        return Optional.ofNullable(since);
    }

    /**
     * Gets the deprecation message value.
     *
     * @return returns the optional deprecation message value.
     */
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    @Override
    protected Node createNode() {
        return new ObjectNode(MapUtils.of(), getSourceLocation())
                       .withOptionalMember("since", getSince().map(Node::from))
                       .withOptionalMember("message", getMessage().map(Node::from));
    }

    @Override
    public DeprecatedTrait.Builder toBuilder() {
        return builder().since(since).message(message).sourceLocation(getSourceLocation());
    }

    /**
     * @return Returns a builder used to create a deprecated trait.
     */
    public static DeprecatedTrait.Builder builder() {
        return new Builder();
    }

    /**
     * Builder used to create a DeprecatedTrait.
     */
    public static final class Builder extends AbstractTraitBuilder<DeprecatedTrait, Builder> {
        private String since;
        private String message;

        private Builder() {}

        public Builder since(String since) {
            this.since = since;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        @Override
        public DeprecatedTrait build() {
            return new DeprecatedTrait(this);
        }
    }
}
