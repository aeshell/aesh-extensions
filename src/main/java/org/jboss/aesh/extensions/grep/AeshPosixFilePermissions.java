/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.grep;

import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.*;
/**
 * Copy of PosixFilePermission to also write out file type attribute
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class AeshPosixFilePermissions {
    private AeshPosixFilePermissions() { }

    // Write string representation of permission bits to {@code sb}.
    private static void writeBits(StringBuilder sb, boolean r, boolean w, boolean x) {
        if (r) {
            sb.append('r');
        } else {
            sb.append('-');
        }
        if (w) {
            sb.append('w');
        } else {
            sb.append('-');
        }
        if (x) {
            sb.append('x');
        } else {
            sb.append('-');
        }
    }

    /**
     * @return  the string representation of the file
     */
    public static String toString(PosixFileAttributes attr) {
        Set<PosixFilePermission> perms = attr.permissions();
        StringBuilder sb = new StringBuilder(10);
        if(attr.isDirectory())
            sb.append('d');
        else if(attr.isSymbolicLink())
            sb.append('l');
        else
            sb.append('-');

        writeBits(sb, perms.contains(OWNER_READ), perms.contains(OWNER_WRITE),
          perms.contains(OWNER_EXECUTE));
        writeBits(sb, perms.contains(GROUP_READ), perms.contains(GROUP_WRITE),
          perms.contains(GROUP_EXECUTE));
        writeBits(sb, perms.contains(OTHERS_READ), perms.contains(OTHERS_WRITE),
          perms.contains(OTHERS_EXECUTE));
        return sb.toString();
    }
}
