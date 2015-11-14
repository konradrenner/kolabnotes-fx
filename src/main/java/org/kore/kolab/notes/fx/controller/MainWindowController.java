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

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

/**
 *
 * @author Konrad Renner
 */
public class MainWindowController{
    
    @FXML
    private ChoiceBox accountChoiceBox;

    @FXML
    void initialize(URL location, ResourceBundle resources) {
        //TODO
        accountChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println(".changed()");
                //TODO
            }
            
        });
    }
    
    @FXML
    void createAccount(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.MainWindowController.changeAccount()");
    }
    
    @FXML
    void editAccount(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.MainWindowController.changeAccount()");
    }
    
    @FXML
    void syncNow(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.MainWindowController.changeAccount()");
    }
}
