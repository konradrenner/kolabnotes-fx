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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.domain.account.AccountRepository;
import org.kore.kolab.notes.fx.sync.SyncService;

/**
 * FXML Controller class
 *
 * @author Konrad Renner
 */
public class ToolbarController implements Initializable, RefreshViewBus.RefreshListener {

    private static String SELECTED_ACCOUNT;
    
    @FXML
    private ChoiceBox accountChoiceBox;
    
    private ResourceBundle bundle;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initAccountChoiceBox(new AccountRepository());
        bundle = resources;
        
        accountChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                SELECTED_ACCOUNT = newValue;

                AccountRepository repo = new AccountRepository();

                Account newAccount = repo.getAccount(newValue).get();
                newAccount.setIsActive(true);

                Optional<Account> oldOptional = repo.getAccount(oldValue);
                Account[] accountsToUpdate;
                if (oldOptional.isPresent()) {
                    Account oldAccount = repo.getAccount(oldValue).get();
                    oldAccount.setIsActive(false);

                    accountsToUpdate = new Account[]{newAccount, oldAccount};
                } else {
                    accountsToUpdate = new Account[]{newAccount};
                }

                repo.updateAccount(accountsToUpdate);

                RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(getSelectedAccount(), null, RefreshViewBus.RefreshTypes.CHANGE_ACCOUNT);
                RefreshViewBus.informListener(refreshEvent);
            }
            
        });

        RefreshViewBus.subscribe(this, RefreshViewBus.RefreshTypes.NEW_ACCOUNT, RefreshViewBus.RefreshTypes.DELETED_ACCOUNT);
    }

    @Override
    public String getId() {
        return "ToolbarController";
    }

    @Override
    public void refreshRequest(RefreshViewBus.RefreshEvent event) {
        if (event.getType() == RefreshViewBus.RefreshTypes.NEW_ACCOUNT) {
            accountChoiceBox.getItems().add(event.getObjectId());
            accountChoiceBox.getSelectionModel().select(event.getObjectId());
        } else if (event.getType() == RefreshViewBus.RefreshTypes.DELETED_ACCOUNT) {
            accountChoiceBox.getItems().remove(event.getObjectId());
            accountChoiceBox.getSelectionModel().select(0);
        }
    }

    void initAccountChoiceBox(AccountRepository repository){
        List<Account> accounts = repository.getAccounts();
        
        ObservableList<String> accountNames = FXCollections.observableArrayList();

        int selection = 0;
        int i = 0;
        for (Account account : accounts) {
            accountNames.add(account.getId());
            if (account.isIsActive()) {
                selection = i;
            }
            i++;
        }

        accountChoiceBox.setItems(accountNames);
        accountChoiceBox.getSelectionModel().select(selection);
        SELECTED_ACCOUNT = accountChoiceBox.getSelectionModel().getSelectedItem().toString();
    }
    
    public final static String getSelectedAccount(){
        return SELECTED_ACCOUNT;
    }
    
    @FXML
    void createAccount(ActionEvent event){
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("createAccount"));
        dialog.setHeaderText(null);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label(bundle.getString("name")), 0, 0);
        TextField accountName = new TextField();
        grid.add(accountName, 1, 0);

        grid.add(new Label(bundle.getString("host")), 0, 1);
        TextField host = new TextField();
        grid.add(host, 1, 1);

        grid.add(new Label(bundle.getString("port")), 0, 2);
        TextField port = new TextField();
        grid.add(port, 1, 2);

        grid.add(new Label(bundle.getString("rootFolder")), 0, 3);
        TextField rootFolder = new TextField();
        grid.add(rootFolder, 1, 3);

        grid.add(new Label(bundle.getString("mail")), 0, 4);
        TextField mail = new TextField();
        grid.add(mail, 1, 4);

        CheckBox kolab = new CheckBox(bundle.getString("enableKolab"));
        kolab.setSelected(true);
        grid.add(kolab, 0, 6);

        CheckBox ssl = new CheckBox(bundle.getString("enableSSL"));
        grid.add(ssl, 0, 7);

        CheckBox sharedFolders = new CheckBox(bundle.getString("enableSharedFolders"));
        grid.add(sharedFolders, 0, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Account newAccount = new Account(accountName.getText());
                newAccount.setEmail(mail.getText());
                newAccount.setEnableKolabExtensions(!kolab.isIndeterminate() && kolab.isSelected());
                newAccount.setSslEnabled(!ssl.isIndeterminate() && ssl.isSelected());
                newAccount.setSyncSharedFolders(!sharedFolders.isIndeterminate() && sharedFolders.isSelected());
                newAccount.setRootFolder(rootFolder.getText());
                newAccount.setPassword("");
                newAccount.setHost(host.getText());
                newAccount.setPort(Integer.parseInt(port.getText()));

                return newAccount;
            }
            return null;
        });

        Optional<Account> newAccount = dialog.showAndWait();

        if (newAccount.isPresent()) {
            Account account = newAccount.get();
            AccountRepository repo = new AccountRepository();
            repo.createAccount(account);

            RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(getSelectedAccount(), account.getId(), RefreshViewBus.RefreshTypes.NEW_ACCOUNT);
            RefreshViewBus.informListener(refreshEvent);
        }
    }
    
    @FXML
    void editAccount(ActionEvent event){
        //TODO

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(getSelectedAccount(), null, RefreshViewBus.RefreshTypes.EDITED_ACCOUNT);
        RefreshViewBus.informListener(refreshEvent);
    }
    
    @FXML
    void syncNow(ActionEvent event) {
        if ("local".equals(SELECTED_ACCOUNT.trim())) {
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("password"));
        dialog.setHeaderText(null);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label(bundle.getString("password")), 0, 0);
        PasswordField password = new PasswordField();
        grid.add(password, 1, 0);

        Platform.runLater(() -> password.requestFocus());

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return password.getText();
            }
            return null;
        });

        Optional<String> givenPassword = dialog.showAndWait();

        if (givenPassword.isPresent()) {
            new SyncService(new AccountRepository().getAccount(SELECTED_ACCOUNT).get(), givenPassword.get()).start();
        }
    }

    @FXML
    void deleteAccount(ActionEvent event) {
        if ("local".equals(SELECTED_ACCOUNT.trim())) {
            return;
        }

        AccountRepository repo = new AccountRepository();

        Optional<Account> account = repo.getAccount(SELECTED_ACCOUNT);

        repo.deleteAccount(account.get());

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(getSelectedAccount(), SELECTED_ACCOUNT, RefreshViewBus.RefreshTypes.DELETED_ACCOUNT);
        RefreshViewBus.informListener(refreshEvent);
    }
}
