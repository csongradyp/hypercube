package com.noe.hypercube.ui.desktop;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static com.noe.hypercube.ui.desktop.NavigationUtil.navigateTo;


public class FileView extends VBox implements Initializable {

    @FXML
    private TableView<File> table;
    @FXML
    private TableColumn<File, String> extColumn;
    @FXML
    private TableColumn<File, String> fileNameColumn;
    @FXML
    private TableColumn<File, String> fileSizeColumn;
    @FXML
    private TableColumn<File, String> dateColumnRight;

    @FXML
    private BreadCrumbBar<String> breadcrumb;
    @FXML
    private SegmentedButton localDrives;
    @FXML
    private SegmentedButton remoteDrives;

    private boolean selected;

    public FileView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLocalDrives();
        setupRemoteDrives();
        setupFileTableView();
        breadcrumb.setOnCrumbAction( new BreadCrumbEventHandler( table ) );
    }

    private void setupRemoteDrives() {
        ToggleButton addButton = new ToggleButton( "+" );
        addButton.setTooltip( new Tooltip( "Add new remote drive") );
        remoteDrives.getButtons().add( addButton );
        addButton.setOnMouseClicked( event -> {
            remoteDrives.getButtons().add(0, new ToggleButton( "New" ));
            addButton.setSelected( false );
        } );
    }

    private void setupFileTableView() {
        FileViewKeyEventHandler keyEventHandlerRight = new FileViewKeyEventHandler( table, breadcrumb );
        keyEventHandlerRight.init( new File( "c:/" ) );
        table.setOnKeyPressed( keyEventHandlerRight );
        table.setOnMouseClicked( new FileViewMouseEventHandler( breadcrumb ) );
        Platform.runLater( () -> {
            if ( isSelected() ) {
                table.requestFocus();
            }
        }
        );

        fileNameColumn.setCellValueFactory( new PropertyValueFactory<>( "Name" ) );

        fileSizeColumn.setCellFactory( new FormattedTableCellFactory<>( TextAlignment.RIGHT ) );
        fileSizeColumn.setCellValueFactory( param -> new ReadOnlyObjectWrapper( isStepBack( param ) ? "" : FileSizeCalculator.calculate( param.getValue() ) ) );

        extColumn.setCellValueFactory( param -> new ReadOnlyObjectWrapper( param.getValue().isFile() ? FilenameUtils.getExtension( param.getValue().getName() ) : "" ) );

        dateColumnRight.setCellValueFactory( param -> new ReadOnlyObjectWrapper( isStepBack( param ) ? "" : new Date( param.getValue().lastModified() ).toString() ) );
    }

    private boolean isStepBack( TableColumn.CellDataFeatures<File, String> param ) {
        return param.getValue().getName().equals( NavigationUtil.TO_PARENT_PLACEHOLDER );
    }

    private void setupLocalDrives() {
        File[] roots = File.listRoots();
        List<ToggleButton> drives = new ArrayList<>( 5 );
        for ( File root : roots ) {
            ToggleButton button = new ToggleButton( root.getPath() );
            button.setOnMouseClicked( event -> {
                ToggleButton source = (ToggleButton) event.getSource();
                navigateTo( table, breadcrumb, new File( source.getText() ) );
                if(!source.isSelected()) {
                    source.setSelected( true );
                }
            } );
            drives.add( button );
        }
        localDrives.getButtons().addAll( drives );
        localDrives.getButtons().get( 0 ).setSelected( true );
        localDrives.setOnMouseClicked( new DriveMouseEventHandler( breadcrumb, table ) );
    }

    public boolean isSelected() {
        return selected;
    }

    @FXML
    public void setSelected( boolean selected ) {
        this.selected = selected;
    }
}
