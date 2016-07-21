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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.Pane;
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
        this.attachmentList = new VBox(1.0);
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
        ObservableList<Node> children = attachmentList.getChildren();

        int idx = 0;
        for (FXAttachment att : attachments) {
            Pane createAttachmentNode = createAttachmentNode(idx++, att);
            children.add(createAttachmentNode);
        }

        content.setCenter(scroll);
        Pane buttonPane = createButtons();
        buttonPane.prefWidthProperty().bind(content.widthProperty());
        content.setTop(buttonPane);

        getDialogPane().setContent(content);
    }

    private Pane createButtons() {
        BorderPane pane = new BorderPane();
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
                        note.setModificationDate(new Timestamp(System.currentTimeMillis()));

                        attachmentList.getChildren().add(createAttachmentNode(attachmentList.getChildren().size(), attachment));
                        repo.createAttachment(attachment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        pane.setCenter(add);
        pane.setPadding(new Insets(1.0));
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        return pane;
    }

    private Pane createAttachmentNode(int index, FXAttachment attachment) {
        Insets insets = new Insets(2.0);
        BorderPane content = new BorderPane();
        FlowPane contentFlow = new FlowPane(Orientation.VERTICAL);
        contentFlow.setPadding(new Insets(1.0));
        Text fileName = new Text(attachment.getFileName());
        fileName.setFont(Font.font(null, FontWeight.BOLD, 15));
        
        Text mimeType = new Text(attachment.getMimeType());
        mimeType.setFont(Font.font(null, FontWeight.NORMAL, 15));

        contentFlow.getChildren().addAll(fileName, mimeType);

        content.setCenter(contentFlow);
        
        FlowPane flow = new FlowPane(Orientation.VERTICAL);
        Button show = new Button(bundle.getString("open"));
        show.setPadding(Insets.EMPTY);

        show.setOnAction(new OpenActionHandler(attachment));

        Button delete = new Button(bundle.getString("delete"));

        delete.setOnAction(new DeleteActionHandler(attachment, index));
        
        flow.getChildren().addAll(show, delete);
        flow.setPadding(insets);
        flow.setHgap(2);

        flow.setPadding(insets);
        show.setPadding(insets);
        delete.setPadding(insets);
        
        content.setLeft(flow);
        
        content.setPrefHeight(55);
        //content.setBorder(new Border(new BorderStroke(Paint.valueOf(Color.BLACK.toString()), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        return content;
    }

    class OpenActionHandler implements EventHandler<ActionEvent> {

        private final FXAttachment attachment;

        public OpenActionHandler(FXAttachment attachment) {
            this.attachment = attachment;
        }

        @Override
        public void handle(ActionEvent event) {
            try {
                FileChooser chooser = new FileChooser();
                chooser.setInitialFileName(attachment.getFileName());

                File file = chooser.showSaveDialog(stage);

                if (file != null) {
                    try (OutputStream stream = new FileOutputStream(file);
                            ByteArrayInputStream input = new ByteArrayInputStream(attachment.getAttachmentData())) {
                        int count;
                        byte[] bytes = new byte[1024];
                        while ((count = input.read(bytes)) != -1) {
                            stream.write(bytes, 0, count);
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    class DeleteActionHandler implements EventHandler<ActionEvent> {

        private final FXAttachment attachment;
        private final int index;

        public DeleteActionHandler(FXAttachment attachment, int index) {
            this.attachment = attachment;
            this.index = index;
        }

        @Override
        public void handle(ActionEvent event) {
            attachmentList.getChildren().remove(index);
            note.setModificationDate(new Timestamp(System.currentTimeMillis()));
            note.removeAttachment(attachment);
            repo.deleteAttachment(attachment);
        }

    }
}
