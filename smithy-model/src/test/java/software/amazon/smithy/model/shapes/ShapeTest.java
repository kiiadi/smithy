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

package software.amazon.smithy.model.shapes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.traits.DocumentationTrait;
import software.amazon.smithy.model.traits.Trait;

public class ShapeTest {

    private static final class SubShape extends Shape {
        private SubShape(final Builder builder) {
            super(builder, ShapeType.STRUCTURE, false);
        }

        public static Builder builder() {
            return new Builder();
        }

        public <R> R accept(final ShapeVisitor<R> cases) {
            throw new UnsupportedOperationException();
        }

        public static final class Builder extends AbstractShapeBuilder<Builder, SubShape> {
            @Override
            public SubShape build() {
                return new SubShape(this);
            }
        }
    }

    private static class MyTrait implements Trait {
        private String name;
        private SourceLocation sourceLocation;

        MyTrait(String name, SourceLocation sourceLocation) {
            this.name = name;
            this.sourceLocation = sourceLocation != null ? sourceLocation : SourceLocation.none();
        }

        public String getName() {
            return name;
        }

        public SourceLocation getSourceLocation() {
            return sourceLocation;
        }

        @Override
        public Node toNode() {
            return Node.objectNode();
        }
    }

    private static class OtherTrait extends MyTrait {
        OtherTrait(String name, SourceLocation sourceLocation) {
            super(name, sourceLocation);
        }
    }

    private static class AnotherTrait extends OtherTrait {
        AnotherTrait(String name, SourceLocation sourceLocation) {
            super(name, sourceLocation);
        }
    }

    @Test
    public void requiresShapeId() {
        Assertions.assertThrows(IllegalStateException.class, () -> SubShape.builder().build());
    }

    @Test
    public void convertsShapeToBuilder() {
        Shape shape1 = StringShape.builder().id("ns.foo#baz").build();
        Shape shape2 = Shape.shapeToBuilder(shape1).build();
        StringShape shape3 = Shape.shapeToBuilder(StringShape.builder().id("ns.foo#baz").build()).build();

        assertThat(shape1, equalTo(shape2));
        assertThat(shape1, equalTo(shape3));
    }

    @Test
    public void castsToString() {
        Shape shape = SubShape.builder().id("ns.foo#baz").build();

        assertEquals("(structure: `ns.foo#baz`)", shape.toString());
    }

    @Test
    public void hasSource() {
        Shape shape = SubShape.builder().id("ns.foo#baz").source("foo", 1, 2).build();

        assertEquals("foo", shape.getSourceLocation().getFilename());
        assertEquals(1, shape.getSourceLocation().getLine());
        assertEquals(2, shape.getSourceLocation().getColumn());
    }

    @Test
    public void sourceCannotBeNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SubShape.builder().source(null));
    }

    @Test
    public void hasTraits() {
        MyTrait trait = new MyTrait("foo.baz#foo", null);
        MyTrait otherTrait = new OtherTrait("other", null);
        ShapeId id = ShapeId.from("ns.foo#baz");
        DocumentationTrait documentationTrait = new DocumentationTrait("docs", SourceLocation.NONE);
        Shape shape = SubShape.builder()
                .id(id)
                .addTrait(trait)
                .addTrait(otherTrait)
                .addTrait(documentationTrait)
                .build();

        assertTrue(shape.getTrait(MyTrait.class).isPresent());
        assertTrue(shape.findTrait("foo.baz#foo").isPresent());
        assertTrue(shape.hasTrait("foo.baz#foo"));
        assertTrue(shape.getTrait(OtherTrait.class).isPresent());
        assertFalse(shape.getTrait(AnotherTrait.class).isPresent());
        assertFalse(shape.findTrait("not-there").isPresent());
        assertFalse(shape.hasTrait("not-there"));

        assertTrue(shape.getTrait(DocumentationTrait.class).isPresent());
        assertTrue(shape.hasTrait(DocumentationTrait.class));
        assertTrue(shape.findTrait("documentation").isPresent());
        assertTrue(shape.findTrait("smithy.api#documentation").isPresent());
        assertTrue(shape.findTrait("documentation").get() instanceof DocumentationTrait);

        Collection<Trait> traits = shape.getAllTraits().values();
        assertThat(traits, hasSize(3));
        assertThat(traits, hasItem(trait));
        assertThat(traits, hasItem(otherTrait));
        assertThat(traits, hasItem(documentationTrait));
    }

    @Test
    public void traitsMustNotBeNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SubShape.builder().id("ns.foo#baz").addTrait(null);
        });
    }

    @Test
    public void removesTraits() {
        MyTrait trait = new MyTrait("foo", null);
        MyTrait otherTrait = new OtherTrait("other", null);
        Shape shape = SubShape.builder()
                .id("ns.foo#baz")
                .addTrait(trait)
                .addTrait(otherTrait)
                .removeTrait("other")
                .build();

        assertThat(shape.getAllTraits(), hasKey("foo"));
        assertThat(shape.getAllTraits(), not(hasKey("other")));
    }

    @Test
    public void differentShapeTypesAreNotEqual() {
        Shape shapeA = StringShape.builder().id("ns.foo#baz").build();
        Shape shapeB = TimestampShape.builder().id("ns.foo#baz").build();

        assertNotEquals(shapeA, shapeB);
    }

    @Test
    public void differentTypesAreNotEqual() {
        Shape shapeA = StringShape.builder().id("ns.foo#baz").build();

        assertNotEquals(shapeA, "");
    }

    @Test
    public void differentTraitNamesNotEqual() {
        MyTrait traitA = new MyTrait("foo", null);
        MyTrait traitB = new MyTrait("other", null);
        Shape shapeA = SubShape.builder().id("ns.foo#baz").addTrait(traitA).build();
        Shape shapeB = SubShape.builder().id("ns.foo#baz").addTrait(traitB).build();

        assertNotEquals(shapeA, shapeB);
    }

    @Test
    public void differentTraitsNotEqual() {
        MyTrait traitA = new MyTrait("foo", null);
        MyTrait traitB = new OtherTrait("foo", null);
        Shape shapeA = SubShape.builder().id("ns.foo#baz").addTrait(traitA).build();
        Shape shapeB = SubShape.builder().id("ns.foo#baz").addTrait(traitB).build();

        assertNotEquals(shapeA, shapeB);
    }

    @Test
    public void differentIdsAreNotEqual() {
        Shape shapeA = SubShape.builder().id("ns.foo#baz").build();
        Shape shapeB = SubShape.builder().id("ns.foo#bar").build();

        assertNotEquals(shapeA, shapeB);
    }

    @Test
    public void sameInstanceIsEqual() {
        Shape shapeA = SubShape.builder().id("ns.foo#baz").build();

        assertEquals(shapeA, shapeA);
    }

    @Test
    public void sameValueIsEqual() {
        Shape shapeA = SubShape.builder().id("ns.foo#baz").build();
        Shape shapeB = SubShape.builder().id("ns.foo#baz").build();

        assertEquals(shapeA, shapeB);
    }

    @Test
    public void samesTraitsIsEqual() {
        MyTrait traitA = new MyTrait("foo", null);
        Shape shapeA = SubShape.builder().id("ns.foo#baz").addTrait(traitA).build();
        Shape shapeB = SubShape.builder().id("ns.foo#baz").addTrait(traitA).build();

        assertEquals(shapeA, shapeB);
    }
}
