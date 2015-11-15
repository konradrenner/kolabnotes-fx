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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 *
 * @author Konrad Renner
 */
public class NotebookController {
    
    @FXML
    private VBox notebookBox;

    public final static void refreshView(String accountId){
        //TODO
    }
    
    @FXML
    void addNotebook(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.NotebookController.addNotebook()");
        //TODO
    }
    
    @FXML
    void deleteNotebook(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.NotebookController.deleteNotebook()");
        //TODO
    }
}
