/*
 * Copyright (C) 2015 KoRe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kore.kolab.notes.fx.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteFactory;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;

/**
 *
 * @author Konrad Renner
 */
public class NotebookController implements Initializable, RefreshViewBus.RefreshListener {
    
    @FXML
    private VBox notebookBox;

    private ResourceBundle bundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToBus();
        bundle = resources;
    }

    private void subscribeToBus() {
        RefreshViewBus.subscribe(this, RefreshViewBus.RefreshTypes.CHANGE_ACCOUNT,
                RefreshViewBus.RefreshTypes.SYNCED_ACCOUNT,
                RefreshViewBus.RefreshTypes.DELETED_NOTEBOOK,
                RefreshViewBus.RefreshTypes.NEW_NOTEBOOK,
                RefreshViewBus.RefreshTypes.NEW_ACCOUNT);
    }

    @Override
    public void refreshRequest(RefreshViewBus.RefreshEvent event) {
        NoteRepository repo = new NoteRepository();
        List<FXNotebook> notebooks = repo.getNotebooks(event.getActiveAccount());

        ArrayList<Node> nbNames = new ArrayList<>(notebooks.size());
        notebooks.stream().forEach((notebook) -> {
            Text text = new Text(notebook.getSummary());
            text.setTextAlignment(TextAlignment.CENTER);

            Hyperlink link = new Hyperlink("select");
            link.setOnAction(ev -> {
                RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), notebook.getId(), RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK);
                RefreshViewBus.informListener(refreshEvent);
            });

            TextFlow flow = new TextFlow(text, link);
            nbNames.add(flow);
        });

        notebookBox.getChildren().setAll(nbNames);
    }

    @Override
    public String getId() {
        return "NotebookController";
    }

    @FXML
    void showAllNotes(ActionEvent event) {
        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), null, RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK);
        RefreshViewBus.informListener(refreshEvent);
    }

    @FXML
    void addNotebook(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(bundle.getString("createNotebook"));
        dialog.setTitle(bundle.getString("createNotebook"));
        dialog.setContentText(bundle.getString("enterNotebookName"));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            NoteRepository repo = new NoteRepository();

            FXNotebook notebook = new NoteFactory(ToolbarController.getSelectedAccount()).newNotebook(name);
            repo.createNotebook(notebook);
            
            RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), notebook.getId(), RefreshViewBus.RefreshTypes.NEW_NOTEBOOK);
            RefreshViewBus.informListener(refreshEvent);
        });
    }
    
    @FXML
    void deleteNotebook(ActionEvent event) {
        NoteRepository repo = new NoteRepository();

        List<FXNotebook> notebooks = repo.getNotebooks(ToolbarController.getSelectedAccount());

        if (notebooks.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(bundle.getString("warning"));
            alert.setHeaderText(null);
            alert.setContentText(bundle.getString("nonotebooks"));

            alert.showAndWait();
            return;
        }

        List<String> choices = new ArrayList<>(notebooks.size());

        notebooks.stream().forEach((notebook) -> {
            choices.add(notebook.getSummary());
        });

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(bundle.getString("deleteNotebook"));
        dialog.setHeaderText(null);
        dialog.setContentText(bundle.getString("selectNotebookForDeletion"));

        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent((String name) -> {
            FXNotebook book = repo.getNotebookBySummary(ToolbarController.getSelectedAccount(), name);
            
            repo.deleteNotebook(book);

            RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), book.getId(), RefreshViewBus.RefreshTypes.DELETED_NOTEBOOK);

            RefreshViewBus.informListener(refreshEvent);
        });
    }
}
