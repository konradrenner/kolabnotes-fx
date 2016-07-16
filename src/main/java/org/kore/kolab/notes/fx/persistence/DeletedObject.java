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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author Konrad Renner
 */
@NamedQueries({
    @NamedQuery(name = "DeletedObject.findAll", query = "SELECT obj FROM DeletedObject obj WHERE obj.accountId = :accountId ORDER BY obj.deletionTimestamp"),
    @NamedQuery(name = "DeletedObject.findByObjectId", query = "SELECT obj FROM DeletedObject obj WHERE obj.accountId = :accountId and obj.objectId = :objectId ORDER BY obj.deletionTimestamp")
})
@Entity
public class DeletedObject implements Serializable {

    public enum Type {
        TAG, NOTE, NOTEBOOK;
    }

    @Id
    private Timestamp deletionTimestamp;

    private String accountId;

    private String objectId;

    private String objectSummary;

    private Type type;

    public DeletedObject() {
        this.deletionTimestamp = new Timestamp(System.currentTimeMillis());
    }

    public String getAccountId() {
        return accountId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectSummary() {
        return objectSummary;
    }

    public void setObjectSummary(String objectSummary) {
        this.objectSummary = objectSummary;
    }

    public Timestamp getDeletionTimestamp() {
        return new Timestamp(deletionTimestamp.getTime());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.deletionTimestamp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeletedObject other = (DeletedObject) obj;
        if (!Objects.equals(this.deletionTimestamp, other.deletionTimestamp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DeletedObject{" + "deletionTimestamp=" + deletionTimestamp + ", accountId=" + accountId + ", objectId=" + objectId + ", objectSummary=" + objectSummary + ", type=" + type + '}';
    }

}
