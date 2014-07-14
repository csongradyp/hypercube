package com.noe.hypercube.ui.util;


import com.noe.hypercube.ui.domain.IFile;

public final class FileSizeCalculator {

    private static final String DIR_PLACEHOLDER = "<DIR>  ";

    private FileSizeCalculator() {
    }

    public static String calculate(final IFile file) {
        if (!file.isDirectory()) {
            long length = file.size();
            return humanReadableByteCount(length, false);
        }
        return DIR_PLACEHOLDER;
    }

    public static String calculateSelected(final java.io.File file) {
        return humanReadableByteCount(cal(file), false);
    }

    private static long cal(java.io.File file) {
        long size = 0l;
        if (!file.isDirectory()) {
            return file.length();
        }
        java.io.File[] files = file.listFiles();
        if (files != null) {
            for (java.io.File fileContent : files) {
                size += cal(fileContent);
            }
        }
        return size;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPEZY" : "KMGTPEZY").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
