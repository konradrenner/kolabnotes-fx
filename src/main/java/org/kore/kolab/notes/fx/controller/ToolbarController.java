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
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.domain.tag.AccountRepository;

/**
 * FXML Controller class
 *
 * @author Konrad Renner
 */
public class ToolbarController implements Initializable{
    
    @FXML
    private ChoiceBox accountChoiceBox;
    
    private static String SELECTED_ACCOUNT;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initAccountChoiceBox(new AccountRepository());
        
        accountChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                SELECTED_ACCOUNT = accountChoiceBox.getSelectionModel().getSelectedItem().toString();
                
                MainWindowController.refreshViews(SELECTED_ACCOUNT);
            }
            
        });
    }

    void initAccountChoiceBox(AccountRepository repository){
        List<Account> accounts = repository.getAccounts();
        
        ObservableList<String> accountNames = FXCollections.observableArrayList();
        
        accounts.stream().forEach((account) -> {
            accountNames.add(account.getId());
        });
        
        accountChoiceBox.setItems(accountNames);
    }
    
    public final static String getSelectedAccount(){
        return SELECTED_ACCOUNT;
    }
    
    @FXML
    void createAccount(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.controller.ToolbarController.createAccount()");
    }
    
    @FXML
    void editAccount(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.controller.ToolbarController.editAccount()");
    }
    
    @FXML
    void syncNow(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.controller.ToolbarController.syncNow()");
    }
}
