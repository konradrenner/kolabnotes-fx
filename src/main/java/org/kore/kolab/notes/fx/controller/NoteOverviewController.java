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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteFactory;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class NoteOverviewController implements Initializable, RefreshViewBus.RefreshListener {
    
    @FXML
    private Accordion noteAccordion;

    @FXML
    private BorderPane noteRootPane;

    private String notebookId;

    private ResourceBundle bundle;

    private DateFormat dateFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToBus();
        bundle = resources;
        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        noteAccordion.prefWidthProperty().bind(noteRootPane.widthProperty());
    }
    
    private void subscribeToBus() {
        RefreshViewBus.subscribe(this, RefreshViewBus.RefreshTypes.CHANGE_ACCOUNT,
                RefreshViewBus.RefreshTypes.SYNCED_ACCOUNT,
                RefreshViewBus.RefreshTypes.NEW_ACCOUNT,
                RefreshViewBus.RefreshTypes.EDITED_NOTE,
                RefreshViewBus.RefreshTypes.NEW_NOTE,
                RefreshViewBus.RefreshTypes.DELETED_NOTE,
                RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK,
                RefreshViewBus.RefreshTypes.SELECTED_TAG);
    }

    @Override
    public void refreshRequest(RefreshViewBus.RefreshEvent event) {
        NoteRepository repo = new NoteRepository();
        List<FXNote> notes;
        if (event.getType() == RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK && event.getObjectId() != null) {
            notebookId = event.getObjectId();
            notes = repo.getNotebook(notebookId).getNotes();
        } else if (event.getType() == RefreshViewBus.RefreshTypes.EDITED_NOTE) {
            FXNotebook notebook = repo.getNote(event.getObjectId()).getNotebook();
            notebookId = notebook.getId();
            notes = notebook.getNotes();
        } else if (event.getType() == RefreshViewBus.RefreshTypes.SELECTED_TAG) {
            notebookId = null;
            FXTag tag = new TagRepository().getTagWithRefresh(event.getObjectId());
            notes = tag.getNotesAsList();
        } else if (event.getType() == RefreshViewBus.RefreshTypes.NEW_NOTE
                || event.getType() == RefreshViewBus.RefreshTypes.DELETED_NOTE
                || event.getType() == RefreshViewBus.RefreshTypes.EDITED_NOTE) {
            if (notebookId == null) {
                List<FXNotebook> notebooks = repo.getNotebooks(event.getActiveAccount());

                notes = new ArrayList<>();
                notebooks.stream().forEach((book) -> {
                    notes.addAll(book.getNotes());
                });
            } else {
                notes = repo.getNotebook(notebookId).getNotes();
            }
        } else {
            notebookId = null;
            List<FXNotebook> notebooks = repo.getNotebooks(event.getActiveAccount());

            notes = new ArrayList<>();
            notebooks.stream().forEach((book) -> {
                notes.addAll(book.getNotes());
            });
        }

        ArrayList<TitledPane> titeledPanes = new ArrayList<>(notes.size());

        for (FXNote note : notes) {
            titeledPanes.add(createNoteView(note));
        }

        this.noteAccordion.getPanes().setAll(titeledPanes);
    }

    @Override
    public String getId() {
        return "NoteOverviewController";
    }

    @FXML
    void importFolder(ActionEvent event) {
        if (notebookNotSelected()) {
            return;
        }

        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(bundle.getString("newnote"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        final File selectedDir = chooser.showDialog(noteRootPane.getScene().getWindow());

        if (selectedDir != null) {

            String selectedAccount = ToolbarController.getSelectedAccount();

            NoteRepository repo = new NoteRepository();
            NoteFactory factory = new NoteFactory(selectedAccount);
            FXNotebook notebook = repo.getNotebook(notebookId);
            List<FXNote> notes = new ArrayList<>();
            
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(selectedDir.toPath(), (Path entry) -> {
                boolean isNotDirectory = !Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS);
                boolean readable = Files.isReadable(entry);
                boolean endsWith = entry.getFileName().toString().toLowerCase().endsWith(".txt");
                return isNotDirectory && readable && endsWith;
            })) {

                String lineSeparator = System.getProperty("line.separator");

                for (Path path : directoryStream) {
                    String filename = path.getFileName().toString();
                    String summary = filename.substring(0, filename.length() - 4);

                    StringBuilder description = new StringBuilder("<pre>");
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            description.append(line).append(lineSeparator);
                        }
                    }
                    description.append("</pre>");

                    FXNote newNote = factory.newNote(summary, notebook);
                    newNote.setDescription(description.toString());
                    notes.add(newNote);
                }

                repo.createNotes(notebook, notes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), notebookId, RefreshViewBus.RefreshTypes.SELECTED_NOTEBOOK);
            RefreshViewBus.informListener(refreshEvent);
        }
    }

    @FXML
    void addNote(ActionEvent event) {
        if (notebookNotSelected()) {
            return;
        }

        String selectedAccount = ToolbarController.getSelectedAccount();
        
        NoteRepository repo = new NoteRepository();
        FXNote newNote = new NoteFactory(selectedAccount).newNote(bundle.getString("newnote"), repo.getNotebook(notebookId));
        repo.createNote(newNote);

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), newNote.getId(), RefreshViewBus.RefreshTypes.NEW_NOTE);
        RefreshViewBus.informListener(refreshEvent);
    }

    private boolean notebookNotSelected() {
        if (notebookId == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(bundle.getString("error"));
            alert.setHeaderText(bundle.getString("chooseNotebook"));
            alert.showAndWait();
            return true;
        }
        return false;
    }
    
    private TitledPane createNoteView(FXNote note) {
        GridPane gridpane = new GridPane();
        gridpane.add(new Label(bundle.getString("notebook")), 0, 1);
        gridpane.add(new Text(note.getNotebook().getSummary()), 1, 1);
        gridpane.add(new Label(bundle.getString("productid")), 0, 2);
        gridpane.add(new Text(note.getProductId()), 1, 2);
        gridpane.add(new Label(bundle.getString("creationDate")), 0, 3);
        gridpane.add(new Text(dateFormatter.format(note.getCreationDate())), 1, 3);
        gridpane.add(new Label(bundle.getString("modificationDate")), 0, 4);
        gridpane.add(new Text(dateFormatter.format(note.getModificationDate())), 1, 4);

        TitledPane titledPane = new TitledPane(note.getSummary(), gridpane);
        titledPane.expandedProperty().addListener(new NoteSelectionListener(note.getId()));
        return titledPane;
    }

    class NoteSelectionListener implements ChangeListener<Boolean> {

        private final String noteUID;

        public NoteSelectionListener(String noteUID) {
            this.noteUID = noteUID;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue) {
                RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), noteUID, RefreshViewBus.RefreshTypes.SELECTED_NOTE);
                RefreshViewBus.informListener(refreshEvent);
            }
        }

    }
}
