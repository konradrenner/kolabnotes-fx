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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import org.kore.kolab.notes.Note;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;
import org.kore.kolab.notes.fx.domain.tag.FXTag;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToBus();
        
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

        setTags(Collections.EMPTY_LIST);
    }

    private void setValues(String noteUID) {
        //TODO
        this.noteUID = noteUID;
        NoteRepository repo = new NoteRepository();
        FXNote note = repo.getNote(this.noteUID);
        this.summaryTextField.setText(note.getSummary());
        this.noteEditor.setHtmlText(note.getDescription());
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
        tags.stream().map((tag) -> new Text(tag.getSummary())).map((text) -> {
            text.setTextAlignment(TextAlignment.CENTER);
            return text;
        }).map((text) -> new TextFlow(text)).map((flow) -> {
            flow.setPadding(new Insets(10));
            return flow;
        }).forEach((flow) -> {
            uiTags.add(flow);
        });

        this.tagBox.getChildren().setAll(uiTags);
    }

    @Override
    public String getId() {
        return "NoteDetailController";
    }

    
    @FXML
    void saveNote(ActionEvent event){
        if (noteUID == null) {
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
        
        repo.updateNote(note);
    }
    
    @FXML
    void editTags(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.NoteDetailController.editTags()");
        //TODO
    }
    
    @FXML
    void changeColor(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.NoteDetailController.changeColor()");
        //TODO
    }
}
