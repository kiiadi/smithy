=================
Using SmithyBuild
=================

Building a Smithy model requires constructing a configuration file,
``smithy-build.json``. This file is used to describe how a model is created
and what projections of the model to create.


.. _smithy-build-json:

Using ``smithy-build.json``
===========================

The ``smithy-build.json`` file is used to describe how a model is created and
what projections of the model to create.

The configuration file accepts the following properties:

.. list-table::
    :header-rows: 1
    :widths: 10 20 70

    * - Property
      - Type
      - Description
    * - version
      - ``string``
      - **Required.** Defines the version of SmithyBuild. Set to `1.0`.
    * - outputDirectory
      - ``string``
      - **Required.** The location where projections are written. Each
        projection will create a subdirectory named after the projection, and
        the artifacts from the projection, including a ``model.json`` file,
        will be placed in the directory.
    * - imports
      - ``string[]``
      - Provides a list of relative imports to combine into a single model.
        When a directory is encountered, all files and all files within all
        subdirectories are imported. Note that build systems MAY choose to rely
        on other mechanisms for importing models and forming a composite model.
        These imports are used in every projection. Note: imports are relative
        to the configuration file.
    * - projections
      - ``map<string, object>``
      - A map of projection names to projection configurations.
    * - plugins
      - ``map<string, object>``
      - Defines the plugins to apply to the model when building every
        projection. Plugins are a mapping of plugin names to an arbitrary
        plugin configuration object.

The following is an example ``smithy-build.json`` configuration:

.. tabs::

    .. code-tab:: json

        {
            "version": "1.0",
            "outputDirectory": "build/output",
            "imports": ["foo.json", "some/directory"],
            "projections": {
                "my-abstract-projection": {
                    "abstract": true
                },
                "projection-name": {
                    "imports": ["projection-specific-imports/"],
                    "transforms": [
                        {"name": "excludeShapesByTag", "arguments": ["internal", "beta", "..."]},
                        {"name": "excludeTraitsByTag", "arguments": ["internal"]}
                    ],
                    "plugins": {
                        "plugin-name": {
                            "plugin-config": "value"
                        },
                        "...": {}
                    }
                }
            },
            "plugins": {
                "plugin-name": {
                    "plugin-config": "value"
                },
                "...": {}
            }
        }


.. _projections:

Projections
===========

A projection of a model is a filtered and modified version of a Smithy model
that is intended for specific audiences or customers. Projections are
useful to companies that maintain internal and external versions of an API
or include parameters and operations that are available to only a subset of
their customers.

Projections are defined in the smithy-build.json file in the ``projections``
property. Projection names MUST match the following pattern: ``^[A-Za-z0-9\-_.]+$``.

A projection accepts the following configuration:

.. list-table::
    :header-rows: 1
    :widths: 10 20 70

    * - Property
      - Type
      - Description
    * - abstract
      - ``boolean``
      - Defines the projection as a placeholder that other projections apply.
        Smithy will not build artifacts for abstract projections. Abstract
        projections must not define ``imports`` or ``plugins``.
    * - imports
      - ``string[]``
      - Provides a list of relative imports to include when building this
        specific projection. When a directory is encountered, all files and
        all files within all subdirectories are imported. Note: imports are
        relative to the configuration file.
    * - transforms
      - ``list<Transforms>``
      - Defines the transformations to apply to the projection.
        Transformations are used to remove shapes, remove traits, modify trait
        contents, and any other kind of transformation necessary for the
        projection. Transforms are applied in the order defined.
    * - plugins
      - ``map<string, object>``
      - Defines the plugins to apply to the model when building this
        projection. Plugins are a mapping of plugin names to an arbitrary
        plugin configuration object. SmithyBuild will attempt to resolve
        plugin names using `Java SPI`_ to locate and instance of ``software.amazon.smithy.build.SmithyBuildPlugin``
        that returns a matching name when calling ``getName``. SmithyBuild will
        emit a warning when a plugin cannot be resolved.


.. _projection-artifacts:

Projection artifacts
--------------------

SmithyBuild will write artifacts for each projection inside of
`outputDirectory`.

* The model that is projected is placed inside of ``${outputDirectory}/${projectionName}/model/model.json``.
* Build information about the projection build result, including the
  configuration of the projection and the validation events encountered when
  validating the projected model, are written to ``${outputDirectory}/${projectionName}/build-info/smithy-build-info.json``.
