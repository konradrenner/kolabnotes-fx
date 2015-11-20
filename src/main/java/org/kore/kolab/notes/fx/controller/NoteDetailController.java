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
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;

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
        //TODO
        noteUID = null;
    }

    private void setValues(String noteUID) {
        //TODO
        this.noteUID = noteUID;
        NoteRepository repo = new NoteRepository();
        FXNote note = repo.getNote(this.noteUID);
        this.summaryTextField.setText(note.getSummary());
    }

    @Override
    public String getId() {
        return "NoteDetailController";
    }

    
    @FXML
    void saveNote(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.NoteDetailController.saveNote()");
        //TODO
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
