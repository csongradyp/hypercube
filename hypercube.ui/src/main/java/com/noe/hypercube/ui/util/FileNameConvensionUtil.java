package com.noe.hypercube.ui.util;


public final class FileNameConvensionUtil {

    private FileNameConvensionUtil() {
    }

    public static final String ACCOUNT_ICON_POSTFIX = "-icon.png";

    public static String getIconFileName(String account) {
        return account.replace(" ", "").toLowerCase() + ACCOUNT_ICON_POSTFIX;
    }

    public static String getStyleSheetFileName(String account) {
        return account.replace(" ", "").toLowerCase() + ".css";
    }
}
