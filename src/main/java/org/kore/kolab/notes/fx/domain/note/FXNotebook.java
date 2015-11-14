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
    @NamedQuery(name = "FXNotebook.findAll", query = "SELECT notebook FROM FXNotebook notebook ORDER BY notebook.summary"),
    @NamedQuery(name = "FXNotebook.findBySummary", query = "SELECT notebook FROM FXNotebook notebook WHERE notebook.summary = :summary ORDER BY notebook.summary"),
    @NamedQuery(name = "FXNotebook.findAllModified", query = "SELECT notebook FROM FXNotebook notebook WHERE notebook.modificationDate < :modificationDate ORDER BY notebook.summary")
})
@Table(name = "notebook")
@Entity
public class FXNotebook extends KolabObject implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Column(nullable = false, unique = true)
    private String summary;
    
    public FXNotebook(String id) {
        this.id = id;
    }

    protected FXNotebook() {
        //Tool
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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
        if (!(object instanceof FXNotebook)) {
            return false;
        }
        FXNotebook other = (FXNotebook) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FXNotebook{"+ super.toString() + "summary=" + summary +  '}';
    }
    
}
