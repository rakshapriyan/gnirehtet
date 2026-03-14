/*
 * Copyright (C) 2017 Genymobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.genymobile.gnirehtet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandLineArgumentsTest {

    private static final int ACCEPT_ALL = CommandLineArguments.PARAM_SERIAL | CommandLineArguments.PARAM_DNS_SERVER
            | CommandLineArguments.PARAM_ROUTES;

    @Test
    public void testNoArgs() {
        CommandLineArguments args = CommandLineArguments.parse(ACCEPT_ALL);
        Assertions.assertNull(args.getSerial());
        Assertions.assertNull(args.getDnsServers());
    }

    @Test
    public void testSerialOnly() {
        CommandLineArguments args = CommandLineArguments.parse(ACCEPT_ALL, "myserial");
        Assertions.assertEquals("myserial", args.getSerial());
        Assertions.assertNull(args.getDnsServers());
    }

    @Test
    public void testInvalidParameter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CommandLineArguments.parse(ACCEPT_ALL, "myserial", "other");
        });
    }

    @Test
    public void testDnsServersOnly() {
        CommandLineArguments args = CommandLineArguments.parse(ACCEPT_ALL, "-d", "8.8.8.8");
        Assertions.assertNull(args.getSerial());
        Assertions.assertEquals("8.8.8.8", args.getDnsServers());
    }

    @Test
    public void testSerialAndDnsServers() {
        CommandLineArguments args = CommandLineArguments.parse(ACCEPT_ALL, "myserial", "-d", "8.8.8.8");
        Assertions.assertEquals("myserial", args.getSerial());
        Assertions.assertEquals("8.8.8.8", args.getDnsServers());
    }

    @Test
    public void testDnsServersAndSerial() {
        CommandLineArguments args = CommandLineArguments.parse(ACCEPT_ALL, "-d", "8.8.8.8", "myserial");
        Assertions.assertEquals("myserial", args.getSerial());
        Assertions.assertEquals("8.8.8.8", args.getDnsServers());
    }

    @Test
    public void testSerialWithNoDnsServersParameter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CommandLineArguments.parse(ACCEPT_ALL, "myserial", "-d");
        });
    }

    @Test
    public void testNoDnsServersParameter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CommandLineArguments.parse(ACCEPT_ALL, "-d");
        });
    }

    @Test
    public void testRoutesParameter() {
        CommandLineArguments args = CommandLineArguments.parse(ACCEPT_ALL, "-r", "1.2.3.0/24");
        Assertions.assertEquals("1.2.3.0/24", args.getRoutes());
    }

    @Test
    public void testNoRoutesParameter() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CommandLineArguments.parse(ACCEPT_ALL, "-r");
        });
    }
}