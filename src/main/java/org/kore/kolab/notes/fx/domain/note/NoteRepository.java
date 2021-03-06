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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.kore.kolab.notes.fx.persistence.DeletedObject;
import org.kore.kolab.notes.fx.persistence.PersistenceManager;

/**
 *
 * @author Konrad Renner
 */
public class NoteRepository {
    private final EntityManager em;

    public NoteRepository() {
        em = PersistenceManager.createEntityManager();
    }

    public List<FXNotebook> getNotebooksModifiedAfter(String accountId, Timestamp lastSync) {
        return Collections.unmodifiableList(em.createNamedQuery("FXNotebook.findAllModified", FXNotebook.class).setParameter("accountId", accountId).setParameter("modificationDate", lastSync).getResultList());
    }

    public List<FXNote> getNotesModifiedAfter(String accountId, Timestamp lastSync) {
        return Collections.unmodifiableList(em.createNamedQuery("FXNote.findAllModified", FXNote.class).setParameter("accountId", accountId).setParameter("modificationDate", lastSync).getResultList());
    }
    
    public List<FXNotebook> getNotebooks(String accountId){
        return Collections.unmodifiableList(em.createNamedQuery("FXNotebook.findAll", FXNotebook.class).setParameter("accountId", accountId).getResultList());
    }

    public FXNotebook getNotebookBySummary(String accountId, String summary) {
        return em.createNamedQuery("FXNotebook.findBySummary", FXNotebook.class).setParameter("accountId", accountId).setParameter("summary", summary).getSingleResult();
    }

    public boolean anyLocalChanges(String accountId, Timestamp lastSync) {
        List<FXNotebook> notebooks = em.createNamedQuery("FXNotebook.findAllModified", FXNotebook.class).setParameter("accountId", accountId).setParameter("modificationDate", lastSync).getResultList();
        List<FXNote> notes = em.createNamedQuery("FXNote.findAllModified", FXNote.class).setParameter("accountId", accountId).setParameter("modificationDate", lastSync).getResultList();

        return !notes.isEmpty() || !notebooks.isEmpty();
    }

    public FXNotebook getNotebook(String uid) {
        FXNotebook book = em.find(FXNotebook.class, uid);
        if (book == null) {
            return null;
        }
        em.refresh(book);
        return book;
    }

    public FXNote getNote(String uid) {
        return em.find(FXNote.class, uid);
    }
    
    public void createNotebook(FXNotebook notebook){
        em.getTransaction().begin();
        em.persist(notebook);
        em.getTransaction().commit();
        em.close();
    }
    
    public void createNote(FXNote note){
        em.getTransaction().begin();
        FXNotebook book = note.getNotebook();
        book.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(book);
        em.persist(note);
        em.getTransaction().commit();
        em.close();
    }

    public void createNotes(FXNotebook book, Collection<FXNote> notes) {
        em.getTransaction().begin();
        book.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(book);
        for (FXNote note : notes) {
            em.persist(note);
        }
        em.getTransaction().commit();
        em.close();
    }

    public void createAttachment(FXAttachment attachment) {
        em.getTransaction().begin();
        FXNotebook book = attachment.getNote().getNotebook();
        book.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(book);
        em.persist(attachment);
        em.getTransaction().commit();
        em.close();
    }
    
    public void updateNotebook(FXNotebook notebook){
        em.getTransaction().begin();
        notebook.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(notebook);
        em.getTransaction().commit();
        em.close();
    }
    
    public void updateNote(FXNote note){
        em.getTransaction().begin();
        note.setModificationDate(new Timestamp(System.currentTimeMillis()));
        FXNotebook book = note.getNotebook();
        book.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(book);
        em.merge(note);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteNotebook(FXNotebook notebook) {
        em.getTransaction().begin();
        em.remove(notebook);
        DeletedObject obj = new DeletedObject();
        obj.setAccountId(notebook.getAccountId());
        obj.setObjectId(notebook.getId());
        obj.setObjectSummary(notebook.getSummary());
        obj.setType(DeletedObject.Type.NOTEBOOK);
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
    }
    
    public void deleteNote(FXNote note){
        em.getTransaction().begin();
        FXNotebook book = note.getNotebook();
        book.removeNote(note.getId());
        book.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(book);
        em.remove(note);
        DeletedObject obj = new DeletedObject();
        obj.setAccountId(note.getAccountId());
        obj.setObjectId(note.getId());
        obj.setObjectSummary(note.getSummary());
        obj.setType(DeletedObject.Type.NOTE);
        em.persist(obj);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteAttachment(FXAttachment attachment) {
        em.getTransaction().begin();
        FXNotebook book = attachment.getNote().getNotebook();
        book.setModificationDate(new Timestamp(System.currentTimeMillis()));
        em.merge(book);
        em.remove(attachment);
        em.getTransaction().commit();
        em.close();
    }
}
