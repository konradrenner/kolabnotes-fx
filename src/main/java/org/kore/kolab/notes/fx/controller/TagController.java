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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class TagController implements Initializable {
    
    @FXML
    private FlowPane tagPane;
    
    private static ObservableList<Node> TAGS;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TAGS = tagPane.getChildren();
    }

    public final static void refreshView(String accountId) {
        TagRepository repo = new TagRepository();
        
        TAGS.removeAll();
        
        repo.getTags(accountId).stream().forEach((tag) -> {
            TAGS.add(new Label(tag.getSummary()));
        });
    }
    
    @FXML
    void addTag(ActionEvent event) {
        String accountId = ToolbarController.getSelectedAccount();
        createTag(accountId);
        
        refreshView(accountId);
    }
    
    @FXML
    void chooseTagColor() {
        String accountId = ToolbarController.getSelectedAccount();
        final TagRepository repo = new TagRepository();
        final List<FXTag> tags = repo.getTags(accountId);

        if (tags.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setContentText("There are no tags for changing the color");

            alert.showAndWait();
        } else {
            Optional<Pair<String, Color>> selection = changeTagColor(tags, accountId);
            
            selection.ifPresent(pair -> {
                
                for (FXTag tag : tags) {
                    if (tag.getSummary().equals(pair.getKey())) {
                        if (pair.getValue().equals(Color.TRANSPARENT)) {
                            tag.setColor(null);
                        } else {
                            tag.setColor("#" + Integer.toHexString(pair.getValue().hashCode()));
                        }
                        
                        repo.updateTag(tag);
                        break;
                    }
                }
                
                refreshView(accountId);
            });
        }
    }

    private Optional<Pair<String, Color>> changeTagColor(final List<FXTag> tags, String accountId) {
        ChoiceBox tagsForSelection = new ChoiceBox();
        
        ObservableList<String> observableTags = FXCollections.observableArrayList();
        tags.stream().forEach((tag) -> {
            observableTags.add(tag.getSummary());
        });
        tagsForSelection.setItems(observableTags);
        tagsForSelection.getSelectionModel().select(0);

        Dialog<Pair<String, Color>> dialog = new Dialog<>();
        dialog.setTitle("Choose Color");
        dialog.setHeaderText("Choose Tag color");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        final ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(tagsForSelection, 1, 0);
        grid.add(new Label(""), 0, 1);
        grid.add(colorPicker, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        loginButton.setDisable(true);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> tagsForSelection.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(tagsForSelection.getSelectionModel().getSelectedItem().toString(), colorPicker.getValue());
            }
            return null;
        });

        tagsForSelection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                for (FXTag tag : tags) {
                    if (tag.getSummary().equals(newValue)) {
                        if (tag.getColor() == null) {
                            colorPicker.setValue(Color.TRANSPARENT);
                        } else {
                            colorPicker.setValue(Color.web(tag.getColor()));
                        }
                        break;
                    }
                }
            }
        });
        
        return dialog.showAndWait();
    }

    private void createTag(String accountId) {
        Dialog<Pair<String, Color>> dialog = new Dialog<>();
        dialog.setTitle("Create Tag");
        dialog.setHeaderText("Create new Tag");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField tagname = new TextField();
        tagname.setPromptText("Name");
        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(tagname, 1, 0);
        grid.add(new Label(""), 0, 1);
        grid.add(colorPicker, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        loginButton.setDisable(true);

        tagname.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> tagname.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(tagname.getText(), colorPicker.getValue());
            }
            return null;
        });

        Optional<Pair<String, Color>> result = dialog.showAndWait();

        result.ifPresent(newTag -> {
            String name = newTag.getKey();
            Color color = newTag.getValue();

            String hex;
            if (Color.TRANSPARENT.equals(color)) {
                hex = null;
            } else {
                hex = "#" + Integer.toHexString(color.hashCode());
            }

            TagRepository repo = new TagRepository();
            FXTag fxtag = new FXTag(accountId, UUID.randomUUID().toString());
            fxtag.setSummary(name);
            fxtag.setColor(hex);
            repo.createTag(fxtag);
        });
    }
}
