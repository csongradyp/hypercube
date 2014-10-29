package com.noe.hypercube.ui.util;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

import static com.noe.hypercube.ui.util.FileNameConvensionUtil.getStyleSheetFileName;

public final class StyleUtil {

    private static final String STYLESHEETS_LOCATION = "style/";

    private StyleUtil() {
    }

    public static void changeStyle(final Parent view, final String storage) {
        final ObservableList<AccountInfo> accounts = AccountBundle.getAccounts();
        clearViewFromStyles(view, accounts);
        final Path stylePath = Paths.get(STYLESHEETS_LOCATION, getStyleSheetFileName(storage));
        final String styleURL = getFileURL(stylePath);
        if(exists(stylePath) && !view.getStylesheets().contains(styleURL)) {
            view.getStylesheets().add(styleURL);
        }
    }

    private static void clearViewFromStyles(Parent view, ObservableList<AccountInfo> accounts) {
        for (AccountInfo account : accounts) {
            final String styleURL = getFileURL(Paths.get(STYLESHEETS_LOCATION, getStyleSheetFileName(account.getName())));
            view.getStylesheets().remove(styleURL);
        }
    }

    private static boolean exists(Path stylePath) {
        return stylePath.toFile().exists();
    }

    private static String getFileURL(Path stylePath) {
        return "file:///" + stylePath.toFile().getAbsolutePath().replace("\\", "/");
    }
}
