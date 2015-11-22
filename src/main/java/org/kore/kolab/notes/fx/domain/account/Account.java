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
package org.kore.kolab.notes.fx.domain.account;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author Konrad Renner
 */
@NamedQueries({
    @NamedQuery(name = "Account.findAll", query = "SELECT account FROM Account account ORDER BY account.id"),
})
@Entity
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;
    
    @Column(nullable = false)
    private String rootFolder;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private long syncIntervall;

    @Column(nullable = false)
    private long lastSync;
    
    @Column(nullable = false)
    private boolean syncSharedFolders;
    
    @Column(nullable = false)
    private boolean sslEnabled;
    
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @Column(nullable = false)
    private boolean enableKolabExtensions;
    
    @Enumerated(EnumType.STRING)
    private SyncIntervallType syncIntervallType;

    @Column(nullable = false)
    private boolean isActive;
    
    public Account(String id) {
        this.id = id;
    }

    protected Account() {
        //Tool
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEmail() {
        return email;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getLastSync() {
        return lastSync;
    }

    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getSyncIntervall() {
        return syncIntervall;
    }

    public void setSyncIntervall(long syncIntervall) {
        this.syncIntervall = syncIntervall;
    }

    public boolean isSyncSharedFolders() {
        return syncSharedFolders;
    }

    public void setSyncSharedFolders(boolean syncSharedFolders) {
        this.syncSharedFolders = syncSharedFolders;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public boolean isEnableKolabExtensions() {
        return enableKolabExtensions;
    }

    public void setEnableKolabExtensions(boolean enableKolabExtensions) {
        this.enableKolabExtensions = enableKolabExtensions;
    }

    public SyncIntervallType getSyncIntervallType() {
        return syncIntervallType;
    }

    public void setSyncIntervallType(SyncIntervallType syncIntervallType) {
        this.syncIntervallType = syncIntervallType;
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
        if (!(object instanceof Account)) {
            return false;
        }
        Account other = (Account) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Account{" + "id=" + id + ", email=" + email + ", rootFolder=" + rootFolder + ", password=" + password + ", syncIntervall=" + syncIntervall + ", lastSync=" + lastSync + ", syncSharedFolders=" + syncSharedFolders + ", sslEnabled=" + sslEnabled + ", accountType=" + accountType + ", enableKolabExtensions=" + enableKolabExtensions + ", syncIntervallType=" + syncIntervallType + ", isActive=" + isActive + '}';
    }

}
