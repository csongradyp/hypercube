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

    public static String humanReadableByteCount(Long bytes) {
        if (bytes < UNIT) return bytes + " B";
        final Integer exp = new Double(StrictMath.log(bytes) / StrictMath.log(UNIT)).intValue();
        String pre = SIZE_SYMBOLS.charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / StrictMath.pow(UNIT, exp), pre);
    }
}
