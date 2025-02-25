=========================
Smithy core specification
=========================

*Smithy* is an interface definition language and set of tools that allows
developers to build RPC clients and servers in multiple languages. Smithy
models define a service as a collection of resources, operations, and shapes.

.. contents:: Table of contents
    :depth: 2
    :local:
    :backlinks: none


----------------------------
Status of this specification
----------------------------

This specification is currently at version |version| and is subject to change.


---------------------
Requirements notation
---------------------

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
"SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this
document are to be interpreted as described in [:rfc:`2119`].

This specification makes use of the Augmented Backus-Naur Form (ABNF)
[:rfc:`5234`] notation, including the *core rules* defined in Appendix B
of that document.


-------------
Smithy models
-------------

A Smithy model is made up of shapes, traits, and metadata that define the
services, resources, operations, and data structures used in an API. Smithy
models can be used to drive code generation for client and server frameworks,
documentation generation, and various other forms of static analysis.


Model syntax
============

Smithy models are defined using either the :ref:`Smithy IDL <smithy-language-specification>`
or :ref:`JSON <json-ast>`. The Smithy IDL is the preferred format for
authoring and reading models, while the JSON format is preferred for
tooling and integrations. Unless declared otherwise, specification examples
are written using the IDL syntax. Complementary JSON examples are provided
alongside Smithy IDL examples where appropriate.


.. _smithy-version:

Model version
=============

The Smithy specification is versioned using a `semver <https://semver.org/>`_
``major`` . ``minor`` . ``patch`` version string. The version string does not
support semver extensions. The version of a Smithy model is defined using a
:ref:`version statement <version-statement>` in the Smithy IDL. The following
example sets the version to "|version|":

.. tabs::

    .. code-tab:: smithy

        $version: "0.1.0"

    .. code-tab:: json

        {
            "smithy": "0.1.0"
        }

When no version number is specified in the IDL, an implementation will assume
that the model is compatible. Because this can lead to unexpected parsing
errors, models SHOULD always include a version. The JSON AST model requires that
a version is specified in a top-level "smithy" key-value pair.


Version compatibility
---------------------

Multiple version statements MAY appear in a Smithy model or can be encountered
when merging multiple models together. Multiple versions are supported if and
only if all of the version statements are compatible according to the
following constraints:

1. Each version MUST specify the same major version number. For example,
   ``0.1.0`` and ``1.0.0`` are **not** compatible because they use different
   major version numbers.
2. When dealing with a major version of "0" (for example, ``0.1.0``), versions
   that use the same minor version are considered compatible regardless of the
   patch version. For example, if models are loaded that use a version of
   ``0.1.0``, ``0.1.1``, and ``0.1.2``, then all of the models are considered
   to be compatible. However, ``0.2.0`` and ``0.1.99`` are **not** compatible.
3. When dealing with a major version of "1" or higher, all versions that use
   the same major version number are considered compatible. For example, if
   models are loaded that use a version of ``1.0.0``, ``1.0.1``, and
   ``1.1.0``, then all of the models are considered to be compatible.
   However, ``1.0.0`` and ``2.0.0`` are **not** compatible.


.. _metadata:

Model metadata
==============

:dfn:`Metadata` is a schema-less extensibility mechanism that can be applied
to a model using a :ref:`metadata statement <metadata-statement>`. Metadata
statements start with ``metadata``, followed by the key to set, followed by
``=``, followed by the JSON-like :ref:`node value <node-values>` to assign.

.. tabs::

    .. code-tab:: smithy

        metadata foo = baz
        metadata hello = "bar"
        metadata "lorem" = {
          ipsum: ["dolor"]
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "metadata": {
                "foo": "baz",
                "hello": "bar",
                "lorem": {
                    "ipsum": ["dolor"]
                }
            }
        }

Top-level metadata key-value pair conflicts are resolved by
:ref:`merging metadata <merging-metadata>`


.. _namespaces:

Namespaces
==========

Shapes and traits are defined inside a :dfn:`namespace`. A namespace is
mechanism for logically grouping shapes in a way that makes them reusable
alongside other models without naming conflicts.

A :ref:`namespace statement <namespace-statement>` is used to change the
*current namespace*. A namespace MUST be defined before a shape or trait
definition can be defined. Any number of namespaces can appear in a model.

The following example defines a string shape named ``MyString`` in the
``smithy.example`` namespace and a string shape named ``MyString`` in the
``another.example`` namespace:

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example
        string MyString

        namespace another.example
        string MyString

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyString": {
                        "type": "string"
                    }
                }
            },
            "another.example": {
                "shapes": {
                    "MyString": {
                        "type": "string"
                    }
                }
            }
        }


.. _shapes:

------
Shapes
------

*Shapes* are instances of *types* that describe the structure of an API.
:ref:`Traits <traits>` can be applied to shapes to describe custom facets
of the shape.

Shapes are defined inside of :ref:`namespaces <namespaces>`. Shape definitions
always start with the type name of the shape followed by the name of the
shape.


.. _simple-types:

Simple types
============

:ref:`Simple types <simple-types>` are types that do not contain nested types
or shape references.

.. list-table::
    :header-rows: 1
    :widths: 10 90

    * - Type
      - Description
    * - blob
      - Uninterpreted binary data
    * - boolean
      - Boolean value type
    * - string
      - UTF-8 encoded string
    * - byte
      - 8-bit signed integer ranging from -128 to 127 (inclusive)
    * - short
      - 16-bit signed integer ranging from -32,768 to 32,767 (inclusive)
    * - integer
      - 32-bit signed integer ranging from -2^31 to (2^31)-1 (inclusive)
    * - long
      - 64-bit signed integer ranging from -2^63 to (2^63)-1 (inclusive)
    * - float
      - Single precision IEEE-754 floating point number
    * - double
      - Double precision IEEE-754 floating point number
    * - bigInteger
      - Arbitrarily large signed integer
    * - bigDecimal
      - Arbitrary precision signed decimal number
    * - timestamp
      - Represents an instant in time with no UTC offset or timezone. The
        serialization of a timestamp is determined by a
        :ref:`protocol <protocols-trait>`.
    * - document
      - **Unstable** Represents an untyped JSON-like value that can take on
        one of the following types: null, boolean, string, byte, short,
        integer, long, float, double, an array of these types, or a map of
        these types where the key is string.

The :token:`simple_shape` statement is used to define a simple shape. Simple
shapes are defined by a type, followed by a shape name, followed by a
new line.

.. tip::

    The :ref:`prelude model <prelude>` contains shapes for every simple type.
    These shapes can be referenced using a relative shape ID
    (for example, ``String``) or using an absolute shape ID
    (for example, ``smithy.api#String``).

The following example defines a shape for each simple type in the
``smithy.example`` namespace:

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        blob Blob
        boolean Boolean
        string String
        byte Byte
        short Short
        integer Integer
        long Long
        float Float
        double Double
        bigInteger BigInteger
        bigDecimal BigDecimal
        timestamp Timestamp
        document Document

    .. code-tab:: json

        {
          "smithy": "0.1.0",
          "smithy.example": {
            "shapes": {
              "Blob": {
                "type": "blob",
              },
              "Boolean": {
                "type": "boolean"
              },
              "String": {
                "type": "string"
              },
              "Byte": {
                "type": "byte"
              },
              "Short": {
                "type": "short"
              },
              "Integer": {
                "type": "integer"
              },
              "Long": {
                "type": "long"
              },
              "Float": {
                "type": "float"
              },
              "Double": {
                "type": "double"
              },
              "BigInteger": {
                "type": "bigInteger"
              },
              "BigDecimal": {
                "type": "bigDecimal"
              },
              "Timestamp": {
                "type": "timestamp"
              },
              "Document": {
                "type": "document"
              }
            }
          }
        }


.. _timestamp-serialization-format:

Timestamp serialization format
------------------------------

By default, the serialization format of a timestamp is implicitly determined by
the :ref:`protocol <protocols-trait>` of a service; however, the serialization
format can be explicitly configured to override the default format used by the
protocol by applying the :ref:`timestampFormat-trait` to a timestamp
shape or a member that targets a timestamp.

The following steps are taken to determine the serialization format of a
timestamp:

1. Use the ``timestampFormat`` trait of the :ref:`member <member>` reference if
   present.
2. Use the ``timestampFormat`` trait of the shape if present.
3. Use the format required by the protocol.

The timestamp shape is an abstraction of time; the serialization format of a
timestamp as it is sent over the wire, whether determined by the protocol or by
the ``timestampFormat`` trait, SHOULD NOT have any effect on the types exposed
by tooling to represent a timestamp.


.. _document-type:

Document types
--------------

A document type represents an untyped JSON-like value that can take on one of
the following types: null, boolean, string, byte, short, integer, long, float,
double, an array of these types, or a map of these types where the key is a
string.

Not all protocols support document types, and the serialization format of a
document type is protocol-specific. All JSON protocols SHOULD support document
types and they SHOULD serialize document types inline as normal JSON values.

.. warning::

    Document types are currently considered unstable. They are not generally
    supported by all protocols or tooling, and their design MAY change and
    evolve before a stable release of Smithy.


.. _aggregate-types:

Aggregate types
===============

Aggregate types are types that are composed of other types. Aggregate shapes
reference other shapes using :ref:`members <member>`.

.. list-table::
    :header-rows: 1
    :widths: 10 90

    * - Type
      - Description
    * - :ref:`list`
      - homogeneous collection of values
    * - :ref:`set`
      - Unordered collection of unique homogeneous values
    * - :ref:`map`
      - Map data structure that maps string keys to homogeneous values
    * - :ref:`structure`
      - Fixed set of named heterogeneous members
    * - :ref:`union`
      - Tagged union data structure that can take on one of several
        different, but fixed, types
    * - :ref:`member`
      - Defined in aggregate shapes to reference other shapes


.. _list:

list
----

The :dfn:`list` type represents a homogeneous collection of values. A list is
defined using a :token:`list_statement`. A list statement consists of the
shape named followed by an object with a single key-value pair of "member"
that defines the :ref:`member <member>` of the list.

The following example defines a list with a string member from the
:ref:`prelude <prelude>`:

.. tabs::

    .. code-tab:: smithy

        list MyList {
          member: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyList": {
                        "member": {
                            "target": "String"
                        }
                    }
                }
            }
        }

Traits can be applied to the list shape and its member:

.. tabs::

    .. code-tab:: smithy

        @length(min: 3, max: 10)
        list MyList {
          @length(min: 1, max: 100)
          member: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyList": {
                        "length": {
                            "min": 3,
                            "max": 10
                        },
                        "member": {
                            "length": {
                                "min": 1,
                                "max": 100
                            },
                            "target": "String"
                        }
                    }
                }
            }
        }

Traits can be applied to shapes and members outside of their
definition using an ``apply`` statement:

.. tabs::

    .. code-tab:: smithy

        apply MyList @documentation("Long documentation string...")
        apply MyList$member @documentation("Long documentation string...")

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "traits": {
                    "MyList": {
                        "documentation": "Long documentation string..."
                    },
                    "MyList$member": {
                        "documentation": "Long documentation string..."
                    }
                }
            }
        }


.. _set:

set
---

The :dfn:`set` type represents an unordered collection of unique homogeneous
values. A set is defined using a :token:`set_statement` that consists of the
shape named followed by an object with a single key-value pair of "member"
that defines the :ref:`member <member>` of the set.

The following example defines a set of strings:

.. tabs::

    .. code-tab:: smithy

        set StringSet {
          member: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "StringSet": {
                        "member": {
                            "target": "String"
                        }
                    }
                }
            }
        }

Traits can be applied to the set shape and its members:

.. tabs::

    .. code-tab:: smithy

        @deprecated
        set StringSet {
          @sensitive
          member: String
        }

        // Apply additional traits to the set member.
        apply StringSet$member @documentation("text")

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "StringSet": {
                        "deprecated" true,
                        "member": {
                            "target": "String"
                        }
                    }
                },
                "traits": {
                    "StringSet$member": {
                        "documentation": "text"
                    }
                }
            }
        }

.. note::

    Not all languages support set data structures with non-scalar values.
    Such languages SHOULD represent sets as a custom set data structure that
    can interpret value hash codes and equality. Alternatively, clients MAY
    store the values of a set data structure in a list and rely on the service
    to ensure uniqueness.


.. _map:

map
---

The :dfn:`map` type represents a map data structure that maps string keys to
homogeneous values. A map cannot contain duplicate keys. A map is defined using
a :token:`map_statement`. The ``key`` member of a map MUST get a ``string``
shape.

The following example defines a map of strings to integers:

.. tabs::

    .. code-tab:: smithy

        map IntegerMap {
          key: String,
          value: Integer
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "IntegerMap": {
                        "key": {
                            "target": "String"
                        },
                        "value": {
                            "target": "String"
                        }
                    }
                }
            }
        }


Traits can be applied to the map shape and its members:

.. tabs::

    .. code-tab:: smithy

        @length(min: 0, max: 100)
        map IntegerMap {
          @length(min: 1, max: 10)
          key: String,

          @sensitive
          value: Integer
        }

        // Apply more traits to the key and value members.
        apply IntegerMap$key @documentation("Key documentation")
        apply IntegerMap$value @documentation("Value documentation")

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "IntegerMap": {
                        "length": {
                            "min": 0,
                            "max": 100
                        },
                        "key": {
                            "target": "String",
                            "length": {
                                "min": 1,
                                "max": 10
                            }
                        },
                        "value": {
                            "target": "String",
                            "sensitive": true
                        }
                    }
                },
                "traits": {
                    "IntegerMap$key": {
                        "documentation": "Key documentation"
                    },
                    "IntegerMap$value": {
                        "documentation": "Value documentation"
                    }
                }
            }
        }


.. _structure:

structure
---------

The :dfn:`structure` type represents a fixed set of named heterogeneous members.
A member name maps to exactly one structure :ref:`member <member>` definition.

A structure is defined using a :token:`structure_statement`. A structure
statement is a map of structure :ref:`member` names to the shape targeted by
the member. Any number of inline trait definitions can precede each member.

The following example defines a structure with two members:

.. tabs::

    .. code-tab:: smithy

        structure MyStructure {
          foo: String,
          baz: Integer,
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyStructure": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String"
                            },
                            "baz": {
                                "target": "Integer"
                            }
                        }
                    }
                }
            }
        }