* All plugin artifacts are written to ``${outputDirectory}/${projectionName}/${pluginName}/${artifactName}``,
  where ``${artifactName}`` is the name of an artifact contributed by an
  instance of ``software.amazon.smithy.build.SmithyBuildPlugin``. The relative
  path of each artifact is resolved against ``${outputDirectory}/${projectionName}/${pluginName}/``.
  For example, given an artifact path of ``foo/baz.json``, the resolved path
  would become ``${outputDirectory}/${projectionName}/${pluginName}/foo/baz.json``.


.. _transforms:

Transforms
==========

Transforms are used to filter and modify the model for the projection.
Transforms are applied to the model, in order.

A transform accepts the following configuration:


.. list-table::
    :header-rows: 1
    :widths: 10 20 70

    * - Property
      - Type
      - Description
    * - name
      - ``string``
      - The required name of the transform.
    * - arguments
      - ``string[]``
      - Provides a list of arguments to pass to the transform.


.. _apply-transform:

apply
-----

Applies the transforms defined in the given projection names. Each provided
name must be a valid projection name. The transforms of the referenced
projections are applied in the order provided. No cycles are allowed in
``apply``.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "my-abstract-projection": {
              "abstract": true,
              "transforms": [
                {"name": "foo"}
              ]
            },
            "projection-name": {
              "imports": ["projection-specific-imports/"],
              "transforms": [
                {"name": "baz"},
                {"name": "apply", "arguments": ["my-abstract-projection"]},
                {"name": "bar"}
              ]
            }
          }
        }


.. excludeShapesByTag-transform:

excludeShapesByTag
------------------

Aliases: ``excludeByTag`` (deprecated)

Removes shapes if they are tagged with one or more of the given arguments via
the :ref:`tags trait <tags-trait>`.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "excludeByTag", "arguments": ["foo", "baz"]}
              ]
            }
          }
        }


.. _includeShapesByTag-transform:

includeShapesByTag
------------------

Aliases: ``includeByTag`` (deprecated)

Removes shapes that are not tagged with at least one of the given arguments
via the :ref:`tags trait <tags-trait>`.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeByTag", "arguments": ["foo", "baz"]}
              ]
            }
          }
        }


.. _includeNamespaces-transform:

includeNamespaces
-----------------

Filters out shapes that are not part of one of the given :ref:`namespaces <namespaces>`.
Note that this does not filter out traits based on namespaces.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeNamespaces", "arguments": ["com.foo.bar", "my.api"]}
              ]
            }
          }
        }


.. _includeServices-transform:

includeServices
---------------

Filters out service shapes that are not included in the arguments list of
service shape IDs.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeServices", "arguments": ["my.api#MyService"]}
              ]
            }
          }
        }


.. _excludeTags-transform:

excludeTags
-----------

Removes tags from shapes and trait definitions that match any of the
provided arguments (a list of allowed tags).


.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "excludeTags", "arguments": ["tagA", "tagB"]}
              ]
            }
          }
        }


.. _excludeTraits-transform:

excludeTraits
-------------

Removes trait definitions from a model if the trait name is present in the
provided list of arguments. Any instance of a removed trait is also removed
from shapes in the model.

The shapes that make up trait definitions that are removed *are not*
automatically removed from the model. Use ``removeUnusedShapes`` to remove
orphaned shapes.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "excludeTraits", "arguments": ["since", "com.foo#customTrait"]}
              ]
            }
          }
        }


You can exclude all of the traits in a namespace by ending one of the
arguments with "#". For example, the following configuration excludes
all traits in the "example.foo" namespace:

.. tabs::

    .. code-tab:: json
    
        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "excludeTraits", "arguments": ["example.foo#"]}
              ]
            }
          }
        }


.. _excludeTraitsByTag-transform:

excludeTraitsByTag
------------------

Removes trait definitions from a model if the trait definition has any of
the provided :ref:`tags <tags-trait>`. Any instance of a removed trait is
also removed from shapes in the model.

The shapes that make up trait definitions that are removed *are not*
automatically removed from the model. Use ``removeUnusedShapes`` to remove
orphaned shapes.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "excludeTraitsByTag", "arguments": ["internal"]}
              ]
            }
          }
        }


.. _includeAuth-transform:

includeAuth
-----------

Removes authentication schemes from shapes that do not match one of the
given arguments (a list of authentication schemes).

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeAuth", "arguments": ["aws.v4", "http-basic"]}
              ]
            }
          }
        }


.. _includeEndpoints-transform:

includeEndpoints
----------------

Removes endpoints from endpoints traits that do not have one of the
allowed names.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeEndpoints", "arguments": ["us-east-1", "us-west-2"]}
              ]
            }
          }
        }


