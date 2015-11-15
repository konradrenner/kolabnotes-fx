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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class TagController{
    
    @FXML
    private FlowPane tagPane;
    
    private final static ObservableList<String> TAGS = FXCollections.observableArrayList();

    public final static void refreshView(String accountId) {
        TagRepository repo = new TagRepository();
        
        TAGS.removeAll();
        
        repo.getTags(accountId).stream().forEach((tag) -> {
            TAGS.add(tag.getSummary());
        });
    }
    
    @FXML
    void addTag(ActionEvent event){
        System.out.println("org.kore.kolab.notes.fx.controller.TagController.addTag()");
        //TODO
    }
    
    @FXML
    void chooseTagColor(){
        System.out.println("org.kore.kolab.notes.fx.controller.TagController.chooseTagColor()");
        //TODO
    }
}
