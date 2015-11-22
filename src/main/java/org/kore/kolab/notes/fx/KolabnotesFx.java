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
package org.kore.kolab.notes.fx;

import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kore.kolab.notes.fx.controller.ToolbarController;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.domain.account.AccountRepository;
import org.kore.kolab.notes.fx.domain.account.AccountType;
import org.kore.kolab.notes.fx.domain.account.SyncIntervallType;

/**
 *
 * @author Konrad Renner
 */
public class KolabnotesFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        initLocalAccount(new AccountRepository());
        
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("kolabnotes-fx");
        stage.setMaximized(true);
        stage.show();
        
        RefreshViewBus.informListener(new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), null, RefreshViewBus.RefreshTypes.CHANGE_ACCOUNT));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private void initLocalAccount(AccountRepository accountRepository ){
        Optional<Account> account = new AccountRepository().getAccount("local");
        
        if(!account.isPresent()){
            Account local = new Account("local");
            local.setAccountType(AccountType.LOCAL);
            local.setEmail("local");
            local.setPassword("local");
            local.setRootFolder("Notes");
            local.setHost("localhost");
            local.setSyncIntervallType(SyncIntervallType.NONE);
            
            accountRepository.createAccount(local);
        }
    }
}
