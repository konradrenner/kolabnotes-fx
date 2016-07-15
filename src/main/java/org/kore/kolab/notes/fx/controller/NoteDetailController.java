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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import org.kore.kolab.notes.Note;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class NoteDetailController implements Initializable, RefreshViewBus.RefreshListener {
    
    @FXML
    private ColorPicker noteColorPicker;
    
    @FXML
    private ChoiceBox noteClassificationChoiceBox;
    
    @FXML
    private HTMLEditor noteEditor;
    
    @FXML
    private HBox tagBox;
    
    @FXML
    private TextField summaryTextField;

    private String noteUID;
    private String accountId;

    private List<FXTag> selectedTags;

    private ResourceBundle bundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToBus();
        bundle = resources;
        noteClassificationChoiceBox.setItems(FXCollections.observableArrayList(Note.Classification.values()));
        setEmpty();
    }

    private void subscribeToBus() {
        RefreshViewBus.subscribe(this, RefreshViewBus.RefreshTypes.CHANGE_ACCOUNT,
                RefreshViewBus.RefreshTypes.SYNCED_ACCOUNT,
                RefreshViewBus.RefreshTypes.NEW_ACCOUNT,
                RefreshViewBus.RefreshTypes.DELETED_NOTEBOOK,
                RefreshViewBus.RefreshTypes.EDITED_NOTE,
                RefreshViewBus.RefreshTypes.NEW_NOTE,
                RefreshViewBus.RefreshTypes.DELETED_NOTE,
                RefreshViewBus.RefreshTypes.NEW_NOTEBOOK,
                RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK,
                RefreshViewBus.RefreshTypes.SELECTED_NOTE,
                RefreshViewBus.RefreshTypes.SELECTED_TAG);
    }

    @Override
    public void refreshRequest(RefreshViewBus.RefreshEvent event) {
        accountId = event.getActiveAccount();
        switch (event.getType()) {
            case NEW_NOTE:
                setValues(event.getObjectId());
                break;
            case SELECTED_NOTE:
                setValues(event.getObjectId());
                break;
            default:
                setEmpty();
                break;
        }
    }

    private void setEmpty() {
        noteUID = null;
        this.summaryTextField.setText("");
        this.noteEditor.setHtmlText("");
        this.noteClassificationChoiceBox.getSelectionModel().select(Note.Classification.PUBLIC);
        this.noteColorPicker.setValue(Color.TRANSPARENT);
        this.selectedTags = null;
        setTags(Collections.EMPTY_LIST);
    }

    private void setValues(String noteUID) {
        this.noteUID = noteUID;
        NoteRepository repo = new NoteRepository();
        FXNote note = repo.getNote(this.noteUID);
        this.summaryTextField.setText(note.getSummary());
        this.noteEditor.setHtmlText(note.getDescription() == null ? "" : note.getDescription());
        this.noteClassificationChoiceBox.getSelectionModel().select(note.getClassification());
        
        if (note.getColor() == null) {
            this.noteColorPicker.setValue(Color.TRANSPARENT);
        } else {
            this.noteColorPicker.setValue(Color.web(note.getColor()));
        }
        
        setTags(note.getTags());
    }

    private void setTags(List<FXTag> tags) {
        List<Node> uiTags = new ArrayList<>();

        for (FXTag tag : tags) {
            Text text = new Text(tag.getSummary());

            TextFlow flow = new TextFlow(text);
            flow.setPadding(new Insets(10));
            uiTags.add(flow);
        }

        this.tagBox.getChildren().setAll(uiTags);
    }

    @Override
    public String getId() {
        return "NoteDetailController";
    }

    
    @FXML
    void saveNote(ActionEvent event){
        if (noteUID == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(bundle.getString("error"));
            alert.setHeaderText(bundle.getString("chooseNote"));
            alert.showAndWait();
            return;
        }
        NoteRepository repo = new NoteRepository();
        FXNote note = repo.getNote(this.noteUID);
        
        note.setSummary(this.summaryTextField.getText());
        note.setDescription(this.noteEditor.getHtmlText());
        note.setClassification((Note.Classification) this.noteClassificationChoiceBox.getSelectionModel().getSelectedItem());
        Color color = this.noteColorPicker.getValue();
        if (color.equals(Color.TRANSPARENT)) {
            note.setColor(null);
        } else {
            note.setColor("#" + Integer.toHexString(color.hashCode()));
        }
        
        if (selectedTags != null) {
            note.removeTags(note.getTags());
            note.attachTags(selectedTags);
        }
        
        repo.updateNote(note);

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), note.getId(), RefreshViewBus.RefreshTypes.EDITED_NOTE);
        RefreshViewBus.informListener(refreshEvent);
    }
    
    @FXML
    void editTags(ActionEvent event) {
        if (noteUID == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(bundle.getString("error"));
            alert.setHeaderText(bundle.getString("chooseNote"));
            alert.showAndWait();
            return;
        }

        Dialog<List<FXTag>> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("chooseTags"));
        dialog.setHeaderText(bundle.getString("chooseTags"));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        List<FXTag> tagsFroPreSelection;
        if (selectedTags == null) {
            tagsFroPreSelection = new NoteRepository().getNote(noteUID).getTags();
        } else {
            tagsFroPreSelection = selectedTags;
        }

        TagRepository tagRepo = new TagRepository();

        VBox checkboxes = createCheckBoxesForEditTags(tagRepo, tagsFroPreSelection);

        dialog.getDialogPane().setContent(checkboxes);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                List<FXTag> selectedTagNames = new ArrayList<>();
                List<Node> nodes = checkboxes.getChildren();

                for (int i = 1; i < nodes.size(); i++) {
                    CheckBox box = (CheckBox) nodes.get(i);

                    if (box.isSelected() && !box.isIndeterminate()) {
                        selectedTagNames.add(tagRepo.getTagByName(accountId, box.getText()).get());
                    }
                }

                return selectedTagNames;
            }
            return null;
        });

        selectedTags = dialog.showAndWait().orElse(selectedTags);
        
        if (selectedTags != null) {
            setTags(selectedTags);
        }
    }
    
    @FXML
    void deleteNote(ActionEvent event) {
        if (noteUID == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(bundle.getString("error"));
            alert.setHeaderText(bundle.getString("chooseNote"));
            alert.showAndWait();
            return;
        }

        NoteRepository noteRepository = new NoteRepository();
        FXNote note = noteRepository.getNote(noteUID);
        noteRepository.deleteNote(note);

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), note.getId(), RefreshViewBus.RefreshTypes.DELETED_NOTE);
        RefreshViewBus.informListener(refreshEvent);
    }

    @FXML
    void openAttachments(ActionEvent event) {
        if (noteUID == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(bundle.getString("error"));
            alert.setHeaderText(bundle.getString("chooseNote"));
            alert.showAndWait();
            return;
        }

        new AttachmentDialog(this.accountId, this.noteUID, this.bundle, noteEditor.getScene().getWindow()).showAndWait();
    }

    private VBox createCheckBoxesForEditTags(TagRepository tagRepo, List<FXTag> selectedTags) {
        List<FXTag> tags = tagRepo.getTags(accountId);
        Node[] nodes = new Node[tags.size() + 1];

        nodes[0] = new Label(bundle.getString("choose"));

        for (int i = 0; i < tags.size(); i++) {
            CheckBox box = new CheckBox(tags.get(i).getSummary());
            box.setPadding(new Insets(5));
            for (FXTag tag : selectedTags) {
                if (tag.equals(tags.get(i))) {
                    box.setSelected(true);
                    break;
                }
            }

            nodes[i + 1] = box;
        }
        
        VBox vBox = new VBox(nodes);
        return vBox;
    }
}