.. _includeProtocols-transform:

includeProtocols
----------------

Removes protocols from service shapes that do not match one of the given
arguments (a list of protocol names).

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeProtocols", "arguments": ["aws.rest-json"]}
              ]
            }
          }
        }


.. _includeTags-transform:

includeTags
-----------

Removes tags from shapes and trait definitions that are not in the
argument list (a list of allowed tags).

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeTags", "arguments": ["foo", "baz"]}
              ]
            }
          }
        }


.. _includeTraits-transform:

includeTraits
-------------

Removes trait definitions from a model if the trait name is not present in the
provided list of arguments. Any instance of a removed trait is also removed
from shapes in the model.

The shapes that make up trait definitions that are removed *are not*
automatically removed from the model. Use ``removeUnusedShapes`` to remove
orphaned shapes.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeTraits", "arguments": ["sensitive", "com.foo.baz#customTrait"]}
              ]
            }
          }
        }


You can include all of the traits in a namespace by ending one of the
arguments with "#". For example, the following configuration includes
all traits in the "smithy.api" namespace:

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeTraits", "arguments": ["smithy.api#"]}
              ]
            }
          }
        }


.. _includeTraitsByTag-transform:

includeTraitsByTag
------------------

Removes trait definitions from a model if the trait definition does not
contain one of the provided :ref:`tags <tags-trait>`. Any instance of a
removed trait definition is also removed from shapes in the model.

The shapes that make up trait definitions that are removed *are not*
automatically removed from the model. Use ``removeUnusedShapes`` to remove
orphaned shapes.

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "includeTraitsByTag", "arguments": ["public"]}
              ]
            }
          }
        }


.. _removeUnusedShapes-transform:

removeUnusedShapes
------------------

Aliases: ``treeShaker`` (deprecated)

Removes shapes from the model that are not connected to any service shape.

You can *export* shapes that are not connected to any service shape by
applying specific tags to the shape and adding the list of export tags as
arguments to the transform.

The following example removes shapes that are not connected to any service,
but keeps the shape if it has any of the provided tags:

.. tabs::

    .. code-tab:: json

        {
          "version": "1.0",
          "projections": {
            "exampleProjection": {
              "transforms": [
                {"name": "removeUnusedShapes", "arguments": ["export-tag1", "another-export-tag"]}
              ]
            }
          }
        }


.. _plugins:

Plugins
=======

Plugins are defined in either the top-level "plugins" key-value pair of the
configuration file, or in the "plugins" key-value pair of a projection.
Plugins defined at the top-level of the configuration file are applied to
every projection. Projections that define plugins of the same name as a
top-level plugin completely overwrite the top-level plugin for that projection;
projection settings are not merged in any way.

Plugin names MUST match the following pattern: ``^[A-Za-z0-9\-_.]+$``.

SmithyBuild will attempt to resolve plugin names using `Java SPI`_
to locate and instance of ``software.amazon.smithy.build.SmithyBuildPlugin``
that matches the given plugin name (via ``matchesPluginName``). SmithyBuild
will log a warning when a plugin cannot be resolved.

SmithyBuild DOES NOT attempt to automatically download and install plugins.
Plugins MUST be available in the Java class path or module path in order for
them to be discovered.

The ``model``, ``build-info``, and ``sources`` plugins are plugins that are
always run in every non-abstract projection.


.. _model-plugin:

model plugin
------------

The ``model`` plugin serializes a self-contained and filtered version of the
model as a single file. All of the dependencies of the model are included
in the file.


.. _build-info-plugin:

build-info plugin
-----------------

The ``build-info`` plugin produces a JSON document that contains information
about the projection and model.


.. _sources-plugin:

sources plugin
--------------

The ``sources`` plugin copies the source models and creates a manifest.
When building the ``source`` projection, the models that were used to build the
model are copied over literally. When a JAR is used as a source model, the
Smithy models contained within the JAR are copied as a source model while the
JAR itself is not copied.

When applying a projection, a new model file is created that contains only
the shapes, trait definitions, and metadata that were defined in a source
model *and* all of the newly added shapes, traits, and metadata.

The manifest file is a newline (``\n``) separated file that contains the
relative path from the manifest file to each model file created by the
sources plugin. Lines that start with a number sign (#) are comments and are
ignored. A Smithy manifest file is stored in a JAR as ``META-INF/smithy/manifest``.
All model names referenced by the manifest are relative to ``META-INF/smithy/``.

.. _Java SPI: https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html
