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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagFactory;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class TagController implements Initializable, RefreshViewBus.RefreshListener {
    
    @FXML
    private FlowPane tagPane;

    private ResourceBundle bundle;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToBus();
        bundle = resources;
    }

    private void subscribeToBus() {
        RefreshViewBus.subscribe(this, RefreshViewBus.RefreshTypes.CHANGE_ACCOUNT,
                RefreshViewBus.RefreshTypes.SYNCED_ACCOUNT,
                RefreshViewBus.RefreshTypes.NEW_ACCOUNT,
                RefreshViewBus.RefreshTypes.EDITED_TAG,
                RefreshViewBus.RefreshTypes.NEW_TAG);
    }

    @Override
    public void refreshRequest(RefreshViewBus.RefreshEvent event) {
        TagRepository repo = new TagRepository();

        List<FXTag> notebooks = repo.getTags(event.getActiveAccount());

        ArrayList<Node> tagNames = new ArrayList<>(notebooks.size());
        notebooks.stream().forEach((tag) -> {

            Text text = new Text(tag.getSummary());
            text.getStyleClass().add("list_tag");
            text.setTextAlignment(TextAlignment.CENTER);
            text.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Parent textFlow = text.getParent();

                    for (Node node : textFlow.getParent().getChildrenUnmodifiable()) {
                        node.getStyleClass().remove("selected_tag");
                    }

                    textFlow.getStyleClass().add("selected_tag");

                    RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), tag.getId(), RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK);
                    RefreshViewBus.informListener(refreshEvent);
                }

            });
            if (tag.getColor() != null) {
                text.setFill(Color.web(tag.getColor()));
            }
            
            TextFlow flow = new TextFlow(text);
            flow.setPadding(new Insets(5));
            tagNames.add(flow);
        });

        tagPane.getChildren().setAll(tagNames);
    }

    @Override
    public String getId() {
        return "TagController";
    }

    @FXML
    void addTag(ActionEvent event) {
        String accountId = ToolbarController.getSelectedAccount();
        String uid = createTag(accountId);

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), uid, RefreshViewBus.RefreshTypes.NEW_TAG);
        RefreshViewBus.informListener(refreshEvent);
    }
    
    @FXML
    void chooseTagColor() {
        String accountId = ToolbarController.getSelectedAccount();
        final TagRepository repo = new TagRepository();
        final List<FXTag> tags = repo.getTags(accountId);

        if (tags.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(bundle.getString("warning"));
            alert.setHeaderText(null);
            alert.setContentText(bundle.getString("noTagsForColorChange"));

            alert.showAndWait();
        } else {
            Optional<Pair<String, Color>> selection = changeTagColor(tags, accountId);
            
            selection.ifPresent(pair -> {

                String uid = null;
                for (FXTag tag : tags) {
                    if (tag.getSummary().equals(pair.getKey())) {
                        if (pair.getValue().equals(Color.TRANSPARENT)) {
                            tag.setColor(null);
                        } else {
                            tag.setColor("#" + Integer.toHexString(pair.getValue().hashCode()));
                        }
                        
                        repo.updateTag(tag);
                        uid = tag.getId();
                        break;
                    }
                }
                
                RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), uid, RefreshViewBus.RefreshTypes.EDITED_TAG);
                RefreshViewBus.informListener(refreshEvent);
            });
        }
    }

    private Optional<Pair<String, Color>> changeTagColor(final List<FXTag> tags, String accountId) {
        if (tags.isEmpty()) {
            return Optional.empty();
        }

        ChoiceBox tagsForSelection = new ChoiceBox();
        
        ObservableList<String> observableTags = FXCollections.observableArrayList();
        tags.stream().forEach((tag) -> {
            observableTags.add(tag.getSummary());
        });
        tagsForSelection.setItems(observableTags);
        tagsForSelection.getSelectionModel().select(0);

        Dialog<Pair<String, Color>> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("chooseTagColor"));
        dialog.setHeaderText(bundle.getString("chooseTagColor"));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        Color initColor;
        if (tags.get(0).getColor() == null) {
            initColor = Color.TRANSPARENT;
        } else {
            initColor = Color.web(tags.get(0).getColor());
        }

        final ColorPicker colorPicker = new ColorPicker(initColor);

        grid.add(new Label(bundle.getString("name")), 0, 0);
        grid.add(tagsForSelection, 1, 0);
        grid.add(new Label(""), 0, 1);
        grid.add(colorPicker, 1, 1);

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

    private String createTag(String accountId) {
        Dialog<Pair<String, Color>> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("createTag"));
        dialog.setHeaderText(bundle.getString("createTag"));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField tagname = new TextField();
        tagname.setPromptText(bundle.getString("name"));
        ColorPicker colorPicker = new ColorPicker(Color.TRANSPARENT);

        grid.add(new Label(bundle.getString("name")), 0, 0);
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

        final StringBuilder sb = new StringBuilder();
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
            FXTag fxtag = new TagFactory(accountId).newTag(name);
            fxtag.setColor(hex);
            repo.createTag(fxtag);

            sb.append(fxtag.getId());
        });

        return sb.toString();
    }
}
