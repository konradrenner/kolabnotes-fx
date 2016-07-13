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
package org.kore.kolab.notes.fx.domain.note;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Konrad Renner
 */
public class FXAttachmentId implements Serializable {

    private String accountId;
    private String note;
    private String attachmentid;

    public String getAccountId() {
        return accountId;
    }

    public String getNoteId() {
        return note;
    }

    public String getAttachmentId() {
        return attachmentid;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.accountId);
        hash = 23 * hash + Objects.hashCode(this.note);
        hash = 23 * hash + Objects.hashCode(this.attachmentid);
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
        final FXAttachmentId other = (FXAttachmentId) obj;
        if (!Objects.equals(this.accountId, other.accountId)) {
            return false;
        }
        if (!Objects.equals(this.note, other.note)) {
            return false;
        }
        if (!Objects.equals(this.attachmentid, other.attachmentid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FXAttachmentId{" + "accountId=" + accountId + ", noteId=" + note + ", attachmentId=" + attachmentid + '}';
    }

}
