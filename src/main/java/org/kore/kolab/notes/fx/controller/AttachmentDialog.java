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
package org.kore.kolab.notes.fx.controller;

import java.util.ResourceBundle;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.kore.kolab.notes.fx.domain.note.FXAttachment;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;

/**
 *
 * @author Konrad Renner
 */
public class AttachmentDialog extends Dialog<Void> {

    private final String noteUID;
    private final ResourceBundle bundle;
    private final NoteRepository repo;

    public AttachmentDialog(String noteUID, ResourceBundle bundle) {
        this.noteUID = noteUID;
        this.bundle = bundle;
        this.repo = new NoteRepository();
        init();
    }

    final void init() {
        addContent(repo.getNote(noteUID).getAttachments());
        addButtons();
    }

    private void addContent(Set<FXAttachment> attachments) {
        BorderPane content = new BorderPane();
        ScrollPane scroll = new ScrollPane();
        VBox attachmentList = new VBox();
        scroll.setContent(attachmentList);
        content.setCenter(scroll);

        getDialogPane().setContent(content);
    }

    private void addButtons() {
        ButtonType show = new ButtonType(bundle.getString("open"), ButtonData.OTHER);
        ButtonType add = new ButtonType(bundle.getString("add"), ButtonData.OTHER);
        ButtonType delete = new ButtonType(bundle.getString("delete"), ButtonData.OTHER);
        getDialogPane().getButtonTypes().addAll(show, add, delete, ButtonType.CLOSE);
    }

    private Node createAttachmentNode(FXAttachment attachment) {
        BorderPane content = new BorderPane();
        Text fileName = new Text(attachment.getFileName());
        fileName.setFont(Font.font(null, FontWeight.BOLD, 10));
        
        content.setCenter(fileName);
        
        Text mimeType = new Text(attachment.getMimeType());
        content.setBottom(mimeType);
        
        return content;
    }
}
