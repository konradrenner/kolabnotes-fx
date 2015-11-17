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

import java.sql.Timestamp;
import java.util.UUID;

/**
 *
 * @author Konrad Renner
 */
public class TagFactory {

    private final String accountId;

    public TagFactory(String accountId) {
        this.accountId = accountId;
    }

    public FXTag newTag(String name) {
        long akttime = System.currentTimeMillis();
        FXTag fxtag = new FXTag(accountId, UUID.randomUUID().toString());
        fxtag.setSummary(name);
        fxtag.setCreationDate(new Timestamp(akttime));
        fxtag.setModificationDate(new Timestamp(akttime));
        fxtag.setProductId("kolabnotes-fx");

        return fxtag;
    }
}
