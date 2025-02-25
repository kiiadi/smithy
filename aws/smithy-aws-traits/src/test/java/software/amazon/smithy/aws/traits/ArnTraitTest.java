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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.SourceException;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.model.traits.TraitFactory;

public class ArnTraitTest {

    @Test
    public void loadsTraitWithFromNode() {
        Node node = Node.parse("{\"template\": \"resourceName\"}");
        TraitFactory provider = TraitFactory.createServiceFactory();
        Optional<Trait> trait = provider.createTrait("aws.api#arn", ShapeId.from("ns.foo#foo"), node);

        assertTrue(trait.isPresent());
        assertThat(trait.get(), instanceOf(ArnTrait.class));
        ArnTrait arnTrait = (ArnTrait) trait.get();
        assertThat(arnTrait.getTemplate(), equalTo("resourceName"));
        assertThat(arnTrait.isNoAccount(), is(false));
        assertThat(arnTrait.isNoRegion(), is(false));
        assertThat(arnTrait.getLabels(), empty());
    }

    @Test
    public void canSetRegionAndServiceToNo() {
        Node node = Node.parse("{\"noAccount\": true, \"noRegion\": true, \"absolute\": false, \"template\": \"foo\"}");
        TraitFactory provider = TraitFactory.createServiceFactory();
        Optional<Trait> trait = provider.createTrait("aws.api#arn", ShapeId.from("ns.foo#foo"), node);

        assertTrue(trait.isPresent());
        ArnTrait arnTrait = (ArnTrait) trait.get();
        assertThat(arnTrait.getTemplate(), equalTo("foo"));
        assertThat(arnTrait.isNoAccount(), is(true));
        assertThat(arnTrait.isNoRegion(), is(true));
        assertThat(arnTrait.toNode(), equalTo(node));
        assertThat(arnTrait.toBuilder().build(), equalTo(arnTrait));
    }

    @Test
    public void canSetIncludeTemplateExpressions() {
        Node node = Node.parse("{\"noAccount\": false, \"noRegion\": false, \"template\": \"foo/{Baz}/bar/{Bam}/boo/{Boo}\"}");
        TraitFactory provider = TraitFactory.createServiceFactory();
        Optional<Trait> trait = provider.createTrait("aws.api#arn", ShapeId.from("ns.foo#foo"), node);

        assertTrue(trait.isPresent());
        ArnTrait arnTrait = (ArnTrait) trait.get();
        assertThat(arnTrait.getTemplate(), equalTo("foo/{Baz}/bar/{Bam}/boo/{Boo}"));
        assertThat(arnTrait.getLabels(), contains("Baz", "Bam", "Boo"));
    }

    @Test
    public void resourcePartCannotStartWithSlash() {
        assertThrows(SourceException.class, () -> {
            Node node = Node.parse("{\"template\": \"/resource\"}");
            TraitFactory.createServiceFactory().createTrait("aws.api#arn", ShapeId.from("ns.foo#foo"), node);
        });
    }

    @Test
    public void validatesAccountValue() {
        assertThrows(SourceException.class, () -> {
            Node node = Node.parse("{\"template\": \"foo\", \"noAccount\": \"invalid\"}");
            TraitFactory.createServiceFactory().createTrait("aws.api#arn", ShapeId.from("ns.foo#foo"), node);
        });
    }

    @Test
    public void validatesRegionValue() {
        assertThrows(SourceException.class, () -> {
            Node node = Node.parse("{\"template\": \"foo\", \"noRegion\": \"invalid\"}");
            TraitFactory.createServiceFactory().createTrait("aws.api#arn", ShapeId.from("ns.foo#foo"), node);
        });
    }
}
