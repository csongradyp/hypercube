package com.noe.hypercube.ui.util;

import com.noe.hypercube.ui.bundle.AccountBundle;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.noe.hypercube.ui.util.FileNameConvensionUtil.getStyleSheetFileName;

public final class StyleUtil {

    private static final String STYLESHEETS_LOCATION = "style/";

    private StyleUtil() {
    }

    public static void changeStyle(Parent parent, String storage) {
        final ObservableList<String> accounts = AccountBundle.getAccounts();
        for (String account : accounts) {
            parent.getStylesheets().remove(STYLESHEETS_LOCATION + getStyleSheetFileName(account));
        }
        final String styleSheetFileName = getStyleSheetFileName(storage);
        final Path stylePath = Paths.get(STYLESHEETS_LOCATION, styleSheetFileName);
        if(stylePath.toFile().exists()) {
            parent.getStylesheets().add("file:///" + stylePath.toFile().getAbsolutePath().replace("\\", "/"));
        }
    }
}
