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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import org.kore.kolab.notes.Attachment;
import org.kore.kolab.notes.AuditInformation;
import org.kore.kolab.notes.Colors;
import org.kore.kolab.notes.Identification;
import org.kore.kolab.notes.Note;
import org.kore.kolab.notes.Notebook;
import org.kore.kolab.notes.Tag;
import org.kore.kolab.notes.fx.RefreshViewBus;
import static org.kore.kolab.notes.fx.controller.ToolbarController.getSelectedAccount;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.domain.note.FXAttachment;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;
import org.kore.kolab.notes.fx.persistence.DeletedObject;
import org.kore.kolab.notes.fx.persistence.DeletedObjectRepository;
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

                Timestamp lastSync = new Timestamp(account.getLastSync());

                List<FXTag> localTags = tagRepo.getTagsModifiedAfter(account.getId(), lastSync);
                List<FXNotebook> localNotebooks = noteRepo.getNotebooksModifiedAfter(account.getId(), lastSync);
                boolean tagsDirty = tagRepo.anyLocalChanges(account.getId(), lastSync);
                boolean notesDirty = noteRepo.anyLocalChanges(account.getId(), lastSync);
                updateProgress(2, 10);

                Collection<Notebook> remoteNotebooks = imapRepository.getNotebooks();
                updateProgress(3, 10);
                Set<RemoteTags.TagDetails> remoteTags = imapRepository.getRemoteTags().getTags();
                updateProgress(4, 10);

                EntityManager entityManager = PersistenceManager.createEntityManager();
                entityManager.getTransaction().begin();
                try {
                    cleanLocalData(entityManager, account.getId());
                    updateProgress(5, 10);

                    if (tagsDirty) {
                        syncLocalTags(localTags, remoteTags);
                    }
                    updateProgress(6, 10);

                    if (notesDirty) {
                        syncLocalNotebooks(localNotebooks, remoteNotebooks, imapRepository, imapRepository.getRemoteTags(), lastSync);
                    }
                    updateProgress(7, 10);
                    
                    DeletedObjectRepository deletedRepo = new DeletedObjectRepository(entityManager);

                    syncRemoteTagChanges(localTags, remoteTags, entityManager);
                    updateProgress(8, 10);
                    syncRemoteNotebooks(remoteNotebooks, entityManager, imapRepository, deletedRepo.getDeletedObjects(account.getId()));
                    updateProgress(9, 10);
                    
                    imapRepository.merge();
                    
                    account.setLastSync(System.currentTimeMillis());
                    entityManager.merge(account);
                    deletedRepo.clearDeletedObjects(account.getId());
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

            void cleanLocalData(EntityManager em, String accountId) {
                em.createQuery("delete from FXAttachment where accountId='" + accountId + "'").executeUpdate();
                em.createQuery("delete from FXNote where accountId='" + accountId + "'").executeUpdate();
                em.createQuery("delete from FXNotebook where accountId='" + accountId + "'").executeUpdate();
                em.createQuery("delete from FXTag where accountId='" + accountId + "'").executeUpdate();

            }

            void syncLocalNotebooks(Collection<FXNotebook> notebooks, Collection<Notebook> remoteNotebooks, ImapNotesRepository repo, RemoteTags tags, Timestamp lastSync) {
                for (FXNotebook book : notebooks) {
                    boolean notFound = true;
                    for (Notebook remote : remoteNotebooks) {
                        if (remote.getSummary().equals(book.getSummary())) {
                            syncLocalNotes(book, remote, tags, lastSync);
                            notFound = false;
                            break;
                        }
                    }

                    if (notFound && lastSync.before(book.getCreationDate())) {
                        Notebook remote = repo.createNotebook(book.getId(), book.getSummary());
                        syncLocalNotes(book, remote, tags, lastSync);
                    }
                }
            }

            void syncLocalNotes(FXNotebook localBook, Notebook remoteBook, RemoteTags tags, Timestamp lastSync) {
                for (FXNote note : localBook.getNotes()) {
                    if (lastSync.after(note.getModificationDate())) {
                        continue;
                    }

                    Note remoteNote = remoteBook.getNote(note.getId());

                    if (remoteNote == null) {
                        Note createNote = remoteBook.createNote(UUID.randomUUID().toString(), note.getSummary());
                        mapIntoRemoteNote(createNote, note, tags);
                    } else if (note.getModificationDate().after(remoteNote.getAuditInformation().getLastModificationDate())) {
                        mapIntoRemoteNote(remoteNote, note, tags);
                    }
                }
            }

            private void mapIntoRemoteNote(Note remoteNote, FXNote note, RemoteTags remoteTags) {
                remoteNote.setClassification(note.getClassification());
                remoteNote.setColor(Colors.getColor(note.getColor()));
                remoteNote.setDescription(note.getDescription());

                Set<Tag> categories = remoteNote.getCategories();
                remoteNote.removeCategories(categories.toArray(new Tag[categories.size()]));

                List<FXTag> noteTags = note.getTags();
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
                
                remoteTags.removeTags(remoteNote.getIdentification().getUid());
                remoteTags.attachTags(remoteNote.getIdentification().getUid(), tags);
            }

            void syncLocalTags(Collection<FXTag> localTags, Collection<RemoteTags.TagDetails> remoteTags) {
                for (FXTag localTag : localTags) {
                    boolean notFound = true;
                    for (RemoteTags.TagDetails remoteTag : remoteTags) {
                        if (localTag.getId().equals(remoteTag.getIdentification().getUid())) {
                            setRemoteTag(localTag, remoteTag);
                            notFound = false;
                            break;
                        }
                    }
                }
            }

            void setRemoteTag(FXTag local, RemoteTags.TagDetails remote) {
                if (local.getModificationDate().after(remote.getAuditInformation().getLastModificationDate())) {
                    remote.getTag().setColor(Colors.getColor(local.getColor()));
                    remote.getTag().setName(local.getSummary());
                    remote.getTag().setPriority(local.getPriority());
                }
            }


            void syncRemoteNotebooks(Collection<Notebook> remoteNotebooks, EntityManager entityManager, ImapNotesRepository repo, Map<String, DeletedObject> deletions) {
                for (Notebook remoteNotebook : remoteNotebooks) {

                    if (deletions.containsKey(remoteNotebook.getSummary())) {
                        repo.deleteNotebook(remoteNotebook.getIdentification().getUid());
                        continue;
                    }

                    FXNotebook newBook = new FXNotebook(account.getId(), UUID.randomUUID().toString());
                    newBook.setSummary(remoteNotebook.getSummary());
                    newBook.setCreationDate(remoteNotebook.getAuditInformation().getCreationDate());
                    newBook.setModificationDate(remoteNotebook.getAuditInformation().getLastModificationDate());
                    newBook.setProductId(remoteNotebook.getIdentification().getProductId());
                    syncRemoteNotes(remoteNotebook, newBook, entityManager, deletions);
                    entityManager.merge(newBook);
                }
            }

            void syncRemoteNotes(Notebook remoteNotebook, FXNotebook localBook, EntityManager em, Map<String, DeletedObject> deletions) {
                for (Note remoteNote : remoteNotebook.getNotes()) {
                    
                    DeletedObject delObj = deletions.get(remoteNote.getIdentification().getUid());
                    if (delObj != null && delObj.getDeletionTimestamp().after(remoteNote.getAuditInformation().getLastModificationDate())) {
                        remoteNotebook.deleteNote(remoteNote.getIdentification().getUid());
                        continue;
                    }
                    
                    FXNote newNote = new FXNote(account.getId(), remoteNote.getIdentification().getUid());
                    setLocalNote(newNote, remoteNote, localBook);
                    em.merge(newNote);
                }
            }

            void setLocalNote(FXNote localNote, Note remoteNote, FXNotebook localBook) {
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

                Collection<Attachment> attachments = remoteNote.getAttachments();
                for (Attachment att : attachments) {
                    FXAttachment localAtt = new FXAttachment(localNote.getAccountId(), att.getId(), localNote);
                    localAtt.setAttachmentData(att.getData());
                    localAtt.setMimeType(att.getMimeType());

                    localNote.addAttachment(localAtt);
                }
            }

            void syncRemoteTagChanges(List<FXTag> localTags, Set<RemoteTags.TagDetails> remoteTags, EntityManager entityManager) {
                for (RemoteTags.TagDetails remoteTag : remoteTags) {
                    FXTag localTag = new FXTag(account.getId(), remoteTag.getIdentification().getUid());
                    setLocalTag(localTag, remoteTag);
                    entityManager.merge(localTag);
                }
            }

            void setLocalTag(FXTag localTag, RemoteTags.TagDetails remoteTag) {
                localTag.setProductId(remoteTag.getIdentification().getProductId());
                if (remoteTag.getTag().getColor() == null) {
                    localTag.setColor(null);
                } else {
                    localTag.setColor(remoteTag.getTag().getColor().getHexcode());
                }
                localTag.setCreationDate(remoteTag.getAuditInformation().getCreationDate());
                localTag.setModificationDate(remoteTag.getAuditInformation().getLastModificationDate());
                localTag.setSummary(remoteTag.getTag().getName());
                localTag.setPriority(remoteTag.getTag().getPriority());
            }

            void setLocalTag(FXTag localTag, Tag remoteTag) {
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
