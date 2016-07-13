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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.kore.kolab.notes.fx.domain.note.FXAttachment;
import org.kore.kolab.notes.fx.domain.note.FXNote;
import org.kore.kolab.notes.fx.domain.note.NoteRepository;

/**
 *
 * @author Konrad Renner
 */
public class AttachmentDialog extends Dialog<Void> {

    private final String noteUID;
    private final String accountId;
    private final ResourceBundle bundle;
    private final NoteRepository repo;
    private final Window stage;
    private FileChooser fileChooser;
    private FXNote note;
    private final VBox attachmentList;

    public AttachmentDialog(String accountId, String noteUID, ResourceBundle bundle, Window stage) {
        this.noteUID = noteUID;
        this.accountId = accountId;
        this.bundle = bundle;
        this.repo = new NoteRepository();
        this.stage = stage;
        this.attachmentList = new VBox();
        init();
    }

    final void init() {
        note = repo.getNote(noteUID);
        addContent(note.getAttachments());
        setTitle(bundle.getString("attachments"));
        getDialogPane().setPrefSize(400, 600);
    }

    private void addContent(Set<FXAttachment> attachments) {
        BorderPane content = new BorderPane();
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(attachmentList);
        content.setCenter(scroll);
        content.setTop(createButtons());

        getDialogPane().setContent(content);
    }

    private Node createButtons() {
        FlowPane flow = new FlowPane(Orientation.HORIZONTAL);
        Button add = new Button(bundle.getString("add"));

        add.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new FileChooser();
                }

                File file = fileChooser.showOpenDialog(stage);

                if (file != null) {
                    try (InputStream input = new FileInputStream(file);
                            ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        
                        int count;
                        byte[] bytes = new byte[1024];
                        while ((count = input.read(bytes)) != -1) {
                            output.write(bytes, 0, count);
                        }

                        FXAttachment attachment = new FXAttachment(accountId, file.getName(), repo.getNote(noteUID));
                        attachment.setAttachmentData(output.toByteArray());
                        attachment.setMimeType(Files.probeContentType(file.toPath()));
                        note.addAttachment(attachment);

                        attachmentList.getChildren().add(createAttachmentNode(attachment));
                        repo.createAttachment(attachment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        flow.getChildren().addAll(add);
        flow.setPadding(new Insets(1.0));
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        return flow;
    }

    private Node createAttachmentNode(FXAttachment attachment) {
        BorderPane content = new BorderPane();
        FlowPane contentFlow = new FlowPane(Orientation.VERTICAL);
        contentFlow.setPadding(new Insets(1.0));
        Text fileName = new Text(attachment.getFileName());
        fileName.setFont(Font.font(null, FontWeight.BOLD, 10));
        
        Text mimeType = new Text(attachment.getMimeType());

        contentFlow.getChildren().addAll(fileName, mimeType);

        content.setCenter(contentFlow);
        
        FlowPane flow = new FlowPane(Orientation.HORIZONTAL);
        Button show = new Button(bundle.getString("open"));
        Button delete = new Button(bundle.getString("delete"));
        
        flow.getChildren().addAll(show, delete);
        flow.setPadding(new Insets(1.0));
        
        content.setBottom(flow);
        
        return content;
    }
}
