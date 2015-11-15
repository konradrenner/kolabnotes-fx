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
package org.kore.kolab.notes.fx.domain.tag;

import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.kore.kolab.notes.fx.domain.note.FXNotebook;
import org.kore.kolab.notes.fx.persistence.PersistenceManager;

/**
 *
 * @author Konrad Renner
 */
public class TagRepository {
    private final EntityManager em;

    public TagRepository() {
        em = PersistenceManager.createEntityManager();
    }  
    
    public List<FXTag> getNotebooks(){
        return Collections.unmodifiableList(em.createNamedQuery("FXTag.findAll", FXTag.class).getResultList());
    }
    
    public void createTag(FXTag tag){
        em.getTransaction().begin();
        em.persist(tag);
        em.getTransaction().commit();
        em.close();
    }
    
    public void updateTag(FXTag tag){
        em.getTransaction().begin();
        em.merge(tag);
        em.getTransaction().commit();
        em.close();
    }
}
