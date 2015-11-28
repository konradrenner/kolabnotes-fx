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
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
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
    
    public List<FXNotebook> getNotebooks(String accountId){
        return Collections.unmodifiableList(em.createNamedQuery("FXNotebook.findWithDeletedFlag", FXNotebook.class).setParameter("accountId", accountId).setParameter("deleted", false).getResultList());
    }

    public FXNotebook getNotebookBySummary(String accountId, String summary) {
        return em.createNamedQuery("FXNotebook.findBySummary", FXNotebook.class).setParameter("accountId", accountId).setParameter("summary", summary).getSingleResult();
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
        em.persist(note);
        em.getTransaction().commit();
        em.close();
    }
    
    public void updateNotebook(FXNotebook notebook){
        em.getTransaction().begin();
        notebook.setModificationDate(new Timestamp(System.currentTimeMillis()));
        notebook.setProductId("kolabnotes-fx");
        em.merge(notebook);
        em.getTransaction().commit();
        em.close();
    }
    
    public void updateNote(FXNote note){
        em.getTransaction().begin();
        note.setModificationDate(new Timestamp(System.currentTimeMillis()));
        note.setProductId("kolabnotes-fx");
        em.merge(note);
        em.getTransaction().commit();
        em.close();
    }
    
    public void deleteNotebook(FXNotebook notebook){
        em.getTransaction().begin();
        em.remove(notebook);
        em.getTransaction().commit();
        em.close();
    }
    
    public void deleteNote(FXNote note){
        em.getTransaction().begin();
        em.remove(note);
        em.getTransaction().commit();
        em.close();
    }
}
