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
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.comparators.PosixFileNameComparator;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.extensions.grep.AeshPosixFilePermissions;
import org.jboss.aesh.io.FileAndDirectoryNoDotNamesFilter;
import org.jboss.aesh.io.FileResource;
import org.jboss.aesh.io.Resource;
import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalString;

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

    @Option(shortName = 'h', name ="human-readable", hasValue = false,
            description = "with -l, print sizes in human readable format (e.g., 1K 234M 2G)")
    private boolean humanReadable;

    @Arguments
    private List<Resource> arguments;

    private static final char SPACE = ' ';

    private static final Class<? extends BasicFileAttributes> fileAttributes =
            Config.isOSPOSIXCompatible() ? PosixFileAttributes.class : DosFileAttributes.class;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd HH:mm");
    private static final DateFormat OLD_FILE_DATE_FORMAT = new SimpleDateFormat("MMM dd  yyyy");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".#");

    private static final TerminalColor DIRECTORY_COLOR = new TerminalColor(Color.BLUE, Color.DEFAULT, Color.Intensity.BRIGHT);
    private static final TerminalColor SYMBOLIC_LINK_COLOR = new TerminalColor(Color.CYAN, Color.DEFAULT, Color.Intensity.BRIGHT);

    public Ls() {
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.UP);
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        //just display help and return
        if(help) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("ls"));
            return CommandResult.SUCCESS;
        }

        if(arguments == null) {
            arguments = new ArrayList<>(1);
            arguments.add(commandInvocation.getAeshContext().getCurrentWorkingDirectory());
        }

        int counter = 0;
        for(Resource file : arguments) {
            if(counter > 0) {
                commandInvocation.getShell().out().println(Config.getLineSeparator()+file.getName()+":");
            }

            for(Resource f : file.resolve(commandInvocation.getAeshContext().getCurrentWorkingDirectory())) {
                if(f.isDirectory())
                    displayDirectory(f, commandInvocation.getShell());
                else if(f.isLeaf())
                    displayFile(f,commandInvocation.getShell());
                else if(!f.exists()) {
                    commandInvocation.getShell().out().println("ls: cannot access "+
                            f.toString()+": No such file or directory");
                }
            }
            counter++;
        }

        return CommandResult.SUCCESS;
    }

    private void displayDirectory(Resource input, Shell shell) {
        List<Resource> files = new ArrayList<>();

        if (all) {
            // add "." and ".." to list of files
            files.add(input.newInstance("."));
            files.add(input.newInstance(".."));
            files.addAll(input.list());

        }
        else {
            files = input.list(new FileAndDirectoryNoDotNamesFilter());
        }

        Collections.sort(files, new PosixFileComparator());

        if (longListing) {
            shell.out().print(displayLongListing(files));
        }
        else {
            shell.out().print(Parser.formatDisplayCompactListTerminalString(
                formatFileList(files),
                shell.getSize().getWidth()));
        }
    }

    private List<TerminalString> formatFileList(List<Resource> fileList) {
        ArrayList<TerminalString> list = new ArrayList<>(fileList.size());
        for(Resource file : fileList) {
            if (file.isSymbolicLink()) {
                list.add(new TerminalString(file.getName(), SYMBOLIC_LINK_COLOR));
            } else if (file.isDirectory()) {
                list.add(new TerminalString(file.getName(), DIRECTORY_COLOR));
            } else {
                list.add(new TerminalString(file.getName()));
            }
        }
        return list;
    }

    private void displayFile(Resource input, Shell shell) {
        if(longListing) {
            List<Resource> resourceList = new ArrayList<>(1);
            resourceList.add(input);
            shell.out().print(displayLongListing(resourceList));
        }
        else {
            List<Resource> resourceList = new ArrayList<>(1);
            resourceList.add(input);
            shell.out().print(Parser.formatDisplayListTerminalString(
                    formatFileList(resourceList), shell.getSize().getHeight(), shell.getSize().getWidth()));
        }
    }

    private String displayLongListing(List<Resource> files) {

        StringGroup access = new StringGroup(files.size());
        StringGroup size = new StringGroup(files.size());
        StringGroup owner = new StringGroup(files.size());
        StringGroup group = new StringGroup(files.size());
        StringGroup modified = new StringGroup(files.size());

        try {
            int counter = 0;
            for(Resource file : files) {
                BasicFileAttributes attr = file.readAttributes(fileAttributes, LinkOption.NOFOLLOW_LINKS);

                if (Config.isOSPOSIXCompatible()) {
                    access.addString(AeshPosixFilePermissions.toString(((PosixFileAttributes) attr)), counter);
                } else {
                    access.addString("", counter);
                }

                size.addString(makeSizeReadable(attr.size()), counter);

                if (Config.isOSPOSIXCompatible()) {
                    owner.addString(((PosixFileAttributes) attr).owner().getName(), counter);
                    group.addString(((PosixFileAttributes) attr).group().getName(), counter);
                } else {
                    owner.addString("", counter);
                    group.addString("", counter);
                }

                // show year instead of time when file wasn't changed in actual year
                Date lastModifiedTime = new Date(attr.lastModifiedTime().toMillis());

                Calendar lastModifiedCalendar = Calendar.getInstance();
                lastModifiedCalendar.setTime(lastModifiedTime);

                Calendar nowCalendar = Calendar.getInstance();

                if (lastModifiedCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR)) {
                    modified.addString(DATE_FORMAT.format(lastModifiedTime), counter);
                } else {
                    modified.addString(OLD_FILE_DATE_FORMAT.format(lastModifiedTime), counter);
                }

                counter++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < files.size(); i++) {
            builder.append(access.getString(i))
                .append(owner.getFormattedStringPadRight(i))
                .append(group.getFormattedStringPadRight(i))
                .append(size.getFormattedString(i))
                .append(SPACE)
                .append(modified.getString(i))
                .append(SPACE);

            if (files.get(i).isSymbolicLink()) {
                builder.append(new TerminalString(files.get(i).getName(), SYMBOLIC_LINK_COLOR));
                builder.append(" -> ");
                try {
                    builder.append(files.get(i).readSymbolicLink());
                } catch (IOException ex) {
                    ex.printStackTrace(); // this should not happen
                }
            } else if (files.get(i).isDirectory()) {
                builder.append(new TerminalString(files.get(i).getName(), DIRECTORY_COLOR));
            } else {
                builder.append(files.get(i).getName());
            }

            builder.append(Config.getLineSeparator());
        }

        return builder.toString();
    }

    private String makeSizeReadable(long size) {
        if(!humanReadable)
            return String.valueOf(size);
        else {
            if(size < 10000)
                return String.valueOf(size);
            else if(size < 10000000) //K
                return DECIMAL_FORMAT.format((double) size/1024)+"K";
            else if(size < 1000000000) //M
                return DECIMAL_FORMAT.format((double) size/(1048576))+"M";
            else
                return DECIMAL_FORMAT.format((double) size/(1048576*1014))+"G";
        }
    }

    class PosixFileComparator implements Comparator<Resource> {
        private PosixFileNameComparator posixFileNameComparator = new PosixFileNameComparator();

        @Override
        public int compare(Resource o1, Resource o2) {
            return posixFileNameComparator.compare(o1.getName(), o2.getName());
        }
    }
}
