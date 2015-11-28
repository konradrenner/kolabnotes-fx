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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
import javax.persistence.EntityManager;
import org.kore.kolab.notes.AccountInformation;
import org.kore.kolab.notes.AuditInformation;
import org.kore.kolab.notes.Colors;
import org.kore.kolab.notes.Identification;
import org.kore.kolab.notes.Note;
import org.kore.kolab.notes.Notebook;
import org.kore.kolab.notes.Tag;
import org.kore.kolab.notes.fx.RefreshViewBus;
import static org.kore.kolab.notes.fx.controller.ToolbarController.getSelectedAccount;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;
import org.kore.kolab.notes.fx.persistence.PersistenceManager;
import org.kore.kolab.notes.imap.ImapNotesRepository;
import org.kore.kolab.notes.imap.RemoteTags;
import org.kore.kolab.notes.v3.KolabConfigurationParserV3;
import org.kore.kolab.notes.v3.KolabNotesParserV3;

/**
 *
 * @author Konrad Renner
 */
public class SyncService {

    private final Account account;
    private final String password;

    public SyncService(Account account, String password) {
        this.account = account;
        this.password = password;
    }

    public void start() {
        ProgressForm pForm = new ProgressForm();

        // In real life this task would do something useful and return 
        // some meaningful result:
        Task<Void> task;
        task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                AccountInformation.Builder builder = AccountInformation.createForHost(account.getHost()).username(account.getEmail()).password(password).port(account.getPort());

                if (!account.isSslEnabled()) {
                    builder.disableSSL();
                }

                if (!account.isEnableKolabExtensions()) {
                    builder.disableFolderAnnotation();
                }

                if (account.isSyncSharedFolders()) {
                    builder.enableSharedFolders();
                }

                AccountInformation info = builder.build();
                ImapNotesRepository imapRepository = new ImapNotesRepository(new KolabNotesParserV3(), info, account.getRootFolder(), new KolabConfigurationParserV3());
                updateProgress(1, 10);

                TagRepository tagRepo = new TagRepository();
                NoteRepository noteRepo = new NoteRepository();

                List<FXTag> localTags = tagRepo.getTags(account.getId());
                List<FXNotebook> localNotebooks = noteRepo.getNotebooks(account.getId());
                updateProgress(2, 10);

                Collection<Notebook> remoteNotebooks = imapRepository.getNotebooks();
                updateProgress(3, 10);
                Set<RemoteTags.TagDetails> remoteTags = imapRepository.getRemoteTags().getTags();
                updateProgress(4, 10);

                EntityManager entityManager = PersistenceManager.createEntityManager();
                entityManager.getTransaction().begin();
                try {
                    syncLocalTagChanges(localTags, remoteTags, entityManager);
                    updateProgress(5, 10);
                    syncRemoteTagChanges(localTags, remoteTags, entityManager);
                    updateProgress(6, 10);

                    syncLocalNotebooks(localNotebooks, remoteNotebooks, entityManager, imapRepository);
                    updateProgress(7, 10);

                    syncRemoteNotebooks(remoteNotebooks, localNotebooks, entityManager, imapRepository);
                    updateProgress(8, 10);

                    imapRepository.merge();
                    updateProgress(9, 10);
                    entityManager.getTransaction().commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    entityManager.getTransaction().rollback();
                } finally {
                    entityManager.close();
                    updateProgress(10, 10);
                }

                return null;
            }

            public void syncRemoteNotebooks(Collection<Notebook> remoteNotebooks, List<FXNotebook> localNotebooks, EntityManager entityManager, ImapNotesRepository imapRepository) {
                for (Notebook remoteNotebook : remoteNotebooks) {
                    boolean existed = false;
                    for (FXNotebook localNotebook : localNotebooks) {
                        if (localNotebook.getSummary().equals(remoteNotebook.getSummary())) {

                            syncLocalNotes(localNotebook, remoteNotebook, entityManager, imapRepository);

                            syncRemoteNotes(remoteNotebook, localNotebook, entityManager);

                            existed = true;
                        }
                    }

                    if (!existed) {
                        FXNotebook newBook = new FXNotebook(account.getId(), remoteNotebook.getIdentification().getUid());
                        newBook.setSummary(remoteNotebook.getSummary());
                        newBook.setCreationDate(remoteNotebook.getAuditInformation().getCreationDate());
                        newBook.setModificationDate(remoteNotebook.getAuditInformation().getLastModificationDate());
                        newBook.setProductId(remoteNotebook.getIdentification().getProductId());
                        syncRemoteNotes(remoteNotebook, newBook, entityManager);
                        entityManager.persist(newBook);
                    }
                }
            }

            public void syncLocalNotebooks(List<FXNotebook> localNotebooks, Collection<Notebook> remoteNotebooks, EntityManager entityManager, ImapNotesRepository imapRepository) {
                for (FXNotebook localNotebook : localNotebooks) {
                    boolean existed = false;
                    for (Notebook remoteNotebook : remoteNotebooks) {
                        if (localNotebook.getSummary().equals(remoteNotebook.getSummary())) {

                            syncLocalNotes(localNotebook, remoteNotebook, entityManager, imapRepository);

                            syncRemoteNotes(remoteNotebook, localNotebook, entityManager);

                            existed = true;
                        }
                    }

                    if (!existed && localNotebook.getModificationDate().getTime() <= account.getLastSync()) {
                        entityManager.remove(localNotebook);
                    } else if (!existed && localNotebook.getModificationDate().getTime() > account.getLastSync()) {
                        Notebook remoteNotebook = imapRepository.createNotebook(localNotebook.getId(), localNotebook.getSummary());
                        syncLocalNotes(localNotebook, remoteNotebook, entityManager, imapRepository);
                    }
                }
            }

            public void syncRemoteNotes(Notebook remoteNotebook, FXNotebook localNotebook, EntityManager em) {
                for (Note remoteNote : remoteNotebook.getNotes()) {
                    boolean noteExisted = false;
                    for (FXNote localNote : localNotebook.getNotes()) {
                        if (remoteNote.getIdentification().getUid().equals(localNote.getId())) {
                            noteExisted = true;

                            if (localNote.getModificationDate().getTime() < remoteNote.getAuditInformation().getLastModificationDate().getTime()) {
                                setLocalNote(localNote, remoteNote, localNotebook);
                                em.merge(localNote);
                            }
                        }
                    }

                    if (!noteExisted && remoteNote.getAuditInformation().getLastModificationDate().getTime() > account.getLastSync()) {
                        FXNote newNote = new FXNote(account.getId(), remoteNote.getIdentification().getUid());
                        setLocalNote(newNote, remoteNote, localNotebook);
                        em.persist(newNote);
                    }
                }
            }

            public void setLocalNote(FXNote localNote, Note remoteNote, FXNotebook localBook) {
                localNote.setProductId(remoteNote.getIdentification().getProductId());
                localNote.setDescription(remoteNote.getDescription());
                localNote.setSummary(remoteNote.getSummary());
                localNote.setCreationDate(remoteNote.getAuditInformation().getCreationDate());
                localNote.setModificationDate(remoteNote.getAuditInformation().getLastModificationDate());
                localNote.setNotebook(localBook);
                localNote.setClassification(remoteNote.getClassification());
                if (remoteNote.getColor() == null) {
                    localNote.setColor(null);
                } else {
                    localNote.setColor(remoteNote.getColor().getHexcode());
                }
                
                localNote.removeTags(localNote.getTags());

                ArrayList<FXTag> newTags = new ArrayList<>();
                for (Tag remoteTag : remoteNote.getCategories()) {
                    FXTag localTag = new FXTag(account.getId(), remoteTag.getIdentification().getUid());
                    setLocalTag(localTag, remoteTag);
                    newTags.add(localTag);
                }
                localNote.attachTags(newTags);
            }

            public void syncLocalNotes(FXNotebook localNotebook, Notebook remoteNotebook, EntityManager entityManager, ImapNotesRepository imapRepository) {
                for (FXNote localNote : localNotebook.getNotes()) {
                    boolean noteExisted = false;

                    for (Note remoteNote : remoteNotebook.getNotes()) {

                        if (localNote.getId().equals(remoteNote.getIdentification().getUid())) {
                            noteExisted = true;
                            if (localNote.getModificationDate().getTime() > remoteNote.getAuditInformation().getLastModificationDate().getTime()) {
                                setRemoteNote(remoteNote, localNote);
                            }
                        }
                    }

                    //deleted on server
                    if (!noteExisted && localNote.getModificationDate().getTime() <= account.getLastSync()) {
                        entityManager.remove(localNote);
                    } else if (!noteExisted && localNote.getModificationDate().getTime() > account.getLastSync()) {
                        Identification ident = new Identification(localNote.getId(), localNote.getProductId());
                        AuditInformation audit = new AuditInformation(localNote.getCreationDate(), localNote.getModificationDate());
                        Note newNote = new Note(ident, audit, localNote.getClassification(), localNote.getSummary());
                        Tag[] setRemoteNote = setRemoteNote(newNote, localNote);
                        remoteNotebook.addNote(newNote);
                        //note is not observed, so add manually
                        imapRepository.getRemoteTags().attachTags(localNote.getId(), setRemoteNote);
                    }
                }
            }

            public Tag[] setRemoteNote(Note remoteNote, FXNote localNote) {
                remoteNote.setSummary(localNote.getSummary());
                remoteNote.setColor(Colors.getColor(localNote.getColor()));
                remoteNote.setDescription(localNote.getDescription());
                remoteNote.setClassification(localNote.getClassification());
                
                Set<Tag> categories = remoteNote.getCategories();
                remoteNote.removeCategories(categories.toArray(new Tag[categories.size()]));

                List<FXTag> noteTags = localNote.getTags();
                Tag[] tags = new Tag[noteTags.size()];
                for (int i = 0; i < noteTags.size(); i++) {
                    Identification ident = new Identification(noteTags.get(i).getId(), noteTags.get(i).getProductId());
                    AuditInformation audit = new AuditInformation(noteTags.get(i).getCreationDate(), noteTags.get(i).getModificationDate());
                    Tag tag = new Tag(ident, audit);
                    tag.setColor(Colors.getColor(noteTags.get(i).getColor()));
                    tag.setName(noteTags.get(i).getSummary());
                    tags[i] = tag;
                }
                remoteNote.addCategories(tags);

                return tags;
            }

            public void syncLocalTagChanges(List<FXTag> localTags, Set<RemoteTags.TagDetails> remoteTags, EntityManager entityManager) {
                for (FXTag localTag : localTags) {
                    boolean tagExisted = false;
                    for (RemoteTags.TagDetails remoteTag : remoteTags) {
                        if (localTag.getId().equals(remoteTag.getIdentification().getUid())) {
                            if (localTag.getModificationDate().getTime() > remoteTag.getAuditInformation().getLastModificationDate().getTime()) {
                                remoteTag.getAuditInformation().setLastModificationDate(localTag.getModificationDate().getTime());
                                remoteTag.getTag().setColor(Colors.getColor(localTag.getColor()));
                            }
                            tagExisted = true;
                        }
                    }

                    //Tag got deleted on server
                    if (!tagExisted && localTag.getModificationDate().getTime() <= account.getLastSync()) {
                        entityManager.remove(localTag);
                    }
                }
            }

            public void syncRemoteTagChanges(List<FXTag> localTags, Set<RemoteTags.TagDetails> remoteTags, EntityManager entityManager) {
                for (RemoteTags.TagDetails remoteTag : remoteTags) {
                    boolean tagExisted = false;
                    for (FXTag localTag : localTags) {
                        if (localTag.getId().equals(remoteTag.getIdentification().getUid())) {
                            if (localTag.getModificationDate().getTime() <= remoteTag.getAuditInformation().getLastModificationDate().getTime()) {
                                localTag.setModificationDate(remoteTag.getAuditInformation().getLastModificationDate());
                                if (remoteTag.getTag().getColor() == null) {
                                    localTag.setColor(null);
                                } else {
                                    localTag.setColor(remoteTag.getTag().getColor().getHexcode());
                                }
                            }
                            tagExisted = true;
                            entityManager.merge(localTag);
                        }
                    }

                    //Tag got created on server
                    if (!tagExisted && remoteTag.getAuditInformation().getLastModificationDate().getTime() > account.getLastSync()) {
                        FXTag localTag = new FXTag(account.getId(), remoteTag.getIdentification().getUid());
                        setLocalTag(localTag, remoteTag);

                        entityManager.persist(localTag);
                    }
                }
            }

            public void setLocalTag(FXTag localTag, RemoteTags.TagDetails remoteTag) {
                localTag.setProductId(remoteTag.getIdentification().getProductId());
                if (remoteTag.getTag().getColor() == null) {
                    localTag.setColor(null);
                } else {
                    localTag.setColor(remoteTag.getTag().getColor().getHexcode());
                }
                localTag.setCreationDate(remoteTag.getAuditInformation().getCreationDate());
                localTag.setModificationDate(remoteTag.getAuditInformation().getLastModificationDate());
                localTag.setSummary(remoteTag.getTag().getName());
            }

            public void setLocalTag(FXTag localTag, Tag remoteTag) {
                localTag.setProductId(remoteTag.getIdentification().getProductId());
                if (remoteTag.getColor() == null) {
                    localTag.setColor(null);
                } else {
                    localTag.setColor(remoteTag.getColor().getHexcode());
                }
                localTag.setCreationDate(remoteTag.getAuditInformation().getCreationDate());
                localTag.setModificationDate(remoteTag.getAuditInformation().getLastModificationDate());
                localTag.setSummary(remoteTag.getName());
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
