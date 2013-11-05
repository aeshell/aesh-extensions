/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.ls;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalString;
import org.jboss.aesh.util.FileLister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "ls", description = "[OPTION]... [FILE]...\n" +
        "List information about the FILEs (the current directory by default).\n" +
        "Sort entries alphabetically if none of -cftuvSUX nor --sort is specified.\n")
public class Ls implements Command {

    @Option(shortName = 'H', name = "help", hasValue = false,
            description = "display this help and exit")
    private boolean help;
    @Option(shortName = 'a', name = "all", hasValue = false,
    description = "do not ignore entries starting with .")
    private boolean all;
    @Option(shortName = 'd', name = "directory", hasValue = false,
            description = "list directory entries instead of contents, and do not dereference symbolic links")
    private boolean directory;
    @Option(shortName = 'l', name = "longlisting", hasValue = false,
            description = "use a long listing format")
    private boolean longListing;

    @Option(shortName = 's', name = "size", hasValue = false,
            description = "print the allocated size of each file, in blocks")
    private boolean size;

    @Arguments
    private List<File> arguments;

    private File cwd;

    public Ls() {
        cwd = new File(Config.getUserDir());
    }

    public Ls(File cwd) {
        if(cwd != null && cwd.isDirectory())
            this.cwd = cwd;
        else
            cwd = new File(Config.getUserDir());
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        //just display help and return
        if(help) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("ls"));
            return CommandResult.SUCCESS;
        }

        if(arguments == null) {
            arguments = new ArrayList<File>(1);
            arguments.add(cwd);
        }

        for(File f : arguments) {
            if(f.isDirectory())
                displayDirectory(f, commandInvocation.getShell());
            else if(f.isFile())
                displayFile(f,commandInvocation.getShell());
            else if(!f.exists()) {
                commandInvocation.getShell().out().println("ls: cannot access "+
                        f.toString()+": No such file or directory");
            }
        }

        return CommandResult.SUCCESS;
    }

    public void setCwd(File cwd) {
        if(cwd.isDirectory())
            this.cwd = cwd;
    }

    private void displayDirectory(File input, Shell shell) {
        if(longListing) {
            if(all)
                shell.out().println(
                        displayLongListing(input.listFiles()));
            else
                shell.out().println(
                        displayLongListing(input.listFiles(new FileLister.FileAndDirectoryNoDotNamesFilter())));
        }
        else {
            if(all)
                shell.out().println(Parser.formatDisplayListTerminalString(
                        formatFileList(input.listFiles()),
                        shell.getSize().getHeight(), shell.getSize().getWidth()));
            else
                shell.out().println(Parser.formatDisplayListTerminalString(
                        formatFileList(input.listFiles(new FileLister.FileAndDirectoryNoDotNamesFilter())),
                        shell.getSize().getHeight(), shell.getSize().getWidth()));

        }
    }

    private List<TerminalString> formatFileList(File[] fileList) {
        ArrayList<TerminalString> list = new ArrayList<TerminalString>(fileList.length);
        for(File file : fileList) {
            if(file.isDirectory())
                list.add(new TerminalString(file.getName(),
                        new TerminalColor(Color.BLUE, Color.DEFAULT)));
            else
                list.add(new TerminalString(file.getName()));
        }
        Collections.sort(list, new PosixFileNameComparator());
        return list;
    }

    private void displayFile(File input, Shell shell) {

    }

    private String displayLongListing(File[] files) {

        StringGroup access = getAccessString(files);
        StringGroup size = getSize(files);
        StringGroup owner = getOwner(files);
        StringGroup group = getGroup(files);
        StringGroup modified = getModified(files);

        StringBuilder builder = new StringBuilder();
        for(int i=0; i < files.length; i++) {
            builder.append(access.getString(i))
                    .append(size.getString(i))
                    .append(owner.getString(i))
                    .append(group.getString(i))
                    .append(modified.getString(i))
                    .append(" ")
                    .append(files[i].getName())
                    .append(Config.getLineSeparator());
        }

        return builder.toString();
    }

    private StringGroup getAccessString(File[] files) {
        StringGroup access = new StringGroup(files.length);
        int counter = 0;
        for(File file : files) {
            StringBuilder builder = new StringBuilder();
            if(file.isDirectory())
                builder.append("d");
            else
                builder.append("-");
            if(file.canRead())
                builder.append("r");
            else
                builder.append("-");
            if(file.canWrite())
                builder.append("w");
            else
                builder.append("-");
            if(file.canExecute())
                builder.append("x");
            else
                builder.append("-");

           access.addString(builder.toString(), counter++);
        }
        access.formatStringsBasedOnMaxLength();

        return access;
    }

    private StringGroup getSize(File[] files) {
        StringGroup size = new StringGroup(files.length);
        int counter = 0;
        for(File file : files) {
            //StringBuilder builder = new StringBuilder();

            size.addString("1", counter++);
        }
        size.formatStringsBasedOnMaxLength();

        return size;
    }

    private StringGroup getOwner(File[] files) {
        StringGroup owner = new StringGroup(files.length);
        int counter = 0;
        for(File file : files) {
            //StringBuilder builder = new StringBuilder();

            owner.addString("owner", counter++);
        }
        owner.formatStringsBasedOnMaxLength();

        return owner;
    }

    private StringGroup getGroup(File[] files) {
        StringGroup group = new StringGroup(files.length);
        int counter = 0;
        for(File file : files) {
            //StringBuilder builder = new StringBuilder();

            group.addString("group", counter++);
        }
        group.formatStringsBasedOnMaxLength();

        return group;
    }

    private StringGroup getModified(File[] files) {
        StringGroup modified = new StringGroup(files.length);
        int counter = 0;
        for(File file : files) {
            modified.addString(new Date(file.lastModified()).toString(), counter++);
        }
        modified.formatStringsBasedOnMaxLength();

        return modified;
    }

    class PosixFileNameComparator implements Comparator<TerminalString> {
        private static final char DOT = '.';

        @Override
        public int compare(TerminalString o1, TerminalString o2) {
            if(o1.getCharacters().length() > 1 && o2.getCharacters().length() > 1) {
                if(o1.getCharacters().indexOf(DOT) == 0) {
                    if(o2.getCharacters().indexOf(DOT) == 0)
                        return o1.getCharacters().substring(1).compareToIgnoreCase(o2.getCharacters().substring(1));
                    else
                        return o1.getCharacters().substring(1).compareToIgnoreCase(o2.getCharacters());
                }
                else {
                    if(o2.getCharacters().indexOf(DOT) == 0)
                        return o1.getCharacters().compareToIgnoreCase(o2.getCharacters().substring(1));
                    else
                        return o1.getCharacters().compareToIgnoreCase(o2.getCharacters());
                }
            }
            else
                return 0;
        }
    }
}