Traits can be applied to members inside of the structure or externally
using the ``apply`` statement:

.. tabs::

    .. code-tab:: smithy

        structure MyStructure {
          @required
          foo: String,

          @deprecated
          baz: Integer,
        }

        apply MyStructure$foo @documentation("Documentation content...")

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyStructure": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "required": true
                            },
                            "baz": {
                                "target": "Integer",
                                "deprecated": true
                            }
                        }
                    }
                },
                "traits": {
                    "MyStructure$foo": {
                        "documentation": "Documentation content..."
                    }
                }
            }
        }


.. _union:

union
-----

The union type represents a `tagged union data structure`_ that can take
on several different, but fixed, types. Only one type can be used at any
one time.

A union is defined using a :token:`union_statement`. Union shapes take the
same form as structure shapes.

The following example defines a union shape with several members:

.. tabs::

    .. code-tab:: smithy

        union MyUnion {
          i32: Integer,
          stringA: String,
          @sensitive stringB: String,
        }

        // Apply additional traits to the member named "i32".
        apply MyUnion$i32 @documentation("text")

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyUnion": {
                        "type": "structure",
                        "members": {
                            "i32": {
                                "target": "Integer"
                            },
                            "stringA": {
                                "target": "String"
                            },
                            "stringB": {
                                "target": "String",
                                "sensitive": true
                            }
                        }
                    }
                },
                "traits": {
                    "MyUnion$i32": {
                        "documentation": "text"
                    }
                }
            }
        }


.. _member:

member
------

:dfn:`Members` are defined in :ref:`aggregate types <aggregate-types>` to
reference other shapes using a :ref:`shape ID <shape-id>`. A member MUST NOT
target an ``operation``, ``resource``, ``service``, or ``member`` shape.

The following example defines a list shape. The member of the list is a
member shape with a shape ID of ``MyList$member``. The member targets
the ``MyString`` shape in the same namespace.

.. tabs::

    .. code-tab:: smithy

        list MyList {
          member: MyString
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyList": {
                        "member": {
                            "target": "MyString"
                        }
                    }
                }
            }
        }

Traits can be attached to members inline before the member definition:

.. tabs::

    .. code-tab:: smithy

        list MyList {
          @sensitive
          member: MyString
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyList": {
                        "member": {
                            "target": "MyString",
                            "sensitive": true
                        }
                    }
                }
            }
        }

Traits can be applied to member definitions using the ``apply`` statement
followed by the targeted shape ID followed by the trait value. Traits are
applied to shapes outside of their definition in the JSON AST using the
"traits" key-value pair of a namespace.

.. tabs::

    .. code-tab:: smithy

        apply MyList$member @documentation("Hello")

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "traits": {
                    "MyList$member": {
                        "documentation": "Hello"
                    }
                }
            }
        }

The shape ID of a member consists of the aggregate shape name followed by
"$" followed by the member name. The member name for each shape is defined
in :ref:`shape-id-member-names`.


.. _default-values:

Default values
``````````````

Shapes are used to represent messages that can be sent on the wire and data
structures that are generated in various programming languages. The values
provided for :ref:`members <member>` of :ref:`aggregate shapes <aggregate-types>`
are either always present and set to a default value when necessary or
*boxed*, meaning a value is optionally present with no default value.

- The default value of a ``byte``, ``short``, ``integer``, ``long``,
  ``float``, and ``double`` shape that is not boxed is zero.
- The default value of a ``boolean`` shape that is not boxed is ``false``.
- All other shapes are always considered boxed and have no default value.

Members are considered boxed if and only if the member is marked with the
:ref:`box-trait` or the shape targeted by the member is marked
with the box trait. Members that target strings, timestamps, and
aggregate shapes are always considered boxed and have no default values.


.. _service-types:

Service types
=============

*Service types* are types that form services, resources, and operations.

.. list-table::
    :header-rows: 1
    :widths: 10 90

    * - Type
      - Description
    * - :ref:`service <service>`
      - Entry point of an API that aggregates resources and operations together
    * - :ref:`operation <operation>`
      - Represents the input, output and possible errors of an API operation
    * - :ref:`resource <resource>`
      - Entity with an identity that has a set of operations


..  _service:

Service
-------

A :dfn:`service` is the entry point of an API that aggregates resources and
operations together. The :ref:`resources <resource>` and
:ref:`operations <operation>` of an API are bound within the closure of a
service.

A service shape is defined using a :token:`service_statement` and supports
the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 15 75

    * - Property
      - Type
      - Description
    * - version
      - ``string``
      - **Required**. Defines the version of the service. The version can be
        provided in any format (e.g., ``2017-02-11``, ``2.0``, etc).
    * - :ref:`operations <service-operations>`
      - [:ref:`shape-id`]
      - Binds a list of operations to the service. Each element in the list is
        a shape ID that MUST target an operation.
    * - :ref:`resources <service-resources>`
      - [:ref:`shape-id`]
      - Binds a list of resources to the service. Each element in the list is
        a shape ID that MUST target a resource.


.. _service-operations:

Service operations
``````````````````

:ref:`Operation <operation>` shapes can be bound to a service by adding the
shape ID of an operation to the ``operations`` property of a service.
Operations bound directly to a service are typically RPC-style operations
that do not fit within a resource hierarchy.

.. tabs::

    .. code-tab:: smithy

        service MyService {
          version: "2017-02-11",
          operations: [GetServerTime],
        }

        @readonly
        operation GetServerTime() -> GetServerTimeOutput

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyService": {
                        "type": "service",
                        "version": "2017-02-11",
                        "operations": ["GetServerTime"]
                    },
                    "GetServerTime": {
                        "type": "operation",
                        "output": "GetServerTimeOutput"
                    }
                }
            }
        }

**Validation**

1. An operation MUST NOT be bound to multiple shapes wihin the closure of a
   service.
2. Every operation shape contained within the entire closure of a service MUST
   have a case-insensitively unique shape name, regardless of its namespaces.


.. _service-resources:

Service resources
`````````````````

:ref:`Resource <resource>` shapes can be bound to a service by adding the
shape ID of a resource to the ``resources`` property of a service.

.. tabs::

    .. code-tab:: smithy

        service MyService {
          version: "2017-02-11",
          resources: [MyResource],
        }

        resource MyResource {}

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyService": {
                        "type": "service",
                        "version": "2017-02-11",
                        "resources": ["MyResource"]
                    },
                    "MyResource": {
                        "type": "resource"
                    }
                }
            }
        }

**Validation**

1. A resource MUST NOT be bound to multiple shapes within the closure of a
   service.
2. Every resource shape contained within the entire closure of a service MUST
   have a case-insensitively unique shape name, regardless of their
   namespaces.


..  _operation:

Operation
---------

The :dfn:`operation` type represents the input, output and possible errors of
an API operation. Operation shapes are bound to :ref:`resource <resource>`
shapes and :ref:`service <service>` shapes. Operation shapes are defined using
the :token:`operation_statement`.

The following example defines an operation shape that accepts an input
structure named ``Input``, returns an output structure named ``Output``, and
can potentially return the ``NotFound`` or ``BadRequest``
:ref:`error structures <error-trait>`.

.. tabs::

    .. code-tab:: smithy

        operation MyOperation(Input) -> Output errors [NotFound, BadRequest]

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyOperation": {
                        "type": "operation",
                        "input": "Input",
                        "output": "Output",
                        "errors": ["NotFound", "BadRequest"]
                    }
                }
            }
        }


.. _operation-input:

Operation input
```````````````

The input of an operation is an optional shape ID that MUST target a
structure shape. An operation is not required to accept input.

The following example defines an operation that accepts an input structure
named ``Input``:

.. tabs::

    .. code-tab:: smithy

        operation MyOperation(Input)

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyOperation": {
                        "type": "operation",
                        "input": "Input"
                    }
                }
            }
        }

The input of an operation can be omitted with empty parenthesis after the
shape name. The following example defines an operation that accepts no
input and returns no output:

.. tabs::

    .. code-tab:: smithy

        operation MyOperation()

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyOperation": {
                        "type": "operation"
                    }
                }
            }
        }


.. _operation-output:

Operation output
````````````````

The output of an operation is an optional shape ID that MUST target a
structure shape. An operation is not required to return output.

The following example defines an operation that returns an output
structure named ``Output``:

.. tabs::

    .. code-tab:: smithy

        operation MyOperation() -> Output

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyOperation": {
                        "type": "operation",
                        "output": "Output"
                    }
                }
            }
        }


.. _operation-errors:

Operation errors
````````````````

The errors of an operation is an optional, comma-separated, list of shape IDs
that MUST target structure shapes that are marked with the
:ref:`error-trait`. Errors defined on an operation are errors that can
potentially occur when calling an operation.

The following example defines an operation shape that accepts no input,
returns no output, and can potentially return the
``NotFound`` or ``BadRequest`` error structures.

.. tabs::

    .. code-tab:: smithy

        operation MyOperation() errors [NotFound, BadRequest]

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyOperation": {
                        "type": "operation",
                        "errors": ["NotFound", "BadRequest"]
                    }
                }
            }
        }


..  _resource:

Resource
--------

Smithy defines a :dfn:`resource` as an entity with an identity that has a
set of operations.

A resource shape is defined using a :token:`resource_statement` and supports
the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 30 60

    * - Property
      - Type
      - Description
    * - :ref:`identifiers <resource-identifiers>`
      - Map<String, :ref:`shape-id`>
      - Defines identifier names and shape IDs used to identify the resource.
    * - :ref:`create <create-lifecycle>`
      - :ref:`shape-id`
      - Defines the lifecycle operation used to create the resource.
    * - :ref:`read <read-lifecycle>`
      - :ref:`shape-id`
      - Defines the lifecycle operation used to retrieve the resource.
    * - :ref:`update <update-lifecycle>`
      - :ref:`shape-id`
      - Defines the lifecylce operation used to update the resource.
    * - :ref:`delete <delete-lifecycle>`
      - :ref:`shape-id`
      - Defines the operation used to delete the resource.
    * - :ref:`list <list-lifecycle>`
      - :ref:`shape-id`
      - Defines the lifecycle operation used to list resources of this type.
    * - operations
      - [:ref:`shape-id`]
      - Binds a list of non-lifecycle operations to the resource.
    * - resources
      - [:ref:`shape-id`]
      - Binds a list of resources to this resource as a child resource,
        forming a containment relationship. The resources MUST NOT have a
        cyclical containment hierarchy, and a resource can not be bound more
        than once in the entire closure of a resource or service.


.. _resource-identifiers:

Identifiers
```````````

:dfn:`Identifiers` are used to refer to a specific resource within a service.
The identifiers property of a resource is a map of identifier names to
:ref:`shape IDs <shape-id>` that MUST target string shapes.

For example, the following model defines a ``Forecast`` resource with a
single identifier named ``forecastId`` that targets the ``ForecastId`` shape:

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        resource Forecast {
          identifiers: {
            forecastId: ForecastId
          }
        }

        string ForecastId

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "Forecast": {
                        "type": "resource",
                        "identifiers": {
                            "forecastId": "ForecastId"
                        }
                    },
                    "ForecastId": {
                        "type": "string"
                    }
                }
            }
        }

When a resource is bound as a child to another resource using the "resources"
property, all of the identifiers of the parent resource MUST be repeated
verbatim in the child resource, and the child resource MAY introduce any
number of additional identifiers.

:dfn:`Parent identifiers` are the identifiers of the parent of a resource.
All parent identifiers MUST be bound as identifiers in the input of every
operation bound as a child to a resource. :dfn:`Child identifiers` are the
identifiers that a child resource contains that are not present in the parent
identifiers.

For example, given the following model,

.. tabs::

    .. code-tab:: smithy

        resource ResourceA {
          identifiers: {
            a: String
          },
          resources: [ResourceB],
        }

        resource ResourceB {
          identifiers: {
            a: String,
            b: String,
          },
          resources: [ResourceC],
        }

        resource ResourceC {
          identifiers: {
            a: String,
            b: String,
            c: String,
          }
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "ResourceA": {
                        "type": "resource",
                        "resources": ["ResourceB"],
                        "identifiers": {
                            "a": "String"
                        }
                    },
                    "ResourceB": {
                        "type": "resource",
                        "resources": ["ResourceC"],
                        "identifiers": {
                            "a": "String",
                            "b": "String"
                        }
                    },
                    "ResourceC": {
                        "type": "resource",
                        "identifiers": {
                            "a": "String",
                            "b": "String",
                            "c": "String"
                        }
                    }
                }
            }
        }

``ResourceB`` is a valid child of ``ResourceA`` and contains a child
identifier of "b". ``ResourceC`` is a valid child of ``ResourceB`` and
contains a child identifier of "c".

However, the following defines two *invalid* child resources that do not
define an ``identifiers`` property that is compatible with their parents:

.. tabs::

    .. code-tab:: smithy

        resource ResourceA {
          identifiers: {
            a: String,
            b: String,
          },
          resources: [Invalid1, Invalid2],
        }

        resource Invalid1 {
          // Invalid: missing "a".
          identifiers: {
            b: String,
          },
        }

        resource Invalid2 {
          identifiers: {
            a: String,
            // Invalid: does not target the same shape.
            b: SomeOtherString,
          },
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "ResourceA": {
                        "type": "resource",
                        "identifiers": {
                            "a": "String",
                            "b": "String"
                        },
                        "resources": ["Invalid1", "Invalid2"]
                    },
                    "Invalid1": {
                        "type": "resource",
                        "identifiers": {
                            "b": "String"
                        }
                    },
                    "Invalid2": {
                        "type": "resource",
                        "identifiers": {
                            "a": "String",
                            "b": "SomeOtherString"
                        }
                    }
                }
            }
        }


.. _binding-identifiers:

Binding identifiers to operations
`````````````````````````````````

*Identifier bindings* indicate which top-level members of the input structure
of an operation provide values for the identifiers of a resource.

**Validation**

- Child resources MUST provide identifier bindings for all of its parent's
  identifiers.
- Identifier bindings are only formed on input structure members that are
  marked as :ref:`required-trait`.
- Resource operations MUST form a valid *instance operation* or
  *collection operation*.

.. _instance-operations:

:dfn:`Instance operations` are formed when all of the identifiers of a resource
are bound to the input structure of an operation or when a resource has no
identifiers. The :ref:`read <read-lifecycle>` , :ref:`update <update-lifecycle>`,
and :ref:`delete <delete-lifecycle>` lifecycle operations are examples
of instance operations. An operation bound to a resource MUST form a valid
instance operation if it is not marked with the :ref:`collection-trait`.

.. _collection-operations:

:dfn:`Collection operations` are used when an operation is meant to operate on
a collection of resources rather than a specific resource. Collection
operations are formed when an operation bound to a resource is marked with the
``collectionTrait`` and one or more of the child identifiers of a resource
are not bound to the input structure of an operation. The
:ref:`list <list-lifecycle>` lifecycle operation is an example of a collection
operation.


.. _implicit-identifier-bindings:

Implicit identifier bindings
````````````````````````````

*Implicit identifier bindings* are formed when the input of an operation
contains member names that target the same shapes that are defined in the
"identifiers" property of the resource to which an operation is bound.

For example, given the following model,

.. tabs::

    .. code-tab:: smithy

        resource Forecast {
          identifiers: {
            forecastId: ForecastId,
          },
          read: GetForecast,
        }

        @readonly
        operation GetForecast(GetForecastInput) -> GetForecastOutput

        structure GetForecastInput: {
          @required
          forecastId: ForecastId,
        }

        structure GetForecastOutput {
          @required
          weather: WeatherData,
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "Forecast": {
                        "type": "resource",
                        "identifiers": {
                            "forecastId": "ForecastId"
                        },
                        "read": "GetForecast"
                    },
                    "GetForecast": {
                        "readonly": true,
                        "type": "operation",
                        "input": "GetForecastInput",
                        "output": "GetForecastOutput"
                    },
                    "GetForecastInput": {
                        "type": "structure",
                        "members": {
                            "forecastId": {
                                "target": "ForecastId",
                                "required": true
                            }
                        }
                    },
                    "GetForecastOutput": {
                        "type": "structure",
                        "members": {
                            "weather": {
                                "target": "WeatherData",
                                "required": true
                            }
                        }
                    }
                }
            }
        }

``GetForecast`` forms a valid instance operation because the operation is
not marked with the ``collection`` trait and ``GetForecastInput`` provides
*implicit identifier bindings* by defining a required "forecastId" member
that targets the same shape as the "forecastId" identifier of the resource.

Implicit identifier bindings for collection operations are created in a
similar way to an instance operation, but a collection operation is marked
with the ``collection`` trait and MUST NOT contain identifier bindings for
*all* child identifiers of the resource.

Given the following model,

.. tabs::

    .. code-tab:: smithy

        resource Forecast {
          identifiers: {
            forecastId: ForecastId,
          },
          operations: [BatchPutForecasts],
        }

        @collection
        operation BatchPutForecasts(BatchPutForecastsInput) -> BatchPutForecastsOutput

        structure BatchPutForecastsInput {
          @required
          forecasts: BatchPutForecastList,
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "Forecast": {
                        "type": "resource",
                        "identifiers": {
                            "forecastId": "ForecastId"
                        },
                        "operations": ["BatchPutForecasts"]
                    },
                    "BatchPutForecasts": {
                        "type": "operation",
                        "collection": true,
                        "input": "BatchPutForecastsInput",
                        "output": "BatchPutForecastsOutput"
                    },
                    "BatchPutForecastsInput": {
                        "type": "structure",
                        "members": {
                            "forecasts": {
                                "target": "BatchPutForecastList",
                                "required": true
                            }
                        }
                    }
                }
            }
        }

``BatchPutForecasts`` forms a valid collection operation with implicit
identifier bindings because the operation is marked with the ``collection``
trait and ``BatchPutForecastsInput`` does not require an input member named
"forecastId" that targets ``ForecastId``.


Explicit identifier bindings
````````````````````````````

*Explicit identifier bindings* are defined by applying the
:ref:`resourceIdentifier-trait` to a member of the input of for an
operation bound to a resource. Explicit bindings are necessary when the name of
the input structure member differs from the name of the resource identifier to
which the input member corresponds.

For example, given the following,

.. code-block:: smithy

    resource Forecast {
      // continued from above
      resources: [HistoricalForecast],
    }

    resource HistoricalForecast {
      identifiers: {
        forecastId: ForecastId,
        historicalId: HistoricalForecastId,
      },
      read: GetHistoricalForecast,
      list: ListHistoricalForecasts,
    }

    @readonly
    operation GetHistoricalForecast(GetHistoricalForecastInput) -> GetHistoricalForecastOutput

    structure GetHistoricalForecastInput {
      @required
      @resourceIdentifier(forecastId)
      customForecastIdName: ForecastId,

      @required
      @resourceIdentifier(historicalId)
      customHistoricalIdName: String
    }

the :ref:`resourceIdentifier-trait` on ``GetHistoricalForecastInput$customForecastIdName``
maps it to the "forecastId" identifier is provided by the
"customForecastIdName" member, and the :ref:`resourceIdentifier-trait`
on ``GetHistoricalForecastInput$customHistoricalIdName`` maps that member
to the "historicalId" identifier.


.. _lifecycle-operations:

Lifecycle operations
````````````````````

:dfn:`Lifecycle operations` are used to transition the state of a resource
using well-defined semantics. Lifecycle operations are defined by setting the
``create``, ``read``, ``update``, ``delete``, and ``list`` properties of a
resource to target an operation shape.

The following snippet defines a resource with each lifecycle method:

.. code-block:: smithy

    resource Forecast {
      identifiers: {
        forecastId: ForecastId,
      },
      create: CreateForecast,
      read: GetForecast,
      update: UpdateForecast,
      delete: DeleteForecast,
      list: ListForecasts,
    }


.. _create-lifecycle:

Create lifecycle
````````````````

The ``create`` operation defines the canonical operation used to create the
resource.

**Validation**

- Create operations MUST NOT be marked as :ref:`readonly-trait`.
- Create operations MUST form valid :ref:`instance operations <instance-operations>`
  or :ref:`collection operations <collection-operations>`.

The following snippet defines the ``CreateForecast`` operation.

.. code-block:: smithy

    operation CreateForecast(CreateForecastInput) -> CreateForecastOutput

A create operation can be defined as an :ref:`instance operation <instance-operations>`
such that the client is responsible for providing the identifiers of the
resource when it is created. This kind of create operation SHOULD be marked with
the :ref:`idempotent-trait`. For example:

.. code-block:: smithy

    structure CreateForecastInput {
      // The client provides the resource identifier.
      @required
      forecastId: ForecastId,

      chanceOfRain: Float
    }

A create operation can be defined as a :ref:`collection operation <collection-operations>`
such that the identifier(s) of a resource are defined by the service when a
resource is created. This kind of create operation is formed when the operation
is marked with the :ref:`collection-trait`. For example:

.. code-block:: smithy

    @collection
    operation CreateForecast(CreateForecastInput) -> CreateForecastOutput

    structure CreateForecastInput {
      // No identifier is provided by the client, so the service is
      // responsible for providing the identifier of the resource.
      chanceOfRain: Float,
    }


.. _read-lifecycle:

Read lifecycle
``````````````

The ``read`` operation is the canonical operation used to retrieve the current
representation of a resource.

**Validation**

- Read operations MUST be valid :ref:`instance operations <instance-operations>`.
- Read operations MUST be marked as :ref:`readonly-trait`.

For example:

.. code-block:: smithy

    @readonly
    operation GetForecast(GetForecastInput) -> GetForecastOutput errors [ResourceNotFound]

    structure GetForecastInput {
      @required
      forecastId: ForecastId,
    }


.. _update-lifecycle:

Update lifecycle
````````````````

The ``update`` operation is the canonical operation used to update a
resource.

**Validation**

- Update operations MUST be valid :ref:`instance operations <instance-operations>`.
- Update operations MUST NOT be marked as :ref:`readonly-trait`.

For example:

.. code-block:: smithy

    operation UpdateForecast(UpdateForecastInput) -> UpdateForecastOutput errors [ResourceNotFound]

    structure UpdateForecastInput {
      @required
      forecastId: ForecastId,

      chanceOfRain: Float,
    }


.. _delete-lifecycle:

Delete lifecycle
````````````````

The ``delete`` operation is canonical operation used to delete a resource.

**Validation**

- Delete operations MUST be valid :ref:`instance operations <instance-operations>`.
- Delete operations MUST NOT be marked as :ref:`readonly-trait`.
- Delete operations MUST be marked as :ref:`idempotent-trait`.

For example:

.. code-block:: smithy

    @idempotent
    operation DeleteForecast(DeleteForecastInput) -> DeleteForecastOutput errors [ResourceNotFound]

    structure DeleteForecastInput {
      @required
      forecastId: ForecastId,
    }


.. _list-lifecycle:

List lifecycle
``````````````

The ``list`` operation is the canonical operation used to list a
collection of resources.

**Validation**

- List operations MUST be marked with the :ref:`collection-trait` and MUST
  form valid :ref:`collection operations <collection-operations>`.
- List operations MUST be marked as :ref:`readonly-trait`.
- The output of a list operation SHOULD contain references to the resource
  being listed.
- List operations SHOULD be :ref:`paginated <paginated-trait>`.

For example:

.. code-block:: smithy

    @collection @readonly
    @paginated(inputToken: nextToken, pageSize: maxResults,
               outputToken: nextToken, items: "forecasts")
    operation ListForecasts(ListForecastsInput) -> ListForecastsOutput

    structure ListForecastsInput {
      maxResults: Integer,
      nextToken: String
    }

    structure ListForecastsOutput {
      nextToken: String,
      @required
      forecasts: ForecastList
    }

    list ForecastList {
      member: ForecastId
    }


.. _referencing-resources:

Referencing resources
`````````````````````

References between resources can be defined in a Smithy model at design-time.
Resource references allow tooling to understand the relationships between
resources and how to dereference the location of a resource.

A reference to a resource is formed when the :ref:`references-trait`
is applied to a structure or string shape. The following example creates a
reference to a ``HistoricalForecast`` resource (a resource that requires the
"forecastId" and "historicalId" identifiers):

.. code-block:: smithy

    @references(
      historicalForecast: { resource: HistoricalForecast, service: Weather }
    )
    structure HistoricalReference {
      forecastId: ForecastId,
      historicalId: HistoricalForecastId
    }

Notice that in the above example, the identifiers of the resource were not
explicitly mapped to structure members. This is because the targeted structure
contains members with names that match the names of the identifiers of the
``HistoricalForecast`` resource.

Explicit mappings between identifier names and structure member names can be
defined if needed. For example:

.. code-block:: smithy

    @references(
      historicalForecast: {
        resource: HistoricalForecast,
        service: Weather,
        ids: { forecastId: customForecastId, historicalId: customHistoricalId }
      }
    )
    structure AnotherHistoricalReference {
      customForecastId: String,
      customHistoricalId: String,
    }

A reference can be formed on a string shape for resources that have one
identifier. References applied to a string shape MUST omit the "ids"
property in the reference.

.. code-block:: smithy

    resource SimpleResource {
      identifiers: {
        foo: 'String',
      }
    }

    @references(
      simpleResource: {
        resource: SimpleResource,
        service: MyService
      }
    )
    string SimpleResourceReference

See the :ref:`references-trait` for more information about references.


..  _prelude:

-------------
Prelude model
-------------

Smithy models automatically include a *prelude* model. The prelude model
defines various simple shapes and every trait defined in the
:ref:`core specifications <core-specifications>`. All of the shapes and
traits defined in the prelude are available inside of the ``smithy.api``
namespace.


.. _relative-prelude-targets:

Relative prelude targets
========================

Relative shape ID targets of :ref:`members <member>`,
:ref:`resource identifiers <resource-identifiers>`,
:ref:`trait shape <trait-definition-properties>` targets, and
:ref:`idRef-trait`\s that do not resolve to shapes defined in their same
namespace can resolve to any of the defined
:ref:`prelude shapes <prelude-shapes>`.

Given the following model,

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        list MyList1 {
          member: String
        }

        list MyList2 {
          member: Integer
        }

        integer Integer

    .. code-tab:: json

        {
          "smithy": "0.1.0",
          "smithy.example": {
            "shapes": {
              "MyList1": {
                "type": "list",
                "member": {
                  "target": "String"
                }
              },
              "MyList2": {
                "type": "list",
                "member": {
                  "target": "Integer"
                }
              },
              "Integer": {
                "type": "integer"
              }
            }
          }
        }

The resolved shape targeted by the member of ``MyList1`` is
``smithy.api#String``, and the resolved shape targeted by the member of
``MyList2`` is ``smithy.example#Integer``.


.. _prelude-shapes:

Prelude shapes
==============

The prelude model is defined using the following :ref:`JSON AST <json-ast>`:

.. code-block:: smithy

    $version: "0.1.0"
    namespace smithy.api

    string String

    blob Blob

    bigInteger BigInteger

    bigDecimal BigDecimal

    timestamp Timestamp

    document Document

    @box
    boolean Boolean

    boolean PrimitiveBoolean

    @box
    byte Byte

    byte PrimitiveByte

    @box
    short Short

    short PrimitiveShort

    @box
    integer Integer

    integer PrimitiveInteger

    @box
    long Long

    long PrimitiveLong

    @box
    float Float

    float PrimitiveFloat

    @box
    double Double

    double PrimitiveDouble


.. _shape-id:

--------
Shape ID
--------

A :dfn:`shape ID` is used to refer to other shapes in the model. Shape IDs
adhere to the following syntax:

::

    com.foo.baz#ShapeName$memberName
    \_________/ \_______/ \________/
         |          |          |
     Namespace  Shape name  Member name

Shape IDs are formally defined by the :ref:`shape ID ABNF <shape-id-abnf>`.

Absolute shape ID
    An :dfn:`absolute shape ID` starts with a :token:`namespace` name,
    followed by "``#``", followed by a *relative shape ID*.
Relative shape ID
    A :dfn:`relative shape ID` contains a :token:`shape name <identifier>`
    and an optional :token:`member name <identifier>`. The namespace of the
    shape ID is either inherited from the namespace in which the shape ID is
    defined or ``smithy.api`` in cases where a relative shape ID resolves to
    a :ref:`prelude shape <relative-prelude-targets>`. The shape name and
    member name are separated by the "``$``" symbol if a member name is
    present.


Shape ID conflicts
==================

While shape IDs used within a model are case-sensitive, no two shapes in
the model can have the same case-insensitive shape ID. For example,
``com.Foo#baz`` and ``com.foo#baz`` are not allowed in the same model. This
property also extends to member names: ``com.foo#Baz$bar`` and
``com.foo#Baz$Bar`` are not allowed on the same structure.


.. _example-shape-ids:

Example shape IDs
=================

The following table provides examples of shape IDs:

============================  ==========  =========================================================================================
Shape ID                      Type         Description
============================  ==========  =========================================================================================
``Example``                   relative    Resolves to a shape named ``Example`` in the current namespace.
``Example$member``            relative    Resolves to the member of a shape named ``Example`` in the current namespace.
``foo.baz#Example``           absolute    Resolves to a shape named ``Example`` in the ``foo.baz`` namespace.
``foo.baz#Example$baz``       absolute    Resolves to the ``baz`` member of a shape named ``Example`` in the ``foo.baz`` namespace.
``foo.baz#Example$member``    absolute    Resolves to the member of a shape named ``Example`` in the ``foo.baz`` namespace.
============================  ==========  =========================================================================================


.. _relative-shape-id:

Relative shape ID namespace resolution
======================================

The namespace of a relative shape ID resolves to the namespace in which the
shape ID is defined.

For example, given the following Smithy model,

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        string MyString

        structure MyStructure {
          someMemberName: MyString,
        }

    .. code-tab:: json

        {
          "smithy": "0.1.0",
          "smithy.example": {
            "shapes": {
              "MyString": {
                "type": "string"
              },
              "MyStructure": {
                "type": "structure",
                "members": {
                  "someMemberName": {
                    "target": "MyString"
                  }
                }
              }
            }
          }
        }

the structure member, ``someMemberName``, is defined using a relative
shape ID.

Though unnecessary, the structure member ``someMemberName`` could have utilized
an absolute shape ID:

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        string MyString

        structure MyStructure {
          someMemberName: smithy.example#MyString,
        }

    .. code-tab:: json

        {
          "smithy": "0.1.0",
          "smithy.example": {
            "shapes": {
              "MyString": {
                "type": "string"
              },
              "MyStructure": {
                "type": "structure",
                "members": {
                  "someMemberName": {
                    "target": "smithy.example#MyString"
                  }
                }
              }
            }
          }
        }


.. _shape-id-member-names:

Shape ID member names
=====================

A :ref:`member` of an :ref:`aggregate shape <aggregate-types>` can be
referenced in a shape ID by appending "``$``" followed by the
appropriate member name. Member names for each shape are defined as follows:

.. list-table::
    :header-rows: 1
    :widths: 25 30 45

    * - Shape ID
      - Syntax
      - Examples
    * - :ref:`structure` member
      - ``<name>$<member-name>``
      - ``Shape$foo``, ``ns.example#Shape$baz``
    * - :ref:`union` member
      - ``<name>$<member-name>``
      - ``Shape$foo``, ``ns.example#Shape$baz``
    * - :ref:`list` member
      - ``<name>$member``
      - ``Shape$member``, ``ns.example#Shape$member``
    * - :ref:`set` member
      - ``<name>$member``
      - ``Shape$member``, ``ns.example#Shape$member``
    * - :ref:`map` key
      - ``<name>$key``
      - ``Shape$key``, ``ns.example#Shape$key``
    * - :ref:`map` value
      - ``<name>$value``
      - ``Shape$value``, ``ns.example#Shape$value``


.. _shape-names:

Shape names
===========

Consumers of a Smithy model MAY choose to inflect shape names, structure
member names, and other facets of a Smithy model in order to expose a more
idiomatic experience to particular programming languages. In order to make this
easier for consumers of a model, model authors SHOULD utilize a strict form of
PascalCase in which only the first letter of acronyms, abbreviations, and
initialisms are capitalized.

===========   ===============
Recommended   Not recommended
===========   ===============
UserId        UserID
ResourceArn   ResourceARN
IoChannel     IOChannel
HtmlEntity    HTMLEntity
HtmlEntity    HTML_Entity
===========   ===============


.. _traits:

------
Traits
------

*Traits* are model components that can be attached to :doc:`shapes <index>`
to describe additional information about the shape; shapes provide the
structure and layout of an API, while traits provide refinement and style.
Protocols MAY require specific traits to be applied in a model.

Trait names are case-sensitive; it is invalid, for example, to write the
:ref:`documentation-trait` as "Documentation").


Applying traits to shapes
=========================

Trait values immediately preceding a shape definition are applied to the
shape.

The following example applies the ``sensitive`` and ``documentation`` trait
to ``MyString``:

.. code-block:: smithy

    namespace smithy.example

    @sensitive
    @documentation("Contains a string")
    string MyString

Traits can be applied to shapes outside of a shape's definition using the
``apply`` statement. This can be useful for allowing different teams within
the same organization to independently own different facets of a model.
For example, a service team could own the Smithy model that defines the
shapes and traits of the API, and a documentation team could own a Smithy
model that applies documentation traits to the shapes.

The following example applies the :ref:`documentation-trait` and
:ref:`length-trait` to the ``smithy.example#MyString`` shape:

.. code-block:: smithy

    namespace smithy.example

    apply MyString @documentation("This is my string!")
    apply MyString @length(min: 1, max: 10)


.. _trait-values:

Trait values
============

The value provided for a trait depends on the trait is an annotation trait
or a modeled trait.


Annotation trait values
-----------------------

*Annotation traits* are traits that have no value and are either
present or not present. Annotation trait values can be set to ``true``
or ``null`` and can be omitted in the IDL.

For example, the :ref:`sensitive-trait` is an annotation trait:

.. code-block:: smithy

    @sensitive
    string MyString


Modeled trait values
--------------------

Modeled traits are traits that define a ``shape`` in their :ref:`trait definition <trait-definition>`.

Modeled trait values are defined in the IDL by enclosing the value in
parenthesis. A trait that accepts a ``list``, ``map``, ``set``, or
``structure`` with no required members does not require a value and can
omit parenthesis. Omitting a trait value is the same as explicitly setting the
trait to ``null``.


Structure, map, and union trait values
``````````````````````````````````````

Traits that accept a ``structure``, ``union``, or ``map`` are defined using
a JSON-like object in the Smithy IDL or a JSON object in the
:ref:`JSON AST <json-ast>`.

The wrapping braces for the object MUST be omitted in the Smithy IDL.
For example:

.. code-block:: smithy

    @structuredTrait(foo: bar, baz: bam)

Nested structure, map, and union values are defined like JSON value
using the :ref:`node value <node-values>` productions:

.. code-block:: smithy

    @structuredTrait(
        foo: {
            bar: baz,
            qux: true,
        }
    )

If the structure of ``structuredTrait`` has no required properties,
the value can be omitted:

.. code-block:: smithy

    @structuredTrait

This is equivalent to setting the trait to ``null``:

.. code-block:: smithy

    @structuredTrait(null)


Other trait values
``````````````````

All other trait values MUST adhere to the JSON type mappings defined
in :ref:`trait-definition-values` table.


.. _trait-name-resolution:

Trait name resolution
=====================

When a trait is added to a shape, the trait is *resolved* against a namespace.
If the trait name includes a namespace, no resolution is necessary. For
example, the following definition unambiguously references a trait named
``hello`` in the ``foo.baz`` namespace:

.. code-block:: smithy

    @foo.baz#hello("test")
    string MyString

If the trait name does not contain a namespace, trait resolution occurs.
If the trait name matches a trait defined in the *current namespace*, then
that trait is resolved as the trait to apply to the shape. For example, the
following definition applies a trait using a relative trait name in the current
namespace:

.. code-block:: smithy

    namespace smithy.example

    trait hello {
      selector: "*",
    }

    @hello("test")
    string MyString

If no trait can be found by name in the current namespace, then the trait
is assumed to be one of the built-in traits defined inside of the
``smithy.api`` namespace. The following definition applies the sensitive
trait without specifying the ``smithy.api`` namespace in the trait name:

.. code-block:: smithy

    namespace smithy.example

    @sensitive
    string MyString


.. _trait-conflict-resolution:

Trait conflict resolution
=========================

Trait conflict resolution is used when the same trait is applied multiple
times to a shape. Duplicate traits applied to shapes are allowed if, and only
if, both trait values are arrays or both values are exactly equal. If both
values are arrays, then the traits are concatenated into a single array. If
both values are equal, then the conflict is ignored. All other instances of
trait collisions are prohibited.

The following model definition is **invalid** because the ``length`` trait is
duplicated on the ``MyList`` shape with different values:

.. code-block:: smithy

    namespace smithy.example

    @length(min: 0, max: 10)
    list MyList {
      member: String
    }

    apply MyList @length(min: 10, max: 20)

The following model definition is **valid** because the ``length`` trait is
duplicated on the ``MyList`` shape with the same values:

.. code-block:: smithy

    namespace smithy.example

    @length(min: 0, max: 10)
    list MyList {
      member: String
    }

    apply MyList @length(min: 0, max: 10)

The following model definition is **valid** because the ``tags`` trait is
uses a :ref:`list` shape:

.. code-block:: smithy

    namespace smithy.example

    @tags([foo, baz, bar])
    string MyString

    // This is a valid trait collision on an array trait, tags.
    // tags becomes ["foo", "baz", "bar", "bar", "qux"]
    apply MyString @tags([bar, qux])


.. _trait-definition:

Trait definitions
=================

All Smithy traits are defined in the model using a trait definition.
Custom traits can be used in a model to extend Smithy beyond its built-in
capabilities. Traits MUST be declared before they can be used.

Traits are defined inside of a namespace using a
:ref:`trait statement <trait-statement>`. The following example
defines a trait named ``myTraitName`` in the ``smithy.example`` namespace:

.. code-block:: smithy

    namespace smithy.example

    trait myTraitName {
      selector: "*",
    }

After a trait is defined, it can be applied to any shape that matches its
selector. The following example applies the ``myTraitName`` trait to the
``MyString`` shape using a trait name that is relative to the current
namespace:

.. code-block:: smithy

    namespace smithy.example

    @myTraitName
    string MyString

Built-in traits are all defined in the Smithy :ref:`prelude <prelude>`
and are automatically available in every Smithy model.


.. _trait-definition-properties:

Trait definition properties
---------------------------

Trait definitions are objects that accept the following key-value pairs:

.. list-table::
    :header-rows: 1
    :widths: 10 20 70

    * - Property
      - Type
      - Description
    * - selector
      - string
      - **Required**. A valid :ref:`selector <selectors>` that defines where
        the trait can be applied. For example, a ``selector`` set to
        ``:test(list, map)`` means that the trait can be applied to a
        :ref:`list` or :ref:`map` shape.
    * - shape
      - :ref:`shape-id`
      - A shape ID that describes the format of the trait.
        :ref:`Values <trait-definition-values>` provided for the trait MUST
        be compatible with the referenced shape.

        If not defined, the trait is considered an *annotation trait*, meaning
        the trait has no value.
    * - conflicts
      - [string]
      - Defines traits that MUST NOT be applied to the same shape as the trait
        being defined. This allows traits to be defined as mutually exclusive.
        Each string value is the name of a trait (for example,
        ``smithy.example#foo``). The conflicts list does not support relative
        trait references to other traits defined in the same namespace; traits
        with no namespace are assumed to refer to shapes defined in
        ``smithy.api``. Each value MAY reference unknown traits that are not
        defined in the model.
    * - structurallyExclusive
      - boolean
      - Requires that only a single member of a structure can be marked with
        the trait.
    * - documentation
      - string
      - Defines documentation about the trait.
    * - externalDocumentation
      - string
      - A valid URL that defines more information about the trait.
    * - tags
      - [string]
      - Attaches a list of tags to the trait definition that allow the trait
        definition and all instances of the trait definition to be categorized
        and grouped. For example, these tags can be used filter certain traits
        from appearing in different representations of a Smithy model.
    * - deprecated
      - boolean
      - Indicates that a trait is deprecated and should no longer be used.
    * - deprecationReason
      - string
      - Text that explains why the trait is marked as deprecated.
        ``deprecationReason`` can only be set if ``deprecated`` is set to
        ``true``.

The following example defines two custom traits: ``beta`` and
``structuredTrait``:

.. code-block:: smithy

    namespace smithy.example

    // Define an annotation that can be applied to a structure member.
    trait beta {
      selector: "member:of(structure)",
    }

    // Define a trait that has a shape.
    trait structuredTrait {
      selector: "string",
      shape: StructuredTraitShape,
      conflicts: ["smithy.example#beta"],
    }

    // The shape of the "structuredTrait".
    structure StructuredTraitShape {
      @required
      lorem: StringShape,
      @required
      ipsum: StringShape,
      dolor: StringShape,
    }

    // Apply the "beta" trait to the "foo" member.
    structure MyShape {
      @required
      @beta
      foo: StringShape,
    }

    // Apply the structuredTrait to the string.
    @structuredTrait(
      lorem: "This is a custom trait!",
      ipsum: "lorem and ipsum are both required values."
    )
    string StringShape


.. _trait-definition-values:

Trait JSON values
-----------------

The value provided for a trait MUST be compatible with the ``shape`` defined
for the trait. The following table defines each shape type that is available
to target from trait definitions and how values for those shapes are defined
in JSON.

.. list-table::
    :header-rows: 1
    :widths: 20 20 60

    * - Smithy type
      - JSON type
      - Description
    * - blob
      - string
      - A ``string`` value that is base64 encoded. The bytes provided for a
        blob MUST be compatible with the ``format`` of the blob.
    * - boolean
      - boolean
      - Can be set to ``true`` or ``false``.
    * - byte
      - number
      - The value MUST fall within the range of -128 to 127
    * - short
      - number
      - The value MUST fall within the range of -32,768 to 32,767
    * - integer
      - number
      - The value MUST fall within the range of -2^31 to (2^31)-1.
    * - long
      - number
      - The value MUST fall within the range of -2^63 to (2^63)-1.
    * - float
      - number
      - A normal JSON number.
    * - double
      - number
      - A normal JSON number.
    * - bigDecimal
      - string
      - bigDecimal values are serialized as strings to avoid rounding issues
        when parsing a Smithy model in various languages.
    * - bigInteger
      - string | integer
      - bigInteger values can be serialized as strings to avoid truncation
        issues when parsing a Smithy model in various languages.
    * - string
      - string
      - The provided value SHOULD be compatible with the ``format`` of the
        string shape if present; however, this is not validated by Smithy.
    * - timestamp
      - number | string
      - If a number is provided, it represents Unix epoch seconds with optional
        millisecond precision. If a string is provided, it MUST be a valid
        :rfc:`3339` string with optional millisecond precision
        (e.g., ``1990-12-31T23:59:60Z``).
    * - list
      - array
      - Each value in the array MUST be compatible with the referenced member.
    * - map
      - object
      - Each key MUST be compatible with the ``key`` member of the map, and
        each value MUST be compatible with the ``value`` member of the map.
    * - structure
      - object
      - All members marked as required MUST be provided in a corresponding
        key-value pair. Each key MUST correspond to a single member name of
        the structure. Each value MUST be compatible with the member that
        corresponds to the member name.
    * - union
      - object
      - The object MUST contain a single single key-value pair. The key MUST be
        one of the member names of the union shape, and the value MUST be
        compatible with the corresponding shape.

Trait values MUST be compatible with any constraint traits found related to the
shape being validated.


.. _trait-selector:

Trait selector
--------------

A *trait selector* is a :ref:`selector <selectors>` used to define where in the
model it is acceptable for the trait to be applied. Built-in traits listed in
the Smithy specification define where they can be applied using the same syntax
that is used when defining the valid targets of
:ref:`trait definitions <trait-definition>`.



Scope of member traits
======================

Traits that target :ref:`member shapes <member>` apply only in the
context of the member shape and do not affect the shape targeted by the
member. Traits applied to a :ref:`member` shape supersede traits applied to
the shape referenced by the member and do not conflict.


Type refinement traits
======================


.. _box-trait:

``box`` trait
-------------

Summary
    Indicates that a shape is boxed. When a :ref:`member <member>` is marked
    with this trait or the shape targeted by a member is marked with this
    trait, the member may or may not contain a value, and the member has no
    :ref:`default value <default-values>`.

    Boolean, byte, short, integer, long, float, and double shapes are only
    considered boxed if they are marked with the ``box`` trait. All other
    shapes are always considered boxed.
Trait selector
    .. code-block:: css

        :test(boolean, byte, short, integer, long, float, double,
              member > :test(boolean, byte, short, integer, long, float, double))

    *A boolean, byte, short, integer, long, float, double shape or a member that targets one of these shapes*
Value type
    Annotation trait.

The ``box`` trait is primarily used to influence code generation. For example,
in Java, this might mean the value provided as the member of an aggregate
shape can be set to null. In a language like Rust, this might mean the value
is wrapped in an `Option type`_.

.. tabs::

    .. code-tab:: smithy

        @box
        integer BoxedInteger

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "BoxedInteger": {
                        "type": "integer",
                        "box": true
                    }
                }
            }
        }

The :ref:`prelude <prelude>` contains predefined simple shapes that can be
used in all Smithy models, including boxed and unboxed shapes.


.. _deprecated-trait:

``deprecated`` trait
--------------------

Summary
    Marks a shape or member as deprecated.
Trait selector
    ``*``
Value type
    ``object``

The ``deprecated`` trait is an object that supports the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 25 65

    * - Property
      - Type
      - Description
    * - message
      - ``string``
      - Provides a plain text message for a deprecated shape or member.
    * - since
      - ``string``
      - Provides a plain text date or version for when a shape or member was
        deprecated.

.. tabs::

    .. code-tab:: smithy

        @deprecated
        string SomeString

        @deprecated(message: "This shape is no longer used.", since: "1.3")
        string OtherString

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "SomeString": {
                        "type": "string",
                        "deprecated": {}
                    },
                    "OtherString": {
                        "type": "string",
                        "deprecated": {
                            "message": "This shape is no longer used.",
                            "since": "1.3"
                        }
                    }
                }
            }
        }


.. _error-trait:

``error`` trait
---------------

Summary
    Indicates that a structure shape represents an error. All shapes
    referenced by the :ref:`errors list of an operation <operation-errors>`
    MUST be targeted with this trait.
Trait selector
    ``structure``
Value type
    ``string`` that MUST be set to "client" or "server" to indicate if the
    client or server is at fault for the error.

The following structure defines a throttling error.

.. tabs::

    .. code-tab:: smithy

        @error(client)
        structure ThrottlingError {}

Note that this structure is lacking the ``retryable`` trait that generically
lets clients know that the error is retryable.

.. tabs::

    .. code-tab:: smithy

        @error(client)
        @retryable
        structure ThrottlingError {}

When using an HTTP-based protocol, it is recommended to add an
:ref:`httpError-trait` to use an appropriate HTTP status code with
the error.

.. tabs::

    .. code-tab:: smithy

        @error(client)
        @retryable
        @httpError(429)
        structure ThrottlingError {}

The ``message`` member of an error structure is special-cased. It contains
the human-readable message that describes the error. If the ``message`` member
is not defined in the structure, code generated for the error may not provide
an idiomatic way to access the error message (e.g., an exception message
in Java).

.. tabs::

    .. code-tab:: smithy

        @error(client)
        @retryable
        @httpError(429)
        structure ThrottlingError {
          @required
          message: String,
        }


Constraint traits
=================

Constraint traits are used to constrain the values that can be provided
for a shape.


.. _enum-trait:

``enum`` trait
--------------

Summary
    Constrains the acceptable values of a string to a fixed set.
Trait selector
    ``string``
Value type
    ``map`` of enum constant values to objects optionally containing a name,
    documentation, tags, and/or a deprecation flag.

Smithy models SHOULD apply the enum trait when string shapes have a fixed
set of allowable values.

The enum trait is a map of allowed string values to enum constant definition
objects. Enum values do not allow aliasing; all enum constant values MUST be
unique across the entire set.

An enum definition is an object that supports the following optional
properties:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - name
      - string
      - Defines a constant name to use when referencing an enum value.

        Enum constant names MUST start with an upper or lower case ASCII Latin
        letter (``A-Z`` or ``a-z``), or the ASCII underscore (``_``) and be
        followed by zero or more upper or lower case ASCII Latin letters
        (``A-Z`` or ``a-z``), ASCII underscores (``_``), or ASCII digits
        (``0-9``). That is, enum constant names MUST match the following
        regular expression: ``^[a-zA-Z_]+[a-zA-Z_0-9]*$``.

        The following stricter rules are recommended for consistency: Enum
        constant names SHOULD NOT contain any lowercase ASCII Latin letters
        (``a-z``) and SHOULD NOT start with an ASCII underscore (``_``). That
        is, enum names SHOULD match the following regular expression:
        ``^[A-Z]+[A-Z_0-9]*$``.
    * - documentation
      - string
      - Defines documentation about the enum value.
    * - tags
      - ``List<string>``
      - Attaches a list of tags that allow the enum value to be categorized and
        grouped.
    * - deprecated
      - ``boolean``
      - Whether the enum value should be considered deprecated for consumers of
        the Smithy model.

.. note::

      Consumers of a Smithy model MAY choose to represent enum values as
      constants. Those that do SHOULD use the enum definition's ``name``
      property, if specified. Consumers that choose to represent enums as
      constants SHOULD ensure that unknown enum names returned from a service
      do not cause runtime failures.

The following example defines an enum of valid string values for ``MyString``.

.. tabs::

    .. code-tab:: smithy

        @enum(
          t2.nano: {
            name: T2_NANO,
            documentation: "T2 instances are Burstable Performance "
                "Instances that provide a baseline level of CPU "
                "performance with the ability to burst above the "
                "baseline.",
            tags: [ebsOnly]
          },
          t2.micro: {
            name: T2_MICRO,
            documentation: "T2 instances are Burstable Performance "
                 "Instances that provide a baseline level of CPU "
                 "performance with the ability to burst above the "
                 "baseline.",
            tags: [ebsOnly]
          },
          m256.mega: {
            name: M256_MEGA,
            deprecated: true
          }
        )
        string MyString

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyString": {
                        "type": "string",
                        "enum": {
                            "t2.nano": {
                                "name": "T2_NANO",
                                "documentation": "T2 instances are ...",
                                "tags": ["ebsOnly"]
                            },
                            "t2.micro": {
                                "name": "T2_MICRO",
                                "documentation": "T2 instances are ..."
                                "tags": ["ebsOnly"]
                            },
                            "m256.mega": {
                                "name": "M256_MEGA",
                                "deprecated": true
                            }
                        }
                    }
                }
            }
        }


.. _idref-trait:

``idRef`` trait
---------------

Summary
    Indicates that a string value MUST contain a valid
    :ref:`shape ID <shape-id>`. The provided shape ID MAY be absolute or
    relative to the shape to which the trait is applied. A relative
    shape ID that does not resolve to a shape defined in the same namespace
    resolves to a shape defined in the :ref:`prelude <prelude>` if the
    prelude shape is not marked with the :ref:`private-trait`.

    The ``idRef`` trait is used primarily when declaring
    :ref:`trait definitions <trait-definition>` in a model. A trait definition
    that targets a string shape with the ``idRef`` trait indicates that when
    the defined trait is applied to a shape, the value of the trait MUST be
    a valid shape ID. The ``idRef`` trait can also be applied at any level of
    nesting on shapes referenced by trait definitions.
Trait selector
    ``:test(string, member > string)``

    *A string shape or a member that targets a string shape*
Value type
    ``object``

The ``idRef`` trait is an object that supports the following optional
properties:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - failWhenMissing
      - ``boolean``
      - When set to ``true``, the shape ID MUST target a shape that can be
        found in the model.
    * - selector
      - ``string``
      - Defines the :ref:`selector <selectors>` that the resolved shape,
        if found, MUST match.

        ``selector`` defaults to ``*`` when not defined.
    * - errorMessage
      - ``string``
      - Defines a custom error message to use when the shape ID cannot be
        found or does not match the ``selector``.

        A default message is generated when ``errorMessage`` is not defined.

To illustrate an example, a custom trait named ``integerRef`` is defined.
This trait can be attached to any shape, and the value of the trait MUST
contain a valid shape ID that targets an integer shape in the model.

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        trait integerRef {
          selector: "*",
          shape: IntegerRefTraitValue,
        }

        @idRef(failWhenMissing: true, selector: integer)
        string IntegerRefTraitValue

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "traitDefs": {
                    "integerRef": {
                        "selector": "*",
                        "shape": "IntegerRefTraitValue"
                    }
                },
                "shapes": {
                    "IntegerRefTraitValue": {
                        "type": "string",
                        "idRef": {
                            "failWhenMissing": true,
                            "selector": "integer"
                        }
                    }
                }
            }
        }

Given the following model,

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        @integerRef(NotFound)
        string InvalidShape1

        @integerRef(String)
        string InvalidShape2

        @integerRef("invalid-shape-id!")
        string InvalidShape3

        @integerRef(Integer)
        string ValidShape

        @integerRef(MyShape)
        string ValidShape2

        string MyShape

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "InvalidShape1": {
                        "type": "string",
                        "integerRef": "NotFound"
                    },
                    "InvalidShape2": {
                        "type": "string",
                        "integerRef": "String"
                    },
                    "InvalidShape3": {
                        "type": "string",
                        "integerRef": "invalid-shape-id!"
                    },
                    "ValidShape": {
                        "type": "string",
                        "integerRef": "Integer"
                    },
                    "ValidShape2": {
                        "type": "string",
                        "integerRef": "MyShape"
                    },
                    "MyShape": {
                        "type": "string"
                    }
                }
            }
        }

- ``InvalidShape1`` is invalid because the "NotFound" shape cannot be
  found in the model.
- ``InvalidShape2`` is invalid because "smithy.api#String" targets a
  string which does not match the "integer" selector.
- ``InvalidShape3`` is invalid because "invalid-shape-id!" is not a
  syntactically correct shape ID.
- ``ValidShape`` is valid because "smithy.api#Integer" targets an integer.
- ``ValidShape2`` is valid because "MyShape" is a relative ID that targets
  ``smithy.example#MyShape``.

.. _length-trait:

``length`` trait
----------------

Summary
    Constrains a shape to minimum and maximum number of elements or size.
Trait selector
    ``:test(list, map, string, blob, member > :each(list, map, string, blob))``

    *Any list, map, string, or blob; or a member that targets one of these shapes*
Value type
    ``object`` value

The length trait is an object that contains the following key value pairs:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - min
      - ``number``
      - Integer value that represents the minimum inclusive length of a shape.
    * - max
      - ``number``
      - Integer value that represents the maximum inclusive length of a shape.

At least one of min, max is required.

The following table describes what a length trait constrains when applied to
the corresponding shape:

===========  =====================================
Shape        Length constrains
===========  =====================================
list         The number of members
map          The number of key-value pairs
string       The number of Unicode code points
blob         The size of the blob in bytes
===========  =====================================

.. tabs::

    .. code-tab:: smithy

        @length(min: 1, max: 10)
        string MyString

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyString": {
                        "type": "string",
                        "length": {
                            "min": 1,
                            "max": 10
                        }
                    }
                }
            }
        }


.. _pattern-trait:

``pattern`` trait
-----------------

Summary
    Restricts string shape values to a specified regular expression.
Trait selector
    ``:test(string, member > string)``

    *A string or a member that targets a string*
Value type
    ``string`` value

Smithy regular expressions MUST be valid regular expressions according to the
`ECMA 262 regular expression dialect`_. Patterns SHOULD avoid the use of
conditionals, directives, recursion, lookahead, look-behind, back-references,
and look-around in order to ensure maximum compatibility across programming
languages.

.. tabs::

    .. code-tab:: smithy

        @pattern("\\w+")
        string MyString

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyString": {
                        "type": "string",
                        "pattern": "\\w+"
                    }
                }
            }
        }


.. _private-trait:

``private`` trait
-----------------

Summary
    Prevents models defined in a different namespace from referencing the
    targeted shape.
Trait selector
    ``*``
Value type
    Annotation trait

Shapes marked as ``private`` cannot be accessed outside of the namespace in
which the shape is defined. The ``private`` trait is meant only to control
access from within the model itself and SHOULD NOT influence code-generation
of the targeted shape.


.. _range-trait:

``range`` trait
---------------

Summary
    Restricts allowed values of byte, short, integer, long, float, double,
    bigDecimal, and bigInteger shapes within an acceptable lower and upper
    bound.
Trait selector
    ``:test(number, member > number)``

    *A number or a member that targets a number*
Value type
    ``object`` value

The length trait is an object that contains the following key value pairs:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - min
      - ``number``
      - Specifies the allowed inclusive minimum value.
    * - max
      - ``number``
      - Specifies the allowed inclusive maximum value.

At least one of ``min`` or ``max`` is required. ``min`` and ``max`` accept both
integers and real numbers. Real numbers may only be applied to float, double,
or bigDecimal shapes. ``min`` and ``max`` MUST fall within the allowable range
of the targeted numeric shape to which it is applied.

.. tabs::

    .. code-tab:: smithy

        @range(min: 1, max: 10)
        integer MyInt

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyInt": {
                        "type": "integer",
                        "range": {
                            "min": 1,
                            "max": 10
                        }
                    }
                }
            }
        }


.. _required-trait:

``required`` trait
------------------

Summary
    Marks a structure member as required, meaning a value for the member MUST
    be present.
Trait selector
    ``member:of(structure)``

    *Member of a structure*
Value type
    Annotation trait.

The required trait applies to structure data, operation input, output, and
errors. When a member that is part of the input of an operation is marked as
required, a client MUST provide a value for the member when calling the
operation. When a member that is part of the output of an operation or an
error is marked as required, a service MUST provide a value for the member
in a response.

.. tabs::

    .. code-tab:: smithy

        structure MyStructure {
          @required
          foo: FooString,
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyStructure": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "required": true,
                                "target": "FooString"
                            }
                        }
                    }
                }
            }
        }


.. _uniqueItems:

``uniqueItems`` trait
---------------------

Summary
    Indicates that the items in a :ref:`list` MUST be unique.
Trait selector
    ``:test(list > member > simpleType)``

    *A list that targets any simple type.*
Value type
    Annotation trait.

.. tabs::

    .. code-tab:: smithy

        @uniqueItems
        list MyList {
            member: String,
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyList": {
                        "type": "list",
                        "uniqueItems": true,
                        "member": {
                            "target": "String"
                        }
                    }
                }
            }
        }


Behavior traits
===============

Behavior traits are used to alter the behavior of operations.


.. _idempotencyToken-trait:

``idempotencyToken`` trait
--------------------------

Summary
    Defines the input member of an operation that is used by the server to
    identify and discard replayed requests.
Trait selector
    ``:test(member:of(structure) > string)``

    *Any structure member that targets a string*
Value type
    Annotation trait

Only a single member of the input of an operation can be targeted by the
``idempotencyToken`` trait; only top-level structure members of the input of an
operation are considered.

A unique identifier (typically a UUID_) SHOULD be used by the client when
providing the value for the request token member. When the request token is
present, the service MUST ensure that the request is not replayed within a
service-defined period of time. This allows the client to safely retry
operation invocations, including operations that are not read-only, that fail
due to networking issues or internal server errors. The service uses the
provided request token to identify and discard duplicate requests.

Client implementations MAY automatically provide a value for a request token
member if and only if the member is not explicitly provided.

.. tabs::

    .. code-tab:: smithy

        operation AllocateWidget(AllocateWidgetInput)

        structure AllocateWidgetInput {
          @idempotencyToken
          clientToken: String,
        }


.. _idempotent-trait:

``idempotent`` trait
--------------------

Summary
    Indicates that the intended effect on the server of multiple identical
    requests with an operation is the same as the effect for a single such
    request.
Trait selector
    ``operation``
Value type
    Annotation trait
Conflicts with
    :ref:`readonly-trait`

.. tabs::

    .. code-tab:: smithy

        @idempotent
        operation GetSomething(DeleteSomething) -> DeleteSomethingOuput

.. note::

    All operations that are marked as :ref:`readonly-trait` are inherently
    idempotent.


.. _readonly-trait:

``readonly`` trait
------------------

Summary
    Indicates that an operation is effectively read-only.
Trait selector
    ``operation``
Value type
    Annotation trait
Conflicts with
    :ref:`idempotent-trait`

.. tabs::

    .. code-tab:: smithy

        @readonly
        operation GetSomething(GetSomethingInput) -> GetSomethingOutput


.. _retryable-trait:

``retryable`` trait
-------------------

Summary
    Indicates that an error MAY be retried by the client.
Trait selector
    ``structure[trait|error]``

    *A structure shape with the error trait*
Value type
    Annotation trait

.. tabs::

    .. code-tab:: smithy

        @error(server)
        @retryable
        @httpError(503)
        structure ServiceUnavailableError {}


.. _pagination:

.. _paginated-trait:

``paginated`` trait
-------------------

Summary
    The ``paginated`` trait indicates that an operation intentionally limits
    the number of results returned in a single response and that multiple
    invocations might be necessary to retrieve all results.
Trait selector
    ``operation``
Value type
    ``object`` value

Pagination is the process of dividing large result sets into discrete
pages. Smithy provides a built-in pagination mechanism that utilizes a
cursor.

The ``paginated`` trait is an object that contains the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - inputToken
      - ``string``
      - **Required**. The name of the operation input member that represents
        the continuation token. When this value is provided as operation input,
        the service returns results from where the previous response left off.
        This input member MUST NOT be required and MUST target a string shape.
    * - outputToken
      - ``string``
      - **Required**. The name of the operation output member that represents
        the continuation token. When this value is present in operation output,
        it indicates that there are more results to retrieve. To get the next
        page of results, the client uses the output token as the input token of
        the next request. This  output member MUST NOT be required and MUST
        target a string shape.
    * - items
      - ``string``
      - The name of a top-level output member of the operation that is the
        data that is being paginated across many responses. The named output
        member, if specified, MUST target a :ref:`list` or :ref:`map`.
    * - pageSize
      - ``string``
      - The name of an operation input member that limits the maximum number
        of results to include in the operation output. This input member
        MUST NOT be required and MUST target an integer shape.

        .. warning::

            Do not attempt to fill response pages to meet the value provided
            for the ``pageSize`` member of a paginated operation. Attempting to
            match a target number of elements results in an unbounded API with
            an unpredictable latency.

In the example below, a resource defines a paginated operation.

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        @collection @readonly
        @paginated(inputToken: nextToken, outputToken: nextToken,
                  pageSize: maxResults, items: foos)
        operation GetFoos(GetFoosInput) -> GetFoosOutput

        structure GetFoosInput {
          maxResults: Integer,
          nextToken: String
        }

        structure GetFoosOutput {
          nextToken: String,
          @required
          foos: StringList,
        }

        list StringList {
          member: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetFoos": {
                        "type": "operation",
                        "input" :"GetFoosInput",
                        "output": "GetFoosOutput",
                        "readonly": true,
                        "collection": true,
                        "paginated": {
                            "inputToken": "nextToken",
                            "outputToken": "nextToken",
                            "pageSize": "maxResults",
                            "items": "foos"
                        }
                    },
                    "GetFoosInput": {
                        "type": "structure",
                        "members": {
                            "maxResults": {
                                "target": "Integer"
                            }
                            "nextToken": {
                                "target": "String"
                            }
                        }
                    },
                    "GetFoosOutput": {
                        "type": "structure",
                        "members": {
                            "nextToken": {
                                "target": "String"
                            },
                            "foos": {
                                "target": "StringList",
                                "required": true
                            }
                        }
                    },
                    "StringList": {
                        "type": "list",
                        "member": {
                            "target": "String"
                        }
                    }
                }
            }
        }

Pagination Behavior
```````````````````

#. If an operation returns a naturally size-limited subset of data
   (e.g., a top-ten list of users sorted by rank), then the operation
   SHOULD NOT be paginated.

#. Only one list or map per operation can be paginated.

#. Paginated responses MUST NOT return the same item of a paginated result
   set more than once (i.e., a paginated result set is a disjoint union of the
   subsets partitioned by the referenced ``pageSize`` input member and the SLA
   defined by the service).

#. If a paginated request returns data in a sorted order that is not an
   immutable strict total ordering of items, then the paginated request MUST
   provide a temporally static view of the underlying data that does not
   modify the order topology during pagination. For example, a game’s
   leaderboard of top-scoring players cannot have players move from position
   #10 to position #12 during pagination, the last player on page N has to
   have a higher score than the first player on page N+1, no players that
   exist when pagination begins are to be skipped, and players MUST NOT be
   repeated due to moves in the underlying data.

#. If pagination is ordered and newly created resources are returned, then
   newly created resources MUST appear in order on the appropriate page.


Client behavior
```````````````

Smithy clients SHOULD provide abstractions that can be used to automatically
iterate over paginated responses. The following steps describe the process a
client MUST follow when iterating over paginated API calls:

#. Send the initial request to a paginated operation.

#. If the received response does not contain a continuation token in the
   referenced ``outputToken`` member, then there are no more results to
   retrieve and the process is complete.

#. If there is a continuation token in the referenced ``outputToken`` member
   of the response, then the client sends a subsequent request using the same
   input parameters as the original call, but including the last received
   continuation token. Clients are free to change the designated ``pageSize``
   input parameter at this step as needed.

#. If a client receives an identical continuation token from a service in back
   to back calls, then the client MAY choose to stop sending requests. This
   scenario implies a "tail" style API operation where clients are running in
   an infinite loop to send requests to a service in order to retrieve results
   as they are available.

#. Return to step 2.


Continuation tokens
```````````````````

