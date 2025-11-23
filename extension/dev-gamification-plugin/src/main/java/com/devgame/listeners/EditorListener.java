package com.devgame.listeners;

import com.devgame.services.LevelingService;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EditorListener implements FileEditorManagerListener {
    
    private final Map<Document, DocumentListener> documentListeners = new HashMap<>();
    private final Map<Document, Integer> lastLineCounts = new HashMap<>();
    
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) return;
        
        Project project = source.getProject();
        lastLineCounts.put(document, document.getLineCount());
        
        DocumentListener listener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                int currentLineCount = document.getLineCount();
                int previousLineCount = lastLineCounts.getOrDefault(document, currentLineCount);
                
                if (currentLineCount > previousLineCount) {
                    int linesAdded = currentLineCount - previousLineCount;
                    LevelingService.getInstance(project).addLinesWritten(linesAdded);
                    System.out.println("DevGame: Added " + linesAdded + " lines");
                }
                
                lastLineCounts.put(document, currentLineCount);
            }
        };
        
        document.addDocumentListener(listener);
        documentListeners.put(document, listener);
    }
    
    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) return;
        
        DocumentListener listener = documentListeners.remove(document);
        if (listener != null) {
            document.removeDocumentListener(listener);
        }
        lastLineCounts.remove(document);
    }
}
