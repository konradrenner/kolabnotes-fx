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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import org.kore.kolab.notes.fx.RefreshViewBus;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.domain.note.NoteFactory;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;

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
        } else if (event.getType() == RefreshViewBus.RefreshTypes.NEW_NOTE) {
            notes = repo.getNotebook(notebookId).getNotes();
        } else {
            notebookId = null;
            List<FXNotebook> notebooks = repo.getNotebooks(event.getActiveAccount());

            notes = new ArrayList<>();
            notebooks.stream().forEach((book) -> {
                notes.addAll(book.getNotes());
            });
        }

        ObservableList<TitledPane> titeledPanes = FXCollections.observableArrayList();

        notes.stream().forEach((note) -> {
            titeledPanes.add(createNoteView(note));
        });

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
    
    @FXML
    void deleteNote(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.NoteOverviewController.deleteNote()");
        //TODO
    }

    private TitledPane createNoteView(FXNote note) {
        Label label = new Label(note.getDescription());

        return new TitledPane(note.getSummary(), label);
    }
}
