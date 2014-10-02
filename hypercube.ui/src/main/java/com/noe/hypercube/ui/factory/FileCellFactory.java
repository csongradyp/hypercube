package com.noe.hypercube.ui.factory;

import com.noe.hypercube.ui.domain.file.IFile;
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
    public TableCell<IFile, IFile> call(TableColumn<IFile, IFile> tableColumn) {
        final TableCell<IFile, IFile> tableCell = new TableCell<IFile, IFile>() {
            @Override
            public void updateItem(IFile file, boolean empty) {
                super.updateItem(file, empty);
                if (file != null && !empty) {
                    file.getMarkedProperty().addListener((observable, oldValue, newValue) -> updateItem(file, false));
                    if (cellText != null) {
                        setText(cellText.getCellText(file));
                    }
                    if (cellGraphic != null) {
                        setGraphic(cellGraphic.getCellGraphic(file));
                    }
                    getStyleClass().removeAll("table-row-marked", "table-row-shared");
                    if (file.isMarked()) {
                        getStyleClass().add("table-row-marked");
                    }
                    if (file.isShared()) {
                        getStyleClass().add("table-row-shared");
                    }
                    setTextAlignment(alignment);
                    switch (alignment) {
                        case CENTER:
                            setAlignment(Pos.CENTER);
                            break;
                        case RIGHT:
                            setAlignment(Pos.CENTER_RIGHT);
                            break;
                        default:
                            setAlignment(Pos.CENTER_LEFT);
                            break;
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };

        tableCell.setOnMouseDragEntered(event -> {
            final IFile item = tableCell.getItem();
            if(item != null && event.getGestureSource() == tableCell.getTableView()) {
                item.mark();
            }
            event.consume();
        });

        return tableCell;
    }

    public interface CellText {
        public String getCellText(IFile item);
    }

    public interface CellGraphic {
        public Node getCellGraphic(IFile item);
    }

}
