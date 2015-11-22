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
package org.kore.kolab.notes.fx.sync;

import java.util.Collection;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kore.kolab.notes.AccountInformation;
import org.kore.kolab.notes.Note;
import org.kore.kolab.notes.Notebook;
import org.kore.kolab.notes.fx.RefreshViewBus;
import static org.kore.kolab.notes.fx.controller.ToolbarController.getSelectedAccount;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.imap.ImapNotesRepository;
import org.kore.kolab.notes.v3.KolabConfigurationParserV3;
import org.kore.kolab.notes.v3.KolabNotesParserV3;

/**
 *
 * @author Konrad Renner
 */
public class SyncService {

    private final Account account;

    public SyncService(Account account) {
        this.account = account;
    }

    public void start() {
        ProgressForm pForm = new ProgressForm();

        // In real life this task would do something useful and return 
        // some meaningful result:
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                AccountInformation.Builder builder = AccountInformation.createForHost(account.getHost()).username(account.getEmail()).password(account.getPassword()).port(account.getPort());

                if (!account.isSslEnabled()) {
                    builder.disableSSL();
                }

                if (!account.isEnableKolabExtensions()) {
                    builder.disableFolderAnnotation();
                }

                if (account.isSyncSharedFolders()) {
                    builder.enableSharedFolders();
                }

                boolean doit = true;
                AccountInformation info = builder.build();
                ImapNotesRepository imapRepository = new ImapNotesRepository(new KolabNotesParserV3(), info, account.getRootFolder(), new KolabConfigurationParserV3());

                Collection<Notebook> notebooks = imapRepository.getNotebooks();
                for (Notebook notebook : notebooks) {
                    System.out.println(notebook.getSummary());
                    for (Note note : notebook.getNotes()) {
                        System.out.println(note.getSummary());
                    }
                }
                for (int i = 0; i < 10; i++) {
                    updateProgress(i, 10);
                    Thread.sleep(200);
                }
                updateProgress(10, 10);
                return null;
            }
        };

        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
            RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(getSelectedAccount(), account.getId(), RefreshViewBus.RefreshTypes.SYNCED_ACCOUNT);
            RefreshViewBus.informListener(refreshEvent);
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();
    }

    public static class ProgressForm {

        private final Stage dialogStage;
        private final ProgressBar pb = new ProgressBar();
        private final ProgressIndicator pin = new ProgressIndicator();

        public ProgressForm() {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // PROGRESS BAR
            final Label label = new Label();
            label.setText("alerto");

            pb.setProgress(-1F);
            pin.setProgress(-1F);

            final HBox hb = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(pb, pin);

            Scene scene = new Scene(hb);
            dialogStage.setScene(scene);
        }

        public void activateProgressBar(final Task<?> task) {
            pb.progressProperty().bind(task.progressProperty());
            pin.progressProperty().bind(task.progressProperty());
            dialogStage.show();
        }

        public Stage getDialogStage() {
            return dialogStage;
        }
    }
}
