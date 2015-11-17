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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteFactory;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;

/**
 *
 * @author Konrad Renner
 */
public class NotebookController implements Initializable {
    
    @FXML
    private VBox notebookBox;

    private static ObservableList<Node> NOTEBOOKS;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NOTEBOOKS = notebookBox.getChildren();
    }

    public final static void refreshView(String accountId) {
        NoteRepository repo = new NoteRepository();
        List<FXNotebook> notebooks = repo.getNotebooks(accountId);

        ArrayList<Node> nbNames = new ArrayList<>(notebooks.size());
        notebooks.stream().forEach((notebook) -> {
            nbNames.add(new Label(notebook.getSummary()));
        });

        NOTEBOOKS.setAll(nbNames);
    }
    
    @FXML
    void addNotebook(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Create a new notebook");
        dialog.setTitle("Create notebook");
        dialog.setContentText("Please enter the notebook name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            NoteRepository repo = new NoteRepository();

            FXNotebook notebook = new NoteFactory(ToolbarController.getSelectedAccount()).newNotebook(name);
            repo.createNotebook(notebook);
            
            refreshView(ToolbarController.getSelectedAccount());
        });
    }
    
    @FXML
    void deleteNotebook(ActionEvent event) {
        NoteRepository repo = new NoteRepository();

        List<FXNotebook> notebooks = repo.getNotebooks(ToolbarController.getSelectedAccount());

        if (notebooks.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("No notebooks created");

            alert.showAndWait();
            return;
        }

        List<String> choices = new ArrayList<>(notebooks.size());

        notebooks.stream().forEach((notebook) -> {
            choices.add(notebook.getSummary());
        });

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Delete notebook");
        dialog.setHeaderText(null);
        dialog.setContentText("Select a notebook for deletion:");

        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(name -> {
            FXNotebook book = repo.getNotebookBySummary(ToolbarController.getSelectedAccount(), name);
            
            repo.deleteNotebook(book);

            refreshView(ToolbarController.getSelectedAccount());
        });
    }
}
