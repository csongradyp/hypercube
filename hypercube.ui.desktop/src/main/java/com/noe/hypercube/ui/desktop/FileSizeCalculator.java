package com.noe.hypercube.ui.desktop;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class FileSizeCalculator {

    private static final String[] UNITS = {"b", "Kb", "Mb", "Gb", "Tb", "Pb", "Eb", "Zb", "Yb"};
    private static final String DIR_PLACEHOLDER = "<DIR>";
    private static Double CHANGE_VALUE = 1024d;
    private static DecimalFormat FORMAT = new DecimalFormat("#.#");

    {
        FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    private FileSizeCalculator() {
    }

    public static String calculate(final File file) {
        if (!file.isDirectory()) {
            long length = file.length();
            return humanReadableByteCount( length, false );
        }
        return DIR_PLACEHOLDER;
    }

    public static String calculateSelected(final File file) {
        return humanReadableByteCount( cal( file ), false );
    }

    private static long cal( File file ) {
        long size = 0l;
        if (!file.isDirectory()) {
            return file.length();
        }
        File[] files = file.listFiles();
        if(files != null) {
            for (File fileContent : files) {
                size += cal( fileContent );
            }
        }
        return size;
    }

    private static String calculate(final double length) {
        Double resultSize = length;
        int index = 0;
        while (CHANGE_VALUE < resultSize) {
            resultSize /= CHANGE_VALUE;
            index++;
        }
        return FORMAT.format(resultSize) + " " + UNITS[index];
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
