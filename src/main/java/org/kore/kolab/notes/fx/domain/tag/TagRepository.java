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

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
    
    public List<FXTag> getTags(String accountId){
        return Collections.unmodifiableList(em.createNamedQuery("FXTag.findAll", FXTag.class).setParameter("accountId", accountId).getResultList());
    }
    
    public Optional<FXTag> getTagByName(String accountId, String summary) {
        try{
            return Optional.of(em.createNamedQuery("FXTag.findBySummary", FXTag.class).setParameter("accountId", accountId).setParameter("summary", summary).getSingleResult());
        }catch(NoResultException e){
            return Optional.empty();
        }
    }

    public FXTag getTag(String uid) {
        return em.find(FXTag.class, uid);
    }
    
    public void createTag(FXTag tag){
        Optional<FXTag> toCheck = getTagByName(tag.getAccountId(), tag.getSummary());
        
        if(toCheck.isPresent()){
            return;
        }
        
        em.getTransaction().begin();
        em.persist(tag);
        em.getTransaction().commit();
        em.close();
    }
    
    public void updateTag(FXTag tag){
        em.getTransaction().begin();
        tag.setModificationDate(new Timestamp(System.currentTimeMillis()));
        tag.setProductId("kolabnotes-fx");
        em.merge(tag);
        em.getTransaction().commit();
        em.close();
    }
}
