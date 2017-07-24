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
package org.aesh.extensions.highlight;

public enum TokenType {
    debug, // highlight for debugging (white on blue background)

    annotation, // Groovy, Java
    attribute_name, // HTML, CSS
    attribute_value, // HTML
    binary, // Python, Ruby
    char_, // most scanners, also inside of strings
    class_, // lots of scanners, for different purposes also in CSS
    class_variable, // Ruby, YAML
    color, // CSS
    comment, // most scanners
    constant, // PHP, Ruby
    content, // inside of strings, most scanners
    decorator, // Python
    definition, // CSS
    delimiter, // inside strings, comments and other types
    directive, // lots of scanners
    doctype, // Goorvy, HTML, Ruby, YAML
    docstring, // Python
    done, // Taskpaper
    entity, // HTML
    error, // invalid token, most scanners
    escape, // Ruby (string inline variables like //$foo, //@bar)
    exception, // Java, PHP, Python
    filename, // Diff
    float_, // most scanners
    function, // CSS, JavaScript, PHP
    method, // groovy
    global_variable, // Ruby, YAML
    hex, // hexadecimal number; lots of scanners
    id, // CSS
    imaginary, // Python
    important, // CSS, Taskpaper
    include, // C, Groovy, Java, Python, Sass
    inline, // nested code, eg. inline string evaluation; lots of scanners
    inline_delimiter, // used instead of :inline > :delimiter FIXME: Why use inline_delimiter?
    instance_variable, // Ruby
    integer, // most scanners
    key, // lots of scanners, used together with :value
    keyword, // reserved word that's actually implemented; most scanners
    label, // C, PHP
    local_variable, // local and magic variables; some scanners
    map, // Lua tables
    modifier, // used inside on strings; lots of scanners
    namespace, // Clojure, Java, Taskpaper
    octal, // lots of scanners
    predefined, // predefined function: lots of scanners
    predefined_constant, // lots of scanners
    predefined_type, // C, Java, PHP
    preprocessor, // C, Delphi, HTML
    pseudo_class, // CSS
    regexp, // Groovy, JavaScript, Ruby
    reserved, // most scanners
    shell, // Ruby
    string, // most scanners
    symbol, // Clojure, Ruby, YAML
    tag, // CSS, HTML
    type, // CSS, Java, SQL, YAML
    value, // used together with :key; CSS, JSON, YAML
    variable, // Sass, SQL, YAML

    change, // Diff
    delete, // Diff
    head, // Diff, YAML
    insert, // Diff
    eyecatcher, // Diff

    ident, // almost all scanners
    operator, // almost all scanners

    space, // almost all scanners
    plain, // almost all scanners
    unknown
}
