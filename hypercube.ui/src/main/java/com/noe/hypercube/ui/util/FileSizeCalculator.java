package com.noe.hypercube.ui.util;


import com.noe.hypercube.ui.domain.IFile;

public final class FileSizeCalculator {

    private static final Long UNIT = 1024L;
    private static final String SIZE_SYMBOLS = "kMGTPEZY";
    private static final String DIR_PLACEHOLDER = "<DIR>  ";

    private FileSizeCalculator() {
    }

    public static String calculate(final IFile file) {
        if (!file.isDirectory()) {
            return humanReadableByteCount(file.size());
        }
        return DIR_PLACEHOLDER;
    }

    private static String humanReadableByteCount(final Long bytes) {
        if (bytes < UNIT) {
            return bytes + " b";
        }
        final Double exp = StrictMath.log(bytes) / StrictMath.log(UNIT);
        final String pre = SIZE_SYMBOLS.charAt(exp.intValue() - 1) + "i";
        return String.format("%.1f %sB", bytes / StrictMath.pow( UNIT, exp), pre);
    }
}
