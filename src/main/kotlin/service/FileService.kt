package service

import java.awt.Component
import java.io.File
import java.io.IOException
import javax.swing.JFileChooser

class FileService {
    private val listeners: MutableList<FileServiceEventListener> = mutableListOf()
    private val chooser = JFileChooser().apply {
        currentDirectory = File(".")
    }

    var currentFile: File? = null

    fun createFile(filename: String, ownerWindow: Component? = null): File? {
        val dir: File? = chooser.run {
            dialogTitle = "Select directory"
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false

            if (chooser.showOpenDialog(ownerWindow) == JFileChooser.APPROVE_OPTION) {
                chooser.currentDirectory
            } else {
                null
            }
        }
        currentFile = dir?.let {
            File(dir.absolutePath + File.separator + filename).apply {
                createNewFile()
            }
        }

        listeners.forEach { it.consumeEvent(FileServiceEvent.FileChanged(currentFile)) }

        return currentFile
    }

    fun openFile(ownerWindow: Component? = null): File? {
        val file: File? = chooser.run {
            dialogTitle = "Select file to open"
            fileSelectionMode = JFileChooser.FILES_ONLY

            if (chooser.showOpenDialog(ownerWindow) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile
            } else {
                null
            }
        }
        currentFile = file
            ?.apply {
                if (!canRead()) throw IOException("Cannot read file")
            }
            ?: throw IOException("File is null")

        listeners.forEach { it.consumeEvent(FileServiceEvent.FileChanged(currentFile)) }

        return currentFile
    }

    fun saveToFile(data: String, onFileNameMissed: () -> Unit) {
        currentFile
            ?.apply {
                if (canWrite())
                    writeText(data)
                else
                    throw IOException("Cannot write to file")
            }
            ?: onFileNameMissed()
    }

    fun saveFileAs(filename: String, data: String, ownerWindow: Component? = null): File? {
        currentFile = createFile(filename, ownerWindow)?.apply {
            if (canWrite()) {
                writeText(data)
            } else {
                throw IOException("Cannot write to file")
            }
        }

        listeners.forEach { it.consumeEvent(FileServiceEvent.FileChanged(currentFile)) }

        return currentFile
    }

    fun addFileServiceEventListener(listener: FileServiceEventListener) {
        listeners.add(listener)
    }

    fun removeFileServiceEventListener(listener: FileServiceEventListener) {
        listeners.remove(listener)
    }

    interface FileServiceEventListener {
        fun consumeEvent(event: FileServiceEvent)
    }

    sealed class FileServiceEvent {
        class FileChanged(val file: File?) : FileServiceEvent()
    }
}