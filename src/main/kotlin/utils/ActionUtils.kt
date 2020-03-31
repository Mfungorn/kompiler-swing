package utils

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JTextArea
import javax.swing.undo.UndoManager

class UndoAction(private val undoManager: UndoManager) : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
        if (undoManager.canUndo())
            undoManager.undo()
    }
}

class RedoAction(private val undoManager: UndoManager) : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
        if (undoManager.canRedo())
            undoManager.redo()
    }
}

class RemoveSelectedAction(private val textArea: JTextArea) : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
        textArea.replaceSelection("")
    }
}