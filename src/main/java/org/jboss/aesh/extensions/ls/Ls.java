/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.ls;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.extensions.grep.AeshPosixFilePermissions;
import org.jboss.aesh.filters.Filter;
import org.jboss.aesh.filters.file.FileAndDirectoryNoDotNamesFilter;
import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalString;
import org.jboss.aesh.util.PathResolver;

import static org.jboss.aesh.constants.AeshConstants.SPACE;
import static org.jboss.aesh.terminal.Color.BLUE;
import static org.jboss.aesh.terminal.Color.DEFAULT;
import static org.jboss.aesh.terminal.Color.Intensity.BRIGHT;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "ls", description = "[OPTION]... [FILE]...\n" +
    "List information about the FILEs (the current directory by default).\n" +
    "Sort entries alphabetically if none of -cftuvSUX nor --sort is specified.\n")
public class Ls implements Command<CommandInvocation> {

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

    @Option(shortName = 'h', name = "human-readable", hasValue = false,
        description = "with -l, print sizes in human readable format (e.g., 1K 234M 2G)")
    private boolean humanReadable;

    @Arguments
    private List<File> arguments;

    private static final Class<? extends BasicFileAttributes> fileAttributes =
        Config.isOSPOSIXCompatible() ? PosixFileAttributes.class : DosFileAttributes.class;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd hh:mm");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".#");

    public Ls() {
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.UP);
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        // just display help and return
        if (help) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("ls"));
            return CommandResult.SUCCESS;
        }

        if (arguments == null) {
            arguments = new ArrayList<>(1);
            arguments.add(commandInvocation.getAeshContext().getCurrentWorkingDirectory());
        }

        int counter = 0;
        for (File file : arguments) {
            if (counter > 0) {
                commandInvocation.getShell().out().println(Config.getLineSeparator() + file.getName() + ":");
            }

            for (File f : PathResolver.resolvePath(file, commandInvocation.getAeshContext().getCurrentWorkingDirectory())) {
                if (f.isDirectory())
                    displayDirectory(f, commandInvocation.getShell());
                else if (f.isFile())
                    displayFile(f, commandInvocation.getShell());
                else if (!f.exists()) {
                    commandInvocation.getShell().out().println("ls: cannot access " +
                        f.toString() + ": No such file or directory");
                }
            }
            counter++;
        }

        return CommandResult.SUCCESS;
    }

    private void displayDirectory(File input, Shell shell) {
        if (longListing) {
            if (all) {
                File[] files = input.listFiles();
                Arrays.sort(files, new PosixFileNameComparator());
                shell.out().print(displayLongListing(files));
            }
            else {
                File[] files = input.listFiles(Filter.NO_DOT_NAMES.getFileFilter());
                Arrays.sort(files, new PosixFileNameComparator());
                shell.out().print(displayLongListing(files));
            }
        }
        else {
            if (all)
                shell.out().print(Parser.formatDisplayListTerminalString(
                    formatFileList(input.listFiles()),
                    shell.getSize().getHeight(), shell.getSize().getWidth()));
            else
                shell.out().print(Parser.formatDisplayListTerminalString(
                    formatFileList(input.listFiles(new FileAndDirectoryNoDotNamesFilter())),
                    shell.getSize().getHeight(), shell.getSize().getWidth()));

        }
    }

    private List<TerminalString> formatFileList(File[] fileList) {
        ArrayList<TerminalString> list = new ArrayList<TerminalString>(fileList.length);
        for (File file : fileList) {
            if (file.isDirectory())
                list.add(new TerminalString(file.getName(),
                    new TerminalColor(BLUE, DEFAULT)));
            else
                list.add(new TerminalString(file.getName()));
        }
        Collections.sort(list, new PosixTerminalStringNameComparator());
        return list;
    }

    private void displayFile(File input, Shell shell) {
        if (longListing) {
            shell.out().print(displayLongListing(new File[] { input }));
        }
        else {
            shell.out().print(Parser.formatDisplayListTerminalString(
                formatFileList(new File[] { input }), shell.getSize().getHeight(), shell.getSize().getWidth()));
        }
    }

    private String displayLongListing(File[] files) {

        StringGroup access = new StringGroup(files.length);
        StringGroup size = new StringGroup(files.length);
        StringGroup owner = new StringGroup(files.length);
        StringGroup group = new StringGroup(files.length);
        StringGroup modified = new StringGroup(files.length);

        try {
            int counter = 0;
            for (File file : files) {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), fileAttributes);

                access.addString(AeshPosixFilePermissions.toString(((PosixFileAttributes) attr)), counter);
                size.addString(makeSizeReadable(attr.size()), counter);
                if (Config.isOSPOSIXCompatible())
                    owner.addString(((PosixFileAttributes) attr).owner().getName(), counter);
                else
                    owner.addString("", counter);
                if (Config.isOSPOSIXCompatible())
                    group.addString(((PosixFileAttributes) attr).group().getName(), counter);
                else
                    owner.addString("", counter);
                modified.addString(DATE_FORMAT.format(new Date(attr.lastModifiedTime().toMillis())), counter);

                counter++;
            }
        }
        catch (IOException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }

        StringBuilder builder = new StringBuilder();
        TerminalColor directoryColor = new TerminalColor(BLUE, DEFAULT, BRIGHT);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                builder.append(access.getString(i))
                    .append(owner.getFormattedString(i))
                    .append(group.getFormattedString(i))
                    .append(size.getFormattedString(i))
                    .append(SPACE)
                    .append(modified.getString(i))
                    .append(SPACE);
                builder.append(new TerminalString(files[i].getName(), directoryColor))
                    .append(Config.getLineSeparator());

            }
            else {
                builder.append(access.getString(i))
                    .append(owner.getFormattedString(i))
                    .append(group.getFormattedString(i))
                    .append(size.getFormattedString(i))
                    .append(SPACE)
                    .append(modified.getString(i))
                    .append(SPACE)
                    .append(files[i].getName())
                    .append(Config.getLineSeparator());
            }
        }

        return builder.toString();
    }

    private String makeSizeReadable(long size) {
        if (!humanReadable)
            return String.valueOf(size);
        else {
            if (size < 10000)
                return String.valueOf(size);
            else if (size < 10000000) // K
                return DECIMAL_FORMAT.format((double) size / 1024) + "K";
            else if (size < 1000000000) // M
                return DECIMAL_FORMAT.format((double) size / (1048576)) + "M";
            else
                return DECIMAL_FORMAT.format((double) size / (1048576 * 1014)) + "G";
        }
    }

    class PosixTerminalStringNameComparator implements Comparator<TerminalString> {
        private static final char DOT = '.';

        @Override
        public int compare(TerminalString o1, TerminalString o2) {
            if (o1.getCharacters().length() > 1 && o2.getCharacters().length() > 1) {
                if (o1.getCharacters().indexOf(DOT) == 0) {
                    if (o2.getCharacters().indexOf(DOT) == 0)
                        return o1.getCharacters().substring(1).compareToIgnoreCase(o2.getCharacters().substring(1));
                    else
                        return o1.getCharacters().substring(1).compareToIgnoreCase(o2.getCharacters());
                }
                else {
                    if (o2.getCharacters().indexOf(DOT) == 0)
                        return o1.getCharacters().compareToIgnoreCase(o2.getCharacters().substring(1));
                    else
                        return o1.getCharacters().compareToIgnoreCase(o2.getCharacters());
                }
            }
            else
                return 0;
        }
    }

    class PosixFileNameComparator implements Comparator<File> {
        private static final char DOT = '.';

        @Override
        public int compare(File o1, File o2) {
            if (o1.getName().length() > 1 && o2.getName().length() > 1) {
                if (o1.getName().indexOf(DOT) == 0) {
                    if (o2.getName().indexOf(DOT) == 0)
                        return o1.getName().substring(1).compareToIgnoreCase(o2.getName().substring(1));
                    else
                        return o1.getName().substring(1).compareToIgnoreCase(o2.getName());
                }
                else {
                    if (o2.getName().indexOf(DOT) == 0)
                        return o1.getName().compareToIgnoreCase(o2.getName().substring(1));
                    else
                        return o1.getName().compareToIgnoreCase(o2.getName());
                }
            }
            else
                return 0;
        }
    }

}
