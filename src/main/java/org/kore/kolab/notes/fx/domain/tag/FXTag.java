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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.kore.kolab.notes.fx.persistence.KolabObject;

/**
 *
 * @author Konrad Renner
 */
@NamedQueries({
    @NamedQuery(name = "FXTag.findAll", query = "SELECT tag FROM FXTag tag ORDER BY tag.summary"),
    @NamedQuery(name = "FXTag.findBySummary", query = "SELECT tag FROM FXTag tag WHERE tag.summary = :summary ORDER BY tag.summary"),
    @NamedQuery(name = "FXTag.findAllModified", query = "SELECT tag FROM FXTag tag WHERE tag.modificationDate < :modificationDate ORDER BY tag.summary")
})
@Table(name="tag")
@Entity
public class FXTag extends KolabObject implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Column(nullable = false)
    private String summary;
    
    @Column
    private String color;
    
    public FXTag(String id) {
        this.id = id;
    }

    protected FXTag() {
        //Tool
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FXTag)) {
            return false;
        }
        FXTag other = (FXTag) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FXTag{" + super.toString() + "summary=" + summary + ", color=" + color + '}';
    }

   
}
