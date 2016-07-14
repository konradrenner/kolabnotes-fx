/*
 * Copyright (C) 2016 KoRe
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
package org.kore.kolab.notes.fx.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 *
 * @author Konrad Renner
 */
public class DeletedObjectRepository {
    private final EntityManager em;

    public DeletedObjectRepository() {
        em = PersistenceManager.createEntityManager();
    }

    public DeletedObjectRepository(EntityManager em) {
        this.em = em;
    }

    public Map<String, DeletedObject> getDeletedObjects(String accountId) {
        List<DeletedObject> resultList = em.createNamedQuery("DeletedObject.findAll", DeletedObject.class).setParameter("accountId", accountId).getResultList();

        HashMap<String, DeletedObject> map = new HashMap<>();
        for (DeletedObject object : resultList) {
            if (DeletedObject.Type.NOTEBOOK == object.getType()) {
                map.put(object.getObjectSummary(), object);
            } else {
                map.put(object.getObjectId(), object);
            }
        }

        return map;
    }

    public void clearDeletedObjects(String accountId) {
        em.createNativeQuery("delete from DeletedObject where accountId='" + accountId + "'").executeUpdate();
    }
}
