/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual.parser;

import org.jboss.aesh.console.Config;
import org.jboss.aesh.util.ANSI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ManSection {

    private String name;
    private SectionType type;
    private List<ManParameter> parameters;

    public ManSection() {
        parameters = new ArrayList<ManParameter>();
    }

    public ManSection parseSection(List<String> input, int columns) {
        //first line should be the name
        name = input.get(0);
        type = SectionType.getSectionType(name);
        input.remove(0);
        //the first section, ignoring it for now
        if(input.get(0).startsWith("=") &&
                input.get(0).length() == name.length()) {
           //this is the heading, ignore it
            type = SectionType.IGNORED;
        }
        //starting a new section
        else if(input.get(0).startsWith("-") &&
                input.get(0).trim().length() == type.getType().length()) {
            input.remove(0);
            //a new param
            List<String> newOption = new ArrayList<String>();
            boolean startingNewOption = false;
            boolean paramName = false;
            for(String in : input) {
                if(in.trim().length() > 0)
                    newOption.add(in);
                else {
                    if(newOption.size() > 0) {
                        parameters.add(new ManParameter().parseParams(newOption, columns));
                        newOption.clear();
                    }
                }
            }
            if(!newOption.isEmpty())
                parameters.add(new ManParameter().parseParams(newOption, columns));
        }

        return this;
    }

    public String getName() {
        return name;
    }

    public List<ManParameter> getParameters() {
        return parameters;
    }

    public List<String> getAsList() {
        List<String> out = new ArrayList<String>();
        out.add(ANSI.getBold()+type.getType()+ANSI.defaultText());
        for(ManParameter param : parameters)
            out.addAll(param.getAsList());

        out.add(Config.getLineSeparator());
        return out;
    }

    public SectionType getType() {
        return type;
    }

    public String printToTerminal() {
        StringBuilder builder = new StringBuilder();
        builder.append(ANSI.getBold()).append(type.getType()).append(ANSI.defaultText());
        builder.append(Config.getLineSeparator());
        for(ManParameter param : parameters)
            builder.append(param.printToTerminal());

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManSection)) return false;

        ManSection section = (ManSection) o;

        if (type != section.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
