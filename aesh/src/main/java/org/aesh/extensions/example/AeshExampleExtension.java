/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aesh.extensions.example;

import java.io.IOException;

import org.aesh.command.impl.registry.AeshCommandRegistryBuilder;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.command.registry.CommandRegistryException;
import org.aesh.command.settings.Settings;
import org.aesh.command.settings.SettingsBuilder;
import org.aesh.console.ReadlineConsole;
import org.aesh.extensions.cat.Cat;
import org.aesh.extensions.cd.Cd;
import org.aesh.extensions.clear.Clear;
import org.aesh.extensions.echo.Echo;
import org.aesh.extensions.exit.Exit;
import org.aesh.extensions.grep.Grep;
import org.aesh.extensions.harlem.Harlem;
import org.aesh.extensions.less.Less;
import org.aesh.extensions.ls.Ls;
import org.aesh.extensions.matrix.Matrix;
import org.aesh.extensions.mkdir.Mkdir;
import org.aesh.extensions.more.More;
import org.aesh.extensions.pushdpopd.Popd;
import org.aesh.extensions.pushdpopd.Pushd;
import org.aesh.extensions.pwd.Pwd;
import org.aesh.extensions.rm.Rm;
import org.aesh.extensions.touch.Touch;
import org.aesh.readline.Prompt;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class AeshExampleExtension {

    public static void main(String[] args) throws IOException, CommandRegistryException {

       CommandRegistry registry = AeshCommandRegistryBuilder.builder()
                .command(Exit.class)
                .command(Less.class)
                .command(More.class)
                .command(Harlem.class)
                .command(Clear.class)
                .command(Matrix.class)
                .command(Ls.class)
                .command(Grep.class)
                .command(Cat.class)
                .command(Cd.class)
                .command(Pwd.class)
                .command(Touch.class)
                .command(Pushd.class)
                .command(Popd.class)
                .command(Mkdir.class)
                .command(Echo.class)
                .command(Rm.class)
                .create();

        Settings settings = SettingsBuilder.builder()
                .readInputrc(false)
                .logging(true)
                .commandRegistry(registry)
                .build();


        ReadlineConsole console = new ReadlineConsole(settings);
        console.setPrompt(new Prompt("[aesh@extensions]$ "));

        console.start();
    }

}
