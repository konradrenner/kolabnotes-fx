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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;

/**
 *
 * @author Konrad Renner
 */
public class NoteDetailController implements Initializable{
    
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        noteClassificationChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println(".changed()");
                //TODO
            }
            
        });
    }
    
    public final static void refreshView(String accountId){
        //TODO
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
