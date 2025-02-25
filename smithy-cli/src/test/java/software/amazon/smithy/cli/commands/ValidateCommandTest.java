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

package software.amazon.smithy.cli.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.cli.CliError;
import software.amazon.smithy.cli.SmithyCli;

public class ValidateCommandTest {
    @Test
    public void hasValidateCommand() throws Exception {
        PrintStream out = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);
        SmithyCli.create().run("validate", "--help");
        System.setOut(out);
        String help = outputStream.toString("UTF-8");

        assertThat(help, containsString("Validates"));
    }

    @Test
    public void usesModelDiscoveryWithCustomValidClasspath() {
        String dir = getClass().getResource("valid.jar").getPath();
        SmithyCli.create().configureLogging(true).run("validate", "--debug", "--discover-classpath", dir);
    }

    @Test
    public void usesModelDiscoveryWithCustomInvalidClasspath() {
        CliError e = Assertions.assertThrows(CliError.class, () -> {
            String dir = getClass().getResource("invalid.jar").getPath();
            SmithyCli.create().configureLogging(true).run("validate", "--debug", "--discover-classpath", dir);
        });

        assertThat(e.getMessage(), containsString("1 ERROR(s)"));
    }

    @Test
    public void failsOnUnknownTrait() {
        CliError e = Assertions.assertThrows(CliError.class, () -> {
            String model = getClass().getResource("unknown-trait.smithy").getPath();
            SmithyCli.create().configureLogging(true).run("validate", model);
        });

        assertThat(e.getMessage(), containsString("1 ERROR(s)"));
    }

    @Test
    public void allowsUnknownTrait() {
        String model = getClass().getResource("unknown-trait.smithy").getPath();
        SmithyCli.create().configureLogging(true).run("validate", "--allow-unknown-traits", model);
    }
}
