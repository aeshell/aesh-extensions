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
package org.aesh.extensions.text.highlight.scanner;

import org.aesh.extensions.highlight.Syntax;
import org.junit.Test;

public class SQLScannerTestCase extends AbstractScannerTestCase {

    @Test
    public void shouldMatchSQLCreateTablesExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "create_tables.in.sql");
    }

    @Test
    public void shouldMatchSQLMaintenanceExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "maintenance.in.sql");
    }

    @Test
    public void shouldMatchSQLMySQLCommentsExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "mysql-comments.in.sql");
    }

    @Test
    public void shouldMatchSQLMySQLLongQueryExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "mysql-long-queries.in.sql");
    }

    @Test
    public void shouldMatchSQLNorwegianExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "norwegian.in.sql");
    }

    @Test
    public void shouldMatchSQLPittsburghExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "pittsburgh.in.sql");
    }

    @Test
    public void shouldMatchSQLReferenceExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "reference.in.sql");
    }

    @Test
    public void shouldMatchSQLSelectsInExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "selects.in.sql");
    }

    @Test
    public void shouldMatchSQLTheGoatHerderIssue163Example() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "sql", "thegoatherder-issue-163.in.sql");
    }
}