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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import org.kore.kolab.notes.fx.domain.tag.FXTag;
import org.kore.kolab.notes.fx.domain.tag.TagRepository;

/**
 *
 * @author Konrad Renner
 */
public class TagController implements Initializable{
    
    @FXML
    private FlowPane tagPane;
    
    private ObservableList<String> tags;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TagRepository tagRepository = new TagRepository();
        
        List<FXTag> tags1 = tagRepository.getTags();
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
