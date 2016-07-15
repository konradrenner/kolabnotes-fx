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
package org.kore.kolab.notes.fx.domain.note;

import java.sql.Timestamp;
import java.util.UUID;
import org.kore.kolab.notes.Note;

/**
 *
 * @author Konrad Renner
 */
public class NoteFactory {

    private final String accountId;

    public NoteFactory(String accountId) {
        this.accountId = accountId;
    }

    public FXNotebook newNotebook(String name) {
        long akttime = System.currentTimeMillis();
        FXNotebook nb = new FXNotebook(accountId, UUID.randomUUID().toString());
        nb.setSummary(name);
        nb.setCreationDate(new Timestamp(akttime));
        nb.setModificationDate(new Timestamp(akttime));
        nb.setProductId("kolabnotes-fx");

        return nb;
    }
    
    public FXNote newNote(String name, FXNotebook book) {
        long akttime = System.currentTimeMillis();
        FXNote note = new FXNote(accountId, UUID.randomUUID().toString());
        note.setSummary(name);
        note.setCreationDate(new Timestamp(akttime));
        note.setModificationDate(new Timestamp(akttime));
        note.setProductId("kolabnotes-fx");
        note.setClassification(Note.Classification.PUBLIC);
        note.setNotebook(book);
        book.setModificationDate(note.getModificationDate());
        
        return note;
    }
}
