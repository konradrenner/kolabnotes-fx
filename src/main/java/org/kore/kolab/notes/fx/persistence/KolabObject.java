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
package org.kore.kolab.notes.fx.persistence;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Konrad Renner
 */
@MappedSuperclass
public class KolabObject {
    @Id
    protected String id;
    
    @Column(nullable = false)
    protected String productId;
    
    @Column(nullable = false)
    protected Timestamp creationDate;
    
    @Column(nullable = false)
    protected Timestamp modificationDate;
    
    @Column(nullable = false)
    private boolean deleted;
    
    @Column(nullable = false)
    private String accountId;
    
    public KolabObject(String accountId, String id){
        this.id = id;
        this.accountId = accountId;
    }
    
    public KolabObject(){
        //tool
    }

    public String getAccountId() {
        return accountId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Timestamp getCreationDate() {
        return new Timestamp(creationDate.getTime());
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = new Timestamp(creationDate.getTime());
    }

    public Timestamp getModificationDate() {
        return new Timestamp(modificationDate.getTime());
    }

    public void setModificationDate(Timestamp modificationDate) {
        this.modificationDate = new Timestamp(modificationDate.getTime());
    }

    public String getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }
    
    void deleteLogical(){
        this.deleted = true;
    }

    @Override
    public String toString() {
        return "KolabObject{ accountId=" + accountId + ", id=" + id + ", productId=" + productId + ", creationDate=" + creationDate + ", modificationDate=" + modificationDate + ", deleted=" + deleted + '}';
    }

    
}
