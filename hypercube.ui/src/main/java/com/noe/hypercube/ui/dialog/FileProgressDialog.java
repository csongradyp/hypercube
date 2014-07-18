package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.OnProgressListener;
import com.noe.hypercube.ui.util.ProgressAwareInputStream;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Dialog;

import java.nio.file.Path;
import java.util.ResourceBundle;

public class FileProgressDialog extends Dialog {

    private final static String title = "title.copy";
    private final ProgressBar progressBar = new ProgressBar( 0 );
    private final ProgressIndicator indicator = new ProgressIndicator( 0 );
    private OnProgressListener onProgressListener;

    public FileProgressDialog( Object owner, ResourceBundle bundle, Path from, Path to ) {
        super( owner, bundle.getString( title ) );
        init(bundle, from, to);
    }

    private void init(ResourceBundle bundle, Path from, Path to) {
        progressBar.setPrefWidth( 300 );
        indicator.autosize();
        final Label fromLabel = new Label( bundle.getString( "copy.from" ) + from + "*" );
        final Label toLabel = new Label( bundle.getString( "copy.to" ) + to + "*");
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
