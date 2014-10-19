package com.noe.hypercube.ui.util;


public final class FileNameConvensionUtil {

    private FileNameConvensionUtil() {
    }

    public static final String ACCOUNT_ICON_POSTFIX = "-icon.png";

    public static String getIconFileName(String account) {
        return String.format("%s%s", getAccountFilePrefix(account), ACCOUNT_ICON_POSTFIX);
    }

    public static String getStyleSheetFileName(String account) {
        return String.format("%s.css", getAccountFilePrefix(account));
    }

    private static String getAccountFilePrefix(String account) {
        return account.replace(" ", "").toLowerCase();
    }
}
