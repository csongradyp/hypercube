package com.noe.hypercube.ui.desktop.dialog;

import com.noe.hypercube.ui.desktop.OnProgressListener;
import com.noe.hypercube.ui.desktop.util.ProgressAwareInputStream;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;

import java.nio.file.Path;

public class FileProgressDialog extends Dialog {

    private final ProgressBar progressBar = new ProgressBar( 0 );
    private final ProgressIndicator indicator = new ProgressIndicator( 0 );
    private OnProgressListener onProgressListener;

    public FileProgressDialog( Object owner, String title, Path from, Path to ) {
        super( owner, title );
        init(from, to);
    }

    public FileProgressDialog( Object owner, String title, boolean lightweight, Path from, Path to ) {
        super( owner, title, lightweight );
        init(from, to );
    }

    public FileProgressDialog( Object owner, String title, boolean lightweight, DialogStyle style, Path from, Path to ) {
        super( owner, title, lightweight, style );
        init(from, to );
    }

    private void init(Path from, Path to) {
        progressBar.setPrefWidth( 300 );
        indicator.autosize();
        final Label fromLabel = new Label( "From: " + from + "*" );
        final Label toLabel = new Label( "To: " + to + "*");
        final Label currentFile = new Label();
        final HBox progressBox = new HBox( 5, progressBar, indicator );
        progressBox.setAlignment( Pos.CENTER );
        final VBox content = new VBox( 5, fromLabel, toLabel, currentFile, progressBox );
        setContent( content );
    }

    public void resetProgressBar() {
        progressBar.setProgress( 0 );
    }

    public void setCurrentProgress( ProgressAwareInputStream stream ) {
        stream.setOnProgressListener( ( percentage, tag ) -> Platform.runLater( () -> {
            progressBar.setProgress( percentage );
            if ( percentage == 1 ) {
                hide();
            }
        } ) );
    }
}