The ``paginated`` trait indicates that an operation utilizes cursor-based
pagination. When a paginated operation truncates its output, it MUST return a
continuation token in the operation output that can be used to get the next
page of results. This token can then be provided along with the original input
to request additional results from the operation.

#. **Continuation tokens SHOULD be opaque.**

   Plain text continuation tokens inappropriately expose implementation details
   to the client, resulting in consumers building systems that manually
   construct continuation tokens. Making backwards compatible changes to a
   plain text continuation token format is extremely hard to manage.

#. **Continuation tokens SHOULD be versioned.**

   The parameters and context needed to paginate an API call can evolve over
   time. To future-proof these APIs, services SHOULD include some kind of
   version identifier in their continuation tokens. Once the version identifier
   of a token is recognized, a service will then know the appropriate operation
   for decoding and returning the next response for a paginated request.

#. **Continuation tokens SHOULD expire after a period of time.**

   Continuation tokens SHOULD expire after a short period of time (e.g., 24
   hours is a reasonable default for many services). This allows services
   to quickly phase out deprecated continuation token formats, and helps to set
   the expectation that continuation tokens are ephemeral and MUST NOT be used
   after extended periods of time. Services MUST reject a request with a client
   error when a client uses an expired continuation token.

#. **Continuation tokens MUST be bound to a fixed set of filtering parameters.**

   Services MUST reject a request that changes filtering input parameters while
   paging through responses. Services MUST require clients to send the same
   filtering request parameters used in the initial pagination request to all
   subsequent pagination requests.

   :dfn:`Filtering parameters` are defined as parameters that remove certain
   elements from appearing in the result set of a paginated API call. Filtering
   parameters do not influence the presentation of results (e.g., the
   designated ``pageSize`` input parameter partitions a result set into smaller
   subsets but does not change the sum of the parts). Services MUST allow
   clients to change presentation based parameters while paginating through a
   result set.

#. **Continuation tokens MUST NOT influence authorization.**

   A service MUST NOT evaluate authorization differently depending on the
   presence, absence, or contents of a continuation token.


Resource traits
===============


.. _collection-trait:

``collection`` trait
--------------------

Summary
    Indicates that when an operation is bound to a resource, it MUST use a
    :ref:`collection operation binding <collection-operations>`.
Trait selector
    ``operation``
Value type
    Annotation trait

Applying this trait is required in order to bind an operation to a resource
using a collection binding, meaning that one or more of the identifiers of
the resource should not appear as required members of the operation's input.
For example, the list lifecycle operation of a resource MUST be marked with
this trait.


.. _references-trait:

``references`` trait
--------------------

Summary
    Defines the :ref:`resource` shapes that are referenced by a string shape
    or a structure shape and the members of the structure that provide values
    for the :ref:`identifiers <resource-identifiers>` of the resource.

    References provide the ability for tooling to *dereference* a resource
    reference at runtime. For example, if a client receives a response from a
    service that contains references, the client could provide functionality
    to resolve references by name, allowing the end-user to invoke operations
    on a specific referenced resource.
Trait selector
    ``:test(structure, string)``

    *Any structure or string*
Value type
    ``object``

The ``references`` trait is a map in which each key is the name of the
reference, and each value is an object that contains the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 23 67

    * - Property
      - Type
      - Description
    * - service
      - :ref:`shape-id`
      - **Required**. The shape ID of the service to which the resource is
        bound. As with the ``resource`` property, the provided shape ID is not
        required to be resolvable at build time.
    * - resource
      - :ref:`shape-id`
      - **Required**. The shape ID of the referenced resource.

        The provided shape ID is not required to be part of the model;
        references may refer to resources in other models without directly
        depending on the external package in which the resource is defined.
        The reference will not be resolvable at build time but MAY be resolvable
        at runtime if the tool has loaded more than one model.
    * - ids
      - Map<String, String>
      - Defines a mapping of each resource identifier name to a structure
        member name that provides its value. Each key in the map MUST refer
        to one of the identifier names in the identifiers property of the
        resource, and each value in the map MUST refer to a valid structure
        member name that targets a string shape.

        - This property MUST be omitted if the ``references`` trait is applied
          to a string shape.
        - This property MAY be omitted if the identifiers of the resource
          can be :ref:`mapped implicitly <implicit-ids>`.
    * - rel
      - String
      - Defines the semantics of the relationship. The ``rel`` property SHOULD
        contain a link relation as defined in :rfc:`5988#section-4` (i.e.,
        this value SHOULD contain either a `standard link relation`_ or URI).

References MAY NOT be resolvable at runtime in the following circumstances:

#. The members that make up the ``ids`` are not present in a structure at
   runtime (e.g., a member is not marked as :ref:`required-trait`)
#. The targeted resource and/or service shape is not part of the model
#. The reference is bound to a specific service that is unknown to the tool

