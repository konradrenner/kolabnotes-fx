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
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.domain.account.AccountRepository;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagFactory;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 * FXML Controller class
 *
 * @author Konrad Renner
 */
public class ToolbarController implements Initializable, RefreshViewBus.RefreshListener {

    private static String SELECTED_ACCOUNT;
    
    @FXML
    private ChoiceBox accountChoiceBox;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initAccountChoiceBox(new AccountRepository());
        
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
        dialog.setTitle("Create Account");
        dialog.setHeaderText(null);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("Account name"), 0, 0);
        TextField accountName = new TextField();
        grid.add(accountName, 1, 0);
        grid.add(new Label("Root Folder"), 0, 1);
        TextField rootFolder = new TextField();
        grid.add(rootFolder, 1, 1);
        grid.add(new Label("E-Mail"), 0, 2);
        TextField mail = new TextField();
        grid.add(mail, 1, 2);
        grid.add(new Label("Password"), 0, 3);
        PasswordField password = new PasswordField();
        grid.add(password, 1, 3);
        CheckBox kolab = new CheckBox("Enable Kolabextensions");
        grid.add(kolab, 0, 4);
        CheckBox ssl = new CheckBox("Enable SSL");
        grid.add(ssl, 0, 5);
        CheckBox sharedFolders = new CheckBox("Enable Shared folders");
        grid.add(sharedFolders, 0, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Account newAccount = new Account(accountName.getText());
                newAccount.setEmail(mail.getText());
                newAccount.setEnableKolabExtensions(!kolab.isIndeterminate() && kolab.isSelected());
                newAccount.setSslEnabled(!ssl.isIndeterminate() && ssl.isSelected());
                newAccount.setSyncSharedFolders(!sharedFolders.isIndeterminate() && sharedFolders.isSelected());
                newAccount.setRootFolder(rootFolder.getText());
                newAccount.setPassword(password.getText());

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
    void syncNow(ActionEvent event){
        //TODO
        System.out.println("org.kore.kolab.notes.fx.controller.ToolbarController.syncNow()");

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(getSelectedAccount(), null, RefreshViewBus.RefreshTypes.SYNCED_ACCOUNT);
        RefreshViewBus.informListener(refreshEvent);
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

    private String createTag(String accountId) {
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
