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
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.persistence.KolabObject;

/**
 *
 * @author Konrad Renner
 */
@NamedQueries({
    @NamedQuery(name = "FXTag.findAll", query = "SELECT tag FROM FXTag tag WHERE tag.accountId = :accountId ORDER BY tag.tagsummary"),
    @NamedQuery(name = "FXTag.findBySummary", query = "SELECT tag FROM FXTag tag WHERE tag.accountId = :accountId AND tag.tagsummary = :summary"),
    @NamedQuery(name = "FXTag.findAllModified", query = "SELECT tag FROM FXTag tag WHERE tag.accountId = :accountId AND tag.modificationDate < :modificationDate ORDER BY tag.tagsummary")
})
@Table(name="tag")
@Entity
public class FXTag extends KolabObject implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Column(nullable = false)
    private String tagsummary;
    
    @Column
    private String color;
    
    @ManyToMany(mappedBy="tags")
    private List<FXNote> notes;
    
    public FXTag(String accountId, String id) {
        super(accountId, id);
    }

    protected FXTag() {
        //Tool
    }

    public List<FXNote> getNotes() {
        return notes;
    }

    public void setNotes(List<FXNote> notes) {
        this.notes = notes;
    }
    
    public String getSummary() {
        return tagsummary;
    }

    public void setSummary(String summary) {
        this.tagsummary = summary;
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
        return "FXTag{" + super.toString() + "tagsummary=" + tagsummary + ", color=" + color + ", notes=" + notes + '}';
    }
}
