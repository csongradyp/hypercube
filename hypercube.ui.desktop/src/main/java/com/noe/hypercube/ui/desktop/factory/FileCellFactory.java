package com.noe.hypercube.ui.desktop.factory;

import com.noe.hypercube.ui.desktop.domain.IFile;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

public class FileCellFactory implements Callback<TableColumn<IFile, IFile>, TableCell<IFile, IFile>> {

    private CellText cellText;
    private CellGraphic cellGraphic;
    private TextAlignment alignment;

    public FileCellFactory(TextAlignment alignment, CellText cellText) {
        this.cellText = cellText;
        this.alignment = alignment;
    }

    public FileCellFactory(CellGraphic cellGraphic) {
        this(TextAlignment.LEFT);
        this.cellGraphic = cellGraphic;
    }

    public FileCellFactory(TextAlignment alignment) {
        this.alignment = alignment;
    }

    @Override
    public TableCell<IFile, IFile> call(TableColumn<IFile, IFile> iFileIFileTableColumn) {
        return new TableCell<IFile, IFile>() {
            @Override
            public void updateItem(IFile item, boolean empty) {
                if ( item == getItem() ) {
                    return;
                }
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    item.getSelectionProperty().addListener((observable, oldValue, newValue) -> updateItem(item, false));
                    if (cellText != null) {
                        setText(cellText.getCellText(item));
                    }
                    if(cellGraphic != null) {
                        setGraphic(cellGraphic.getCellGraphic(item));
                    }
                    if (item.isSelected()) {
                        getStyleClass().add("table-row-marked");
                    } else {
                        getStyleClass().remove("table-row-marked");
                    }
                    setTextAlignment(alignment);
                    switch ( alignment ) {
                        case CENTER:
                            setAlignment( Pos.CENTER );
                            break;
                        case RIGHT:
                            setAlignment( Pos.CENTER_RIGHT );
                            break;
                        default:
                            setAlignment( Pos.CENTER_LEFT );
                            break;
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }

    public interface CellText {
        public String getCellText(IFile item);
    }

    public interface CellGraphic {
        public Node getCellGraphic(IFile item);
    }

    public void setCellGraphic(CellGraphic cellGraphic) {
        this.cellGraphic = cellGraphic;
    }
}