The following example defines several references:

.. tabs::

    .. code-tab:: smithy

        structure ForecastInformation {
          someId: SomeShapeIdentifier,
          @required forecastId: ForecastId,
          @required meteorologistId: MeteorologistId,
          @required otherData: SomeOtherShape,
          @required bucketName: BucketName,
          @required objectKey: ObjectKey,
        }
        apply ForecastInformation @references(
          forecast: {
            resource: Forecast,
            service: Weather,
          },
          optionalReference: {
            resource: ShapeName,
            service: Weather,
          },
          meteorologist: {
            resource: Meteorologist,
            service: Weather,
          },
          externalObject: {
            resource: "com.foo.baz#Object",
            service: "com.foo.baz#Service",
            ids: {
              bucket: bucketName,
              object: objectKey,
            },
          }
        )


.. _implicit-ids:

Implicit ids
````````````

The "ids" property of a reference MAY be omitted in any of the following
conditions:

1. The shape that the references trait is applied to is a string shape.
2. The shape that the references trait is applied to is a structure shape
   and all of the identifier names of the resource have corresponding member
   names that target string shapes.


.. _resourceIdentifier-trait:

``resourceIdentifier`` trait
----------------------------

Summary
    Indicates that the targeted structure member provides an identifier for a
    resource.
Trait selector
    ``:test(member:of(structure)[trait|required] > string)``

    *Any required member of a structure that targets a string*
Value type
    ``string`` value

The ``resourceIdentifier`` trait may only be used on members of structures that
serve as input shapes for operations bound to resources. The string value
provided must correspond to the name of an identifier for said resource. The
trait is not required when the name of the input structure member is an exact
match for the name of the resource identifier.

.. tabs::

    .. code-tab:: smithy

        resource File {
          identifiers: {
            directory: "String",
            fileName: "String",
          },
          read: GetFile,
        }

        @readonly
        operation GetFile(GetFileInput) -> GetFileOutput errors [NoSuchResource]

        structure GetFileInput {
          @required
          directory: String,

          // resourceIdentifier is used because the input member name
          // does not match the resource identifier name
          @resourceIdentifier(fileName)
          @required
          name: String,
        }


Protocol traits
===============

Serialization and protocols traits define how data is transferred over
the wire.


.. _protocols-trait:

``protocols`` trait
-------------------

Summary
    Defines the protocols supported by a service.
Trait selector
    ``service``
Value type
    ``array`` of protocol ``object``

Smithy is protocol agnostic, which means it focuses on the interfaces and
abstractions that are provided to end-users rather than how the data is sent
over the wire. In Smithy, a *protocol* is a named set of rules that defines
the syntax and semantics of how a client and server communicate. A protocol
name defines both the application protocol of a service and the serialization
formats used in messages. These serialization formats MAY be influenced by
payload serialization traits like :ref:`jsonName-trait` and
:ref:`xmlAttribute-trait`.

The ``protocols`` trait of a service defines a priority ordered list of
protocols supported by the service and the authentication schemes that each
protocol supports. A client MUST understand at least one of the protocols in
order to successfully communicate with the service.

The value of the ``protocols`` trait is an array of protocol objects. Each
protocol object supports the following key-value pairs:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - name
      - ``string``
      - **Required**. The name of the protocol. The name MUST be unique across
        the entire list and MUST match the following regular expression:
        ``^[a-z][a-z0-9\-.+]*$``.
    * - auth
      - ``List<string>``
      - A priority ordered list of authentication schemes supported by this
        protocol. Each value MUST match the following regular expression:
        ``^[a-z][a-z0-9\-.+]*$``.
    * - tags
      - ``List<string>``
      - Attaches a list of tags that allow the protocol to be categorized and
        grouped.

The following example defines a service that supports both the
"smithy.example" protocol and the "aws.mqtt" protocols. The
"aws.mqtt" protocol is defined with an optional list of tags.

.. tabs::

    .. code-tab:: smithy

        @protocols([
            {name: smithy.example, auth: [http-basic]},
            {name: aws.mqtt, auth: [x.509], tags: [internal]}])
        service WeatherService {
          version: "2017-02-11",
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "WeatherService": {
                        "type": "service",
                        "version": "2017-02-11",
                        "protocols": [
                            {"name": "smithy.example", "auth": ["http-basic"]},
                            {"name": "aws.mqtt", "auth": ["x.509"], "tags": ["internal"]}
                        ]
                    }
                }
            }
        }

An operation bound to a service is expected to support every listed ``auth``
scheme of every protocol unless the operation or the service is annotated
with the :ref:`auth-trait`.


Built-in auth schemes
`````````````````````

Smithy defines the following built-in authentication schemes that can be
used with the ``protocols`` trait.

.. list-table::
    :header-rows: 1
    :widths: 20 80

    * - Scheme
      - Description
    * - http-basic
      - HTTP Basic Authentication as defined in :rfc:`2617`.
    * - http-digest
      - HTTP Digest Authentication as defined in :rfc:`2617`.
    * - http-bearer
      - HTTP Bearer Authentication as defined in :rfc:`6750`.
    * - http-x-api-key
      - An HTTP-specific authentication scheme that sends an arbitrary
        API key in the "X-Api-Key" HTTP header.
    * - none
      - Indicates that the API can be called without authentication.

Custom authentication schemes MAY be referenced in Smithy models and
SHOULD utilize some kind of prefix to prevent collisions with other
custom schemes (for example, "aws.v4" is preferred over just "v4").


.. _auth-trait:

``auth`` trait
--------------

Summary
    Defines the priority ordered authentication schemes supported by a service
    or operation. When applied to a service, it defines the default
    authentication schemes of every operation in the service. When applied
    to an operation, it defines the list of all authentication schemes
    supported by the operation, overriding any ``auth`` trait specified
    on a service.
Trait selector
    ``:test(service, operation)``

    *Service or operation shapes*
Value type
    This trait contains a priority ordered list of string values that
    reference authentication schemes defined on a service shape.

The ``auth`` trait is used to explicitly define which authentication schemes
defined by the :ref:`protocols-trait` of a service are supported by an
operation.

Operations that are not annotated with the ``auth`` trait inherit the ``auth``
trait of the service they are bound to, and if the service is not annotated
with the ``auth`` trait, then the operation is expected to support each of
the authentication schemes defined by all of the protocols of the service.
Each entry in the ``auth`` trait MUST map to a corresponding ``auth``
property in the ``protocols`` trait of a service with the exception of the
"none" auth scheme which can be used without defining it in a protocol.

When connecting to a service using a specific protocol, only the intersection
of the ``auth`` schemes listed in the selected protocol and the ``auth``
schemes defined in the resolved ``auth`` trait are eligible for the client
to use.

The following example defines two operations:

* OperationA defines an explicit list of the authentication schemes it
  supports using the ``auth`` trait.
* OperationB does is not annotated with the ``auth`` trait, so the schemes
  supported by this operation inherit from the ``protocols`` trait defined
  on the service.

.. tabs::

    .. code-tab:: smithy

        @protocols([{name: smithy.example, auth: [http-basic, http-digest]}])
        // Every operation by default should support http-basic and http-digest.
        @auth([http-basic, http-digest])
        service AuthenticatedService {
            version: "2017-02-11",
            operations: [OperationA, OperationB]
        }

        // This operation is configured to either be unauthenticated
        // or to use http-basic. It is not expected to support http-digest.
        @auth([none, http-basic])
        operation OperationA()

        // This operation defines no auth, so it is expected to support the auth
        // defined on the service: http-basic and http-digest.
        operation OperationB()

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "AuthenticatedService": {
                        "type": "service",
                        "version": "2017-02-11",
                        "protocols": [{"name": "smithy.example", "auth": ["http-basic", "http-digest"]}],
                        "operations": ["OperationA", "OperationB"]
                    },
                    "OperationA": {
                        "type": "operation",
                        "auth": ["none", "http-basic"]
                    },
                    "OperationB": {
                        "type": "operation"
                    }
                }
            }
        }


The following ``auth`` trait is invalid because it uses an ``auth`` trait
scheme that is not supported by any of the ``protocols`` of the service:

.. code-block:: smithy

    @protocols([{name: smithy.example, auth: [http-basic]}])
    @auth([http-digest]) // <-- Invalid!
    service InvalidExample {
        version: "2017-02-11"
    }

The following example demonstrates how a service that supports multiple
protocols can define different authentication schemes for each protocol.

.. tabs::

    .. code-tab:: smithy

        @protocols([
            {name: example.foo, auth: [http-basic, http-digest]},
            {name: example.baz, auth: [x.509]}])
        @auth([http-basic, x.509])
        service AuthenticatedService {
            version: "2017-02-11",
            operations: [OperationA, OperationB]
        }

        @auth([http-digest, x.509])
        operation OperationA()

        operation OperationB()

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "AuthenticatedService": {
                        "type": "service",
                        "version": "2017-02-11",
                        "protocols": [
                            {"name": "example.foo", "auth": ["http-basic", "http-digest"]},
                            {"name": "example.baz", "auth": ["x.509"]},
                        ],
                        "operations": ["OperationA", "OperationB"]
                    },
                    "OperationA": {
                        "type": "operation",
                        "auth": ["http-digest", "x.509"]
                    },
                    "OperationB": {
                        "type": "operation"
                    }
                }
            }
        }

When connecting to the above service over the "example.foo" protocol:

* ``OperationA`` supports the "http-digest" authentication scheme.
* ``OperationB`` supports the "http-basic" and "http-digest" authentication
  schemes.

When connecting to the above service over the "example.baz" protocol,
both ``OperationA`` and ``OperationB`` support only the x.509 authentication
scheme.


.. _jsonName-trait:

``jsonName`` trait
------------------

Summary
    Allows a serialized object property name in a JSON document to differ from
    a structure member name used in the model.
Trait selector
    ``member:of(structure)``

    *Any structure member*
Value type
    ``string`` value

Given the following structure definition,

.. tabs::

    .. code-tab:: smithy

        structure MyStructure {
          @jsonName(Foo)
          foo: String,

          bar: String,
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "MyStructure": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "jsonName": "Foo"
                            },
                            "bar": {
                                "target": "String"
                            }
                        }
                    }
                }
            }
        }

and the following values provided for ``MyStructure``,

::

    "foo" = "abc"
    "bar" = "def"

the JSON representation of the value would be serialized with the
following document:

.. code-block:: json

    {
        "Foo": "abc",
        "bar": "def"
    }


.. _mediaType-trait:

``mediaType`` trait
-------------------

Summary
    Describes the contents of a blob or string shape using a media type as
    defined by :rfc:`6838` (e.g., "video/quicktime").
Trait selector
    ``:test(blob, string)``

    *Any blob or string*
Value type
    ``string`` value

The ``mediaType`` can be used in tools for documentation, validation,
automated conversion or encoding in code, automatically determining an
appropriate Content-Type for an HTTP-based protocol, etc.

The following example defines a video/quicktime blob:

.. tabs::

    .. code-tab:: smithy

        @mediaType("video/quicktime")
        blob VideoData


.. _timestampFormat-trait:

``timestampFormat`` trait
-------------------------

Summary
    Defines a custom timestamp serialization format.
Trait selector
    ``:test(timestamp, member > timestamp)``

    *timestamp or member that targets a timestamp*
Value type
    ``string`` value

The serialization format of a timestamp shape is normally dictated by the
:ref:`protocol <protocols-trait>` of a service. In order to interoperate with
other web services or frameworks, it is sometimes necessary to use a specific
serialization format that differs from the protocol.

Smithy defines the following built-in timestamp formats:

.. list-table::
    :header-rows: 1
    :widths: 20 80

    * - Format
      - Description
    * - date-time
      - Date time as defined by the ``date-time`` production in
        `RFC3339 section 5.6 <https://xml2rfc.tools.ietf.org/public/rfc/html/rfc3339.html#anchor14>`_
        with no UTC offset (for example, ``1985-04-12T23:20:50.52Z``).
    * - http-date
      - An HTTP date as defined by the ``IMF-fixdate`` production in
        :rfc:`7231#section-7.1.1.1` (for example,
        ``Tue, 29 Apr 2014 18:30:38 GMT``).
    * - epoch-seconds
      - Also known as Unix time, the number of seconds that have elapsed since
        00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970,
        with decimal precision (for example, ``1515531081.1234``).

.. important::

    This trait SHOULD NOT be used unless the intended serialization format of
    a timestmap differs from the default protocol format. Using this trait too
    liberally can cause other tooling to improperly interpret the timestamp.

See :ref:`timestamp-serialization-format` for information on how to
determine the serialization format of a timestamp.


.. _documentation-traits:

Documentation traits
====================

.. _documentation-trait:

``documentation`` trait
-----------------------

Summary
    Adds documentation to a shape or member. The format of the documentation
    trait MUST be a valid HTML fragment.
Trait selector
    ``*``
Value type
    ``string`` value

.. tabs::

    .. code-tab:: smithy

        @documentation("This <em>is</em> documentation about the shape.")
        string MyString


