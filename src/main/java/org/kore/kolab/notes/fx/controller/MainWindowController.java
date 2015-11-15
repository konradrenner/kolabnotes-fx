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
package org.kore.kolab.notes.fx.controller;

/**
 *
 * @author Konrad Renner
 */
public class MainWindowController{
    
    public final static void refreshViews(String accountId){
        TagController.refreshView(accountId);
        NotebookController.refreshView(accountId);
        NoteOverviewController.refreshView(accountId);
        NoteDetailController.refreshView(accountId);
    }
}
