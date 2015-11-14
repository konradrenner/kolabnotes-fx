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
import java.util.Optional;
import javax.persistence.EntityManager;
import org.kore.kolab.notes.fx.domain.account.Account;
import org.kore.kolab.notes.fx.persistence.EntityManagerProducer;

/**
 *
 * @author Konrad Renner
 */
public class AccountRepository {
    
    public List<Account> getAccounts(){
        EntityManager em = EntityManagerProducer.createEntityManager();
        
        return Collections.unmodifiableList(em.createNamedQuery("Account.findAll", Account.class).getResultList());
    }
    
    public Optional<Account> getAccount(String name){
        EntityManager em = EntityManagerProducer.createEntityManager();
        
        Account account = em.find(Account.class, name);
        
        return Optional.ofNullable(account);
    }
    
    public void createAccount(Account account){
        EntityManager em = EntityManagerProducer.createEntityManager();
        
        em.persist(account);
    }
    
    public void updateAccount(Account account){
        EntityManager em = EntityManagerProducer.createEntityManager();
        
        em.merge(account);
    }
    
    public void deleteAccount(Account account){
        EntityManager em = EntityManagerProducer.createEntityManager();
        
        em.remove(account);
    }
}
