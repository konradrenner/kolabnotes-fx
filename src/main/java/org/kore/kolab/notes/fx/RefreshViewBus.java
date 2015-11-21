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
package org.kore.kolab.notes.fx;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;

/**
 *
 * @author Konrad Renner
 */
public class RefreshViewBus {

    public enum RefreshTypes {
        NEW_ACCOUNT, CHANGE_ACCOUNT, EDITED_ACCOUNT, DELETED_ACCOUNT, SYNCED_ACCOUNT, NEW_NOTEBOOK, DELETED_NOTEBOOK, SELECTED_NOTEBOOK, NEW_TAG, EDITED_TAG, SELECTED_TAG, NEW_NOTE, SELECTED_NOTE, DELETED_NOTE, EDITED_NOTE;

        private final Map<String, RefreshListener> listener;

        private RefreshTypes() {
            listener = new LinkedHashMap<>();
        }

        void subscribe(RefreshListener r) {
            listener.put(r.getId(), r);
        }

        Collection<RefreshListener> getListener() {
            return Collections.unmodifiableCollection(this.listener.values());
        }
    }

    public static void subscribe(RefreshListener listener, RefreshTypes... types) {
        for (RefreshTypes type : types) {
            type.subscribe(listener);
        }
    }

    public static void informListener(RefreshEvent event) {
        final Set<String> alreadyInformed = new HashSet<>();
        event.getType().getListener().stream().forEach((listener) -> {
            if (!alreadyInformed.contains(listener.getId())) {
                Platform.runLater(() -> {
                    listener.refreshRequest(event);
                    alreadyInformed.add(listener.getId());
                });

            }
        });
    }

    public interface RefreshListener {
        String getId();
        void refreshRequest(RefreshEvent event);
    }

    public static final class RefreshEvent {

        private final String activeAccount;
        private final String objectId;
        private final RefreshTypes type;

        public RefreshEvent(String activeAccount, String objectId, RefreshTypes type) {
            this.objectId = objectId;
            this.type = type;
            this.activeAccount = activeAccount;
        }

        public String getActiveAccount() {
            return activeAccount;
        }

        public String getObjectId() {
            return objectId;
        }

        public RefreshTypes getType() {
            return type;
        }

        @Override
        public String toString() {
            return "RefreshEvent{" + "activeAccount=" + activeAccount + ", objectId=" + objectId + ", type=" + type + '}';
        }


    }
}
