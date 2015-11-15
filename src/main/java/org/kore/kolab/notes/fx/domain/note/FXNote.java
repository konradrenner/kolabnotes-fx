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

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.kore.kolab.notes.Note;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.persistence.KolabObject;

/**
 *
 * @author Konrad Renner
 */
@NamedQueries({
    @NamedQuery(name = "FXNote.findAll", query = "SELECT note FROM FXNote note WHERE note.accountId = :accountId ORDER BY note.summary"),
    @NamedQuery(name = "FXNote.findWithDeletedFlag", query = "SELECT note FROM FXNote note WHERE note.accountId = :accountId note.deleted = :deleted ORDER BY note.summary"),
    @NamedQuery(name = "FXNote.findAllModified", query = "SELECT note FROM FXNote note WHERE note.accountId = :accountId note.modificationDate < :modificationDate ORDER BY note.summary")
})
@Table(name = "note")
@Entity
public class FXNote extends KolabObject implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Column(nullable = false)
    private String summary;
    
    @Column
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Note.Classification classification;

    @Column
    private String color;
    
    @ManyToOne
    @JoinColumn(name = "nbsummary")
    private FXNotebook notebook;
    
    @ManyToMany
    @JoinTable(
      name="note_tags",
      joinColumns={@JoinColumn(name="note_id", referencedColumnName="id")},
      inverseJoinColumns={@JoinColumn(name="tag_id", referencedColumnName="id")})
    private List<FXTag> tags;
    
    public FXNote(String accountId, String id) {
        super(accountId, id);
    }

    protected FXNote() {
        //Tool
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Note.Classification getClassification() {
        return classification;
    }

    public void setClassification(Note.Classification classification) {
        this.classification = classification;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public FXNotebook getNotebook() {
        return notebook;
    }


    public List<FXTag> getTags() {
        return tags;
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
        if (!(object instanceof FXNote)) {
            return false;
        }
        FXNote other = (FXNote) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FXNote{" + super.toString() + "summary=" + summary + ", description=" + description + ", classification=" + classification + ", color=" + color + ", notebook=" + notebook + ", tags=" + tags + '}';
    }
}
