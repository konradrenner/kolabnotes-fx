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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author Konrad Renner
 */
@Entity
@Table(name = "attachment")
@IdClass(FXAttachmentId.class)
public class FXAttachment implements Serializable {

    @Id
    private String accountId;
    @Id
    private String attachmentid;

    @Id
    @ManyToOne
    @JoinColumn(name = "noteId")
    private FXNote note;

    private String mimeType;
    private String fileName;
    @Lob
    private byte[] attachmentData;

    public FXAttachment(String accountId, String attachmentid, FXNote note) {
        this.accountId = accountId;
        this.attachmentid = attachmentid;
        this.fileName = attachmentid;
        this.note = note;
    }

    public FXAttachment() {
    }

    public String getAccountId() {
        return accountId;
    }

    public FXNote getNote() {
        return note;
    }

    public byte[] getAttachmentData() {
        return attachmentData;
    }

    public void setAttachmentData(byte[] attachmentData) {
        this.attachmentData = attachmentData;
    }

    public String getAttachmentid() {
        return attachmentid;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.accountId);
        hash = 29 * hash + Objects.hashCode(this.attachmentid);
        hash = 29 * hash + Objects.hashCode(this.note);
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
        final FXAttachment other = (FXAttachment) obj;
        if (!Objects.equals(this.accountId, other.accountId)) {
            return false;
        }
        if (!Objects.equals(this.attachmentid, other.attachmentid)) {
            return false;
        }
        if (!Objects.equals(this.note, other.note)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "FXAttachment{" + "attachmentid=" + attachmentid + ", mimeType=" + mimeType + ", fileName=" + fileName + ", attachmentData=" + attachmentData + '}';
    }


}