Effective documentation
```````````````````````

The *effecive documentation trait* of a shape is resolved using the following
process:

#. Use the ``documentation`` trait of the shape, if present.
#. If the shape is a :ref:`member`, then use the ``documentation`` trait of
   the shape targeted by the member, if present.

For example, given the following model,

.. tabs::

    .. code-tab:: smithy

        structure Foo {
            @documentation("Member documentation")
            baz: Baz,

            bar: Baz,

            qux: String,
        }

        @documentation("Shape documentation")
        string Baz

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "Foo": {
                        "type": "structure",
                        "members": {
                            "baz": {
                                "target": "Baz",
                                "documentation": "Member documentation"
                            },
                            "bar": {
                                "target": "Baz"
                            },
                            "qux": {
                                "target": "String"
                            }
                        }
                    },
                    "Baz": {
                        "type": "string",
                        "documentation": "Shape documentation"
                    }
                }
            }
        }

the effective documentation of ``Foo$baz`` resolves to "Member documentation",
``Foo$bar`` resolves to "Shape documentation", ``Foo$qux`` is not documented,
``Baz`` resolves to "Shape documentation", and ``Foo`` is not documented.


.. _examples-trait:

``examples`` trait
------------------

Summary
    Provides example inputs and outputs for operations.
Trait selector
    ``operation``
Value type
    ``array`` of :ref:`example objects <example-object>`


.. _example-object:

Example object
``````````````

Each ``example`` trait value is an object with the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - title
      - ``string``
      - **Required**. A short title that defines the example.
    * - documentation
      - ``string``
      - A longer description of the example.
    * - input
      - ``object``
      - Provides example input parameters for the operation. Each key is
        the name of a top-level input structure member, and each value is the
        value of the member.
    * - output
      - ``object``
      - Provides example output parameters for the operation. Each key is
        the name of a top-level output structure member, and each value is the
        value of the member.

The values provided for the ``input`` and ``output`` properties MUST be
compatible with the shapes and constraints of the corresponding structure.
These values use the same semantics and format as
:ref:`custom trait values <trait-definition-values>`.

.. tabs::

    .. code-tab:: smithy

        @readonly
        operation MyOperation(MyOperationInput) -> MyOperationOutput

        apply MyOperation @examples([
          {
            title: "Invoke MyOperation",
            input: {
              tags: [foo, baz, bar],
            },
            output: {
              status: PENDING,
            }
          },
          {
            title: "Another example for MyOperation",
            input: {
              foo: baz,
            },
            output: {
              status: PENDING,
            }
          },
        ])


.. _externalDocumentation-trait:

``externalDocumentation`` trait
-------------------------------

Summary
    Links a service or operation to a URL that contains additional
    documentation.
Trait selector
    ``:test(service, operation)``

    *Any service or operation*
Value type
    ``string`` value containing a valid URL.

.. tabs::

    .. code-tab:: smithy

        @externalDocumentation("https://www.example.com/")
        service MyService {
          version: "2006-03-01",
        }


.. _sensitive-trait:

``sensitive`` trait
-------------------

Summary
    Indicates that the data stored in the shape or member is sensitive
    and MUST be handled with care.
Trait selector
    ``:test(blob, string, member > :test(blob, string))``

    *Any blob or string; or a member that targets a blob/string*
Value type
    Annotation trait

Sensitive data MUST NOT be exposed in things like exception messages or log
output. Application of this trait SHOULD NOT affect wire logging
(i.e., logging of all data transmitted to and from servers or clients).

.. tabs::

    .. code-tab:: smithy

        @sensitive
        string MyString


.. _since-trait:

``since`` trait
---------------

Summary
    Defines the version or date in which a shape or member was added to
    the model.
Trait selector
    ``*``
Value type
    ``string`` value representing the date it was added.


.. _tags-trait:

``tags`` trait
--------------

Summary
    Tags a shape with arbitrary tag names that can be used to filter and group
    shapes in the model.
Trait selector
    ``*``
Value type
    ``array`` of ``string`` values

Tools can use these tags to filter shapes that should not be visible for a
particular consumer of a model. The string values that can be provided to the
tags trait are arbitrary and up to the model author.

.. tabs::

    .. code-tab:: smithy

        @tags(["experimental", "public"])
        string SomeStructure {}


.. _title-trait:

``title`` trait
---------------

Summary
    Defines a proper name for a service or resource shape. This title can be
    used in automatically generated documentation and other contexts to
    provide a user friendly name for services and resources.
Trait selector
    ``:test(service, resource)``

    *Any service or resource*
Value type
    ``string``

.. tabs::

    .. code-tab:: smithy

        namespace acme.example

        @title("ACME Simple Image Service")
        service MySimpleImageService {
          version: "2006-03-01",
        }


.. _endpoint-traits:

Endpoint Traits
===============

Smithy provides various endpoint binding traits that can be used to configure
request endpoints.

.. contents:: Table of contents
    :depth: 2
    :local:
    :backlinks: none


.. _endpoint-trait:

``endpoint`` trait
------------------

Summary
    Configures a custom operation endpoint.
Trait selector
    ``operation``
Value type
    ``object``

The ``endpoint`` trait is an object that contains the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 10 80

    * - Property
      - Type
      - Description
    * - hostPrefix
      - ``string``
      - **Required** The ``hostPrefix`` property defines a template that expands
        to a valid *host* as defined in :rfc:`3986#section-3.2.2`.
        ``hostPrefix`` MAY contain :ref:`label placeholders <endpoint-Labels>`
        that reference top-level input members of the operation marked with the
        :ref:`hostLabel-trait`. The ``hostPrefix`` MUST NOT contain a scheme,
        userinfo, or port.

The following example defines an operation that uses a custom endpoint:

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        @readonly
        @endpoint(hostPrefix: "{foo}.data.")
        operation GetStatus(GetStatusInput) -> GetStatusOutput

        structure GetStatusInput {
          @required
          @hostLabel
          foo: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetStatus": {
                        "type": "operation",
                        "input": "GetStatusInput",
                        "output": "GetStatusOutput",
                        "readonly": true,
                        "endpoint": {
                            "hostPrefix": "{foo}.data."
                        }
                    },
                    "GetStatusInput": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "required": true,
                                "hostLabel": true
                            }
                        }
                    }
                }
            }
        }


.. _endpoint-Labels:

Labels
``````

``hostPrefix`` patterns MAY contain label placeholders. :dfn:`Labels` consist
of label name characters surrounded by open and closed braces (for example,
"{label_name}" is a label and ``label_name`` is the label name). Every label
MUST correspond to a top-level operation input member, the input member MUST
be marked as :ref:`required <required-trait>`, the input member MUST have the
:ref:`hostLabel-trait`, and the input member MUST reference a string.

Given the following operation,

.. tabs::

    .. code-tab:: smithy

        @readonly
        @endpoint(hostPrefix: "{foo}.data.")
        operation GetStatus(GetStatusInput) -> GetStatusOutput

        structure GetStatusInput {
          @required
          @hostLabel
          foo: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetStatus": {
                        "type": "operation",
                        "input": "GetStatusInput",
                        "output": "GetStatusOutput",
                        "readonly": true,
                        "endpoint": {
                            "hostPrefix": "{foo}.data."
                        }
                    },
                    "GetStatusInput": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "required": true,
                                "hostLabel": true
                            }
                        }
                    }
                }
            }
        }

and the following value provided for ``GetStatusInput``,

::

    "foo" = "abc"

the expanded ``hostPrefix`` evaluates to ``abc.data.``.

Any number of labels can be included within a pattern, provided that they are
not immediately adjacent and do not have identical label names.

Given the following operation,

.. tabs::

    .. code-tab:: smithy

        @readonly
        @endpoint(hostPrefix: "{foo}-{bar}.data.")
        operation GetStatus(GetStatusInput) -> GetStatusOutput

        structure GetStatusInput {
          @required
          @hostLabel
          foo: String

          @required
          @hostLabel
          bar: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetStatus": {
                        "type": "operation",
                        "input": "GetStatusInput",
                        "output": "GetStatusOutput",
                        "readonly": true,
                        "endpoint": {
                            "hostPrefix": "{foo}-{bar}.data."
                        }
                    },
                    "GetStatusInput": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "required": true,
                                "hostLabel": true
                            },
                            "bar": {
                                "target": "String",
                                "required": true,
                                "hostLabel": true
                            }
                        }
                    }
                }
            }
        }

and the following values provided for ``GetStatusInput``,

::

    "foo" = "abc"
    "bar" = "def"

the expanded ``hostPrefix`` evaluates to ``abc-def.data.``.

Labels MUST NOT be adjacent in a ``hostPrefix``. The following operation is
invalid because the ``{foo}`` and ``{bar}`` labels are adjacent:

.. tabs::

    .. code-tab:: smithy

        @readonly
        @endpoint(hostPrefix: "{foo}{bar}.data.")
        operation GetStatus(GetStatusInput) -> GetStatusOutput

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetStatus": {
                        "type": "operation",
                        "input": "GetStatusInput",
                        "output": "GetStatusOutput",
                        "readonly": true,
                        "endpoint": {
                            "hostPrefix": "{foo}{bar}.data."
                        }
                    }
                }
            }
        }


.. _endpoint-ClientBehavior:

Client Behavior
```````````````

If an API operation is decorated with an endpoint trait, a client MUST expand
the ``hostPrefix`` template and prepend the expanded value to the client's
endpoint host prior to its use. Clients MUST fail when expanding a
``hostPrefix`` template if the value of any labeled member is empty or null.

After the ``hostPrefix`` template is expanded, a client MUST prepend the
expanded value to the client's derived endpoint host. The client MUST NOT add
any additional characters between the ``hostPrefix`` and client derived
endpoint host. The resolved host value MUST result in a valid `RFC 3986 Host`_.

Clients SHOULD provide a way for users to disable the ``hostPrefix`` injection
behavior. If a user sets this flag, the client MUST NOT perform any
``hostPrefix`` expansion and MUST NOT prepend the prefix to the client derived
host. The client MUST serialize members to any modeled target location
regardless of this flag.

The ``hostLabel`` trait MUST NOT affect the protocol-specific serialization
logic of a member.

Given the following operation,

.. tabs::

    .. code-tab:: smithy

        @readonly
        @endpoint(hostPrefix: "{foo}.data.")
        @http(method: GET, uri: "/status")
        operation GetStatus(GetStatusInput) -> GetStatusOutput

        structure GetStatusInput {
          @required
          @hostLabel
          @httpHeader(X-Foo)
          foo: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetStatus": {
                        "type": "operation",
                        "input": "GetStatusInput",
                        "output": "GetStatusOutput",
                        "readonly": true,
                        "endpoint": {
                            "hostPrefix": "{foo}.data."
                        },
                        "http": {
                            "method": "GET",
                            "uri": "/status"
                        }
                    },
                    "GetStatusInput": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "required": true,
                                "hostLabel": true,
                                "httpHeader": "X-Foo"
                            }
                        }
                    }
                }
            }
        }

and the following value provided for ``GetStatusInput``,

::

    "foo" = "abc"

the expanded ``hostPrefix`` evaluates to ``abc.data.`` AND the ``X-Foo`` HTTP
header will contain the value ``abc``.


.. _hostLabel-trait:

``hostLabel`` trait
-------------------

Summary
    Binds a top-level operation input structure member to a label in the
    hostPrefix of an endpoint trait.
Trait selector
    ``:test(member:of(structure)[trait|required] > string)``

    *Any required member of a structure that targets a string*
Value type
    Annotation trait

Operations marked with the :ref:`endpoint-trait` MAY contain labels in the
``hostPrefix`` property. These labels reference top-level operation input
structure members that MUST be annotated with the ``hostLabel`` trait. Any
``hostLabel`` trait applied to a member that is not a top-level input member
to an operation marked with the :ref:`endpoint-trait` will be ignored.

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        @readonly
        @endpoint(hostPrefix: "{foo}.data")
        operation GetStatus(GetStatusInput) -> GetStatusOutput

        structure GetStatusInput {
          @required
          @hostLabel
          foo: String
        }

    .. code-tab:: json

        {
            "smithy": "0.1.0",
            "smithy.example": {
                "shapes": {
                    "GetStatus": {
                        "type": "operation",
                        "input": "GetStatusInput",
                        "output": "GetStatusOutput",
                        "readonly": true,
                        "endpoint": {
                            "hostPrefix": "{foo}.data."
                        }
                    },
                    "GetStatusInput": {
                        "type": "structure",
                        "members": {
                            "foo": {
                                "target": "String",
                                "required": true,
                                "hostLabel": true
                            }
                        }
                    }
                }
            }
        }


.. _merging-models:

--------------
Merging models
--------------

Smithy models MAY be divided into multiple files so that they are easier to
maintain and evolve. Smithy tools MUST take the following steps to merge two
models together to form a composite model:

#. Assert that both models use a :ref:`version <smithy-version>` that is
   compatible with the tool. The version is specificed
#. If both models define the same :ref:`namespace <namespaces>`, merge the
   namespaces.

   - Duplicate :ref:`trait definitions <trait-definition>` if found, MUST
     cause the model merge to fail.
   - Duplicate shape names, if found, MUST cause the model merge to fail.
   - Merge any conflicting :ref:`trait <traits>` definitions using
     :ref:`trait conflict resolution  <trait-conflict-resolution>`.
#. Merge the :ref:`metadata <metadata>` properties of both models using the
   :ref:`metadata merge rules <merging-metadata>`.


.. _merging-metadata:

Merging metadata
================

Top-level metadata key-value pairs are merged using the following logic:

1. If a metadata key is only present in one model, then the entry is valid
   and added to the merged model.
2. If both models contain the same key and both values are arrays, then
   the entry is valid; the values of both arrays are concatenated into a
   single array and added to the merged model.
3. If both models contain the same key and both values are exactly equal,
   then the conflict is ignored and the value is added to the merged model.
4. If both models contain the same key and the values do not both map to
   arrays, then the key is invalid and there is a metadata conflict error.

Given the following two Smithy models:

.. code-block:: smithy
    :caption: model-a.smithy

    metadata "foo" = ["baz", "bar"]
    metadata "qux" = "test"
    metadata "validConflict" = "hi!"

.. code-block:: smithy
    :caption: model-b.smithy

    metadata "foo" = ["lorem", "ipsum"]
    metadata "lorem" = "ipsum"
    metadata "validConflict" = "hi!"

Merging ``model-a.smithy`` and ``model-b.smithy`` produces the following
model:

.. code-block:: smithy

    metadata "foo" = ["baz", "bar", "lorem", "ipsum"]
    metadata "qux" = "test"
    metadata "lorem" = "ipsum"
    metadata "validConflict" = "hi!"


.. _tagged union data structure: https://en.wikipedia.org/wiki/Tagged_union
.. _ABNF: https://tools.ietf.org/html/rfc5234
.. _AWS signature version 4: http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html
.. _UUID: https://tools.ietf.org/html/rfc4122
.. _standard link relation: https://www.iana.org/assignments/link-relations/link-relations.xhtml
.. _tz database: https://en.wikipedia.org/wiki/Tz_database
.. _ISO 8601 duration: https://en.wikipedia.org/wiki/ISO_8601#Durations
.. _Option type: https://doc.rust-lang.org/std/option/enum.Option.html
.. _ECMA 262 regular expression dialect: https://www.ecma-international.org/ecma-262/8.0/index.html#sec-patterns
.. _RFC 3986 Host: https://tools.ietf.org/html/rfc3986#section-3.2.2
