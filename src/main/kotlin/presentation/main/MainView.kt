package presentation.main

import org.koin.core.KoinComponent
import org.koin.core.inject
import presentation.TextInputDialog
import presentation.help.HelpView
import service.FileService
import java.awt.Dimension
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.undo.UndoManager


class MainView : JFrame(), KoinComponent {
    private val mainViewController: MainViewController by inject()

    private val inputTextArea: JTextArea
    private val outputTextArea: JTextArea

    init {
        title = "Kompiler"

        layout = BoxLayout(this.contentPane, BoxLayout.Y_AXIS)

        defaultCloseOperation = EXIT_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                showConfirmationDialog(
                    content = "You may have unsaved changes. Proceed closing?",
                    onConfirm = {
                        dispose()
                    },
                    onDecline = {
                        // TODO
                    }
                )
            }
        })

        setSize(800, 600)
        setLocationRelativeTo(null)

        val manager = UndoManager()
        inputTextArea = JTextArea().apply {
            document.addUndoableEditListener(manager)

            val copyKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)
            val pasteKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK)
            val cutKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK)
            val selectAllKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK)
            val undoKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
            val redoKetStroke =
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)

            keymap.apply {
                addActionForKeyStroke(copyKetStroke, TransferHandler.getCopyAction())
                addActionForKeyStroke(pasteKetStroke, TransferHandler.getPasteAction())
                addActionForKeyStroke(cutKetStroke, TransferHandler.getCutAction())
//                addActionForKeyStroke(undoKetStroke, )
//                addActionForKeyStroke(redoKetStroke, )
            }
        }

        outputTextArea = JTextArea().apply {
            isEditable = false
            isFocusable = false
        }

        val fileMenu = JMenu("File").apply {
            add(JMenuItem("Create new").apply {
                addActionListener {
                    createFile()
                }
            })
            add(JMenuItem("Open file").apply {
                addActionListener {
                    openFile()
                }
            })
            add(JMenuItem("Save file").apply {
                addActionListener {
                    saveChanges()
                }
            })
            add(JMenuItem("Save file as").apply {
                addActionListener {
                    saveFileAs()
                }
            })
            add(JMenuItem("Exit").apply {
                addActionListener {
                    showConfirmationDialog(
                        content = "You may have unsaved changes. Proceed closing?",
                        onConfirm = {
                            dispose()
                        },
                        onDecline = {}
                    )
                }
            })
        }
        val editMenu = JMenu("Edit").apply {
            add(JMenuItem("Undo").apply {
                addActionListener {
                    if (manager.canUndo())
                        manager.undo()
                }
            })
            add(JMenuItem("Redo").apply {
                addActionListener {
                    if (manager.canRedo())
                        manager.redo()
                }
            })
            add(JMenuItem("Cut").apply {
                addActionListener {
                    inputTextArea.cut()
                }
            })
            add(JMenuItem("Copy").apply {
                addActionListener {
                    inputTextArea.copy()
                }
            })
            add(JMenuItem("Paste").apply {
                addActionListener {
                    inputTextArea.paste()
                }
            })
            add(JMenuItem("Remove").apply {
                addActionListener {
                    inputTextArea.replaceSelection("")
                }
            })
            add(JMenuItem("Select All").apply {
                addActionListener {
                    inputTextArea.selectAll()
                }
            })
        }
        val textMenu = JMenu("Text").apply {
            add(JMenuItem("Problem").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Grammar").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Grammar classification").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Analysis method").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Diagnostic").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Example").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Literature sources").apply {
                addActionListener {

                }
            })
            add(JMenuItem("Code sources").apply {
                addActionListener {

                }
            })
        }
        val runMenu = JMenu("Run").apply {
            add(JMenuItem("Run").apply {
                addActionListener {

                }
            })
        }
        val helpMenu = JMenu("Help").apply {
            add(JMenuItem("Help").apply {
                addActionListener {
                    val help = HelpView()
                    help.pack()
                    help.isVisible = true
                }
            })
            add(JMenuItem("About").apply {
                addActionListener {
                    JOptionPane.showMessageDialog(
                        this@MainView.parent,
                        """
                            At this stage this program is a simple version of text editor
                            in which you can do basic operations such as text input, editing, saving etc.
                        """.trimIndent(),
                        "About",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                }
            })
        }

        val menuBar: JMenuBar = JMenuBar().apply {
            add(fileMenu)
            add(editMenu)
            add(textMenu)
            add(runMenu)
            add(helpMenu)
        }

        val instruments = JPanel().apply {
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/new.png")), "New file")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    createFile()
                }
            })
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/open.png")), "Open file")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    openFile()
                }
            })
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/save.png")), "Save file")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    saveChanges()
                }
            })

            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/undo.png")), "Undo")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    if (manager.canUndo())
                        manager.undo()
                }
            })
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/redo.png")), "Redo")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    if (manager.canRedo())
                        manager.redo()
                }
            })
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/copy.png")), "Copy")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    inputTextArea.copy()
                }
            })
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/cut.png")), "Cut")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    inputTextArea.cut()
                }
            })
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/paste.png")), "Paste")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    inputTextArea.paste()
                }
            })
        }

        contentPane.apply {
            this@MainView.jMenuBar = menuBar
            add(instruments)
            add(inputTextArea)
            add(outputTextArea)
        }

        mainViewController.fileServiceEventsObservable
            .subscribe {
                when (it) {
                    is FileService.FileServiceEvent.FileChanged -> title = "Kompiler " + it.file?.absolutePath
                }
            }

        mainViewController.errorObservable
            .subscribe {
                showErrorDialog(content = it.localizedMessage)
            }
    }

    private fun createFile() {
        if (inputTextArea.text.isNotEmpty()) {
            showConfirmationDialog(
                "Confirm operation",
                "You have unsaved changes",
                onConfirm = {
                    showFilenameInputDialog("Create", "Create new file") {
                        mainViewController.createFile(it, this)
                    }
                },
                onDecline = {}
            )
        } else {
            showFilenameInputDialog("Create", "Create new file") {
                mainViewController.createFile(it, this)
            }
        }
    }

    private fun openFile() {
        if (inputTextArea.text.isNotEmpty()) {
            showConfirmationDialog(
                "Confirm operation",
                "You have unsaved changes",
                onConfirm = {
                    inputTextArea.text = mainViewController.openFile(this) ?: inputTextArea.text
                },
                onDecline = {}
            )
        } else {
            inputTextArea.text = mainViewController.openFile(this) ?: inputTextArea.text
        }
    }

    private fun saveChanges() {
        mainViewController.saveFile(inputTextArea.text, this) { saveFileAs() }
    }

    private fun saveFileAs() {
        showFilenameInputDialog("Save As", "Save current file as") {
            mainViewController.saveFileAs(it, inputTextArea.text, this)
        }
    }

    private fun showFilenameInputDialog(
        title: String,
        content: String,
        onValidFilename: (String) -> Unit
    ) = TextInputDialog(
        title,
        content,
        Regex("^[\\w,\\s-]+\\.[A-Za-z]+\$"),
        onValidFilename
    ).setVisible(true)

    private fun showConfirmationDialog(
        title: String = "Confirm action",
        content: String,
        onConfirm: () -> Unit,
        onDecline: () -> Unit
    ) {
        val dialogResult = JOptionPane.showConfirmDialog(
            this@MainView.parent,
            content,
            title,
            JOptionPane.OK_CANCEL_OPTION
        )
        if (dialogResult == JOptionPane.OK_OPTION) {
            onConfirm()
        } else {
            onDecline()
        }
    }

    private fun showErrorDialog(title: String = "Error", content: String) = JOptionPane.showMessageDialog(
        this@MainView.parent,
        content,
        title,
        JOptionPane.ERROR_MESSAGE
    )
}