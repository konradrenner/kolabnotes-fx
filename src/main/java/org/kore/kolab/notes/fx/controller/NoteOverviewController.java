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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteFactory;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class NoteOverviewController implements Initializable, RefreshViewBus.RefreshListener {
    
    @FXML
    private Accordion noteAccordion;

    private String notebookId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subscribeToBus();
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
            notes = new TagRepository().getTag(event.getObjectId()).getNotes();
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

    private
    @FXML
    void addNote(ActionEvent event) {
        if (notebookId == null) {
            //TODO select notebook
            return;
        }

        String selectedAccount = ToolbarController.getSelectedAccount();
        
        NoteRepository repo = new NoteRepository();
        FXNote newNote = new NoteFactory(selectedAccount).newNote("New Note", repo.getNotebook(notebookId));
        repo.createNote(newNote);

        RefreshViewBus.RefreshEvent refreshEvent = new RefreshViewBus.RefreshEvent(ToolbarController.getSelectedAccount(), newNote.getId(), RefreshViewBus.RefreshTypes.NEW_NOTE);
        RefreshViewBus.informListener(refreshEvent);
    }
    
    private TitledPane createNoteView(FXNote note) {
        GridPane gridpane = new GridPane();
        gridpane.add(new Label("Product-ID"), 1, 0);
        gridpane.add(new Text(note.getProductId()), 2, 0);
        gridpane.add(new Label("Creation date"), 1, 1);
        gridpane.add(new Text(note.getCreationDate().toString()), 2, 1);
        gridpane.add(new Label("Modification date"), 1, 2);
        gridpane.add(new Text(note.getModificationDate().toString()), 2, 2);

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
