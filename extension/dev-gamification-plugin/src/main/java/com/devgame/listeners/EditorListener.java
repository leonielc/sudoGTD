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
        
        // Initialize the count so we don't count the existing lines as "new" lines
        lastLineCounts.put(document, document.getLineCount());
        
        DocumentListener listener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                int currentLineCount = document.getLineCount();
                int previousLineCount = lastLineCounts.getOrDefault(document, currentLineCount);
                
                // Calculate difference (Positive if you type, Negative if you delete)
                int diff = currentLineCount - previousLineCount;
                
                // Only update if the line count actually changed
                if (diff != 0) {
                    LevelingService.getInstance(project).addLinesWritten(diff);
                    System.out.println("DevGame: Line change: " + diff);
                }
                
                // Update the memory for the next keystroke
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