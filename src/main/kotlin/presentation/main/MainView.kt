package presentation.main

import org.koin.core.KoinComponent
import org.koin.core.inject
import presentation.TextInputDialog
import presentation.help.HelpView
import service.FileService
import utils.RedoAction
import utils.RemoveSelectedAction
import utils.TextLineNumber
import utils.UndoAction
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
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

        val panel = JPanel()
        val gridBagLayout = GridBagLayout()
        val constraints = GridBagConstraints()
        panel.layout = gridBagLayout

        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                showConfirmationDialog(
                    content = "You may have unsaved changes. Proceed closing?",
                    onConfirm = {
                        dispose()
                    },
                    onDecline = {
                        // do nothing
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
            val removeSelectedKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK)
            val undoKetStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
            val redoKetStroke =
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)

            keymap.also {
                it.addActionForKeyStroke(copyKetStroke, TransferHandler.getCopyAction())
                it.addActionForKeyStroke(pasteKetStroke, TransferHandler.getPasteAction())
                it.addActionForKeyStroke(cutKetStroke, TransferHandler.getCutAction())
                it.addActionForKeyStroke(removeSelectedKetStroke, RemoveSelectedAction(this))
                it.addActionForKeyStroke(undoKetStroke, UndoAction(manager))
                it.addActionForKeyStroke(redoKetStroke, RedoAction(manager))
            }
        }
        val inputTextScrollPane = JScrollPane(inputTextArea).apply {
            maximumSize = Dimension(10000, 600)
            preferredSize = Dimension(0, 400)
            minimumSize = Dimension(800, 400)
            setRowHeaderView(TextLineNumber(inputTextArea))

            requestFocus()
        }

        outputTextArea = JTextArea().apply {
            isEditable = false
            isFocusable = false
            preferredSize = Dimension(800, 100)
            minimumSize = Dimension(800, 100)
        }

        val fileMenu = JMenu("File").apply {
            add(JMenuItem("Create new").apply {
                accelerator = KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    createFile()
                }
            })
            add(JMenuItem("Open file").apply {
                accelerator = KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    openFile()
                }
            })
            add(JMenuItem("Save file").apply {
                accelerator = KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    saveChanges()
                }
            })
            add(JMenuItem("Save file as").apply {
                accelerator = KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)
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
                accelerator = KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    if (manager.canUndo())
                        manager.undo()
                }
            })
            add(JMenuItem("Redo").apply {
                accelerator = KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)
                addActionListener {
                    if (manager.canRedo())
                        manager.redo()
                }
            })
            add(JMenuItem("Cut").apply {
                accelerator = KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    inputTextArea.cut()
                }
            })
            add(JMenuItem("Copy").apply {
                accelerator = KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    inputTextArea.copy()
                }
            })
            add(JMenuItem("Paste").apply {
                accelerator = KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    inputTextArea.paste()
                }
            })
            add(JMenuItem("Remove").apply {
                accelerator = KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK)
                addActionListener {
                    inputTextArea.replaceSelection("")
                }
            })
            add(JMenuItem("Select All").apply {
                accelerator = KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK)
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
                    mainViewController.test(inputTextArea.text)
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
        val toolbar = JToolBar(JToolBar.HORIZONTAL).apply {
            isFloatable = false
            add(JButton(
                ImageIcon(ImageIO.read(this@MainView::class.java.getResource("/new.png")), "New file")
            ).apply {
                preferredSize = Dimension(24, 24)
                addActionListener {
                    createFile()
                }
            })
            add(
                JButton(
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

        val textSplitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            inputTextScrollPane,
            JScrollPane(outputTextArea)
        ).apply {
            setDividerLocation(0.3)
            resetToPreferredSizes()
        }

        jMenuBar = menuBar

        constraints.apply {
            gridx = 0
            gridy = 0
            gridwidth = 20
            gridheight = 1
            fill = GridBagConstraints.BOTH
            weightx = 1.0
            weighty = 0.0
            anchor = GridBagConstraints.NORTH
        }
        gridBagLayout.setConstraints(toolbar, constraints)
        panel.add(toolbar)

        constraints.apply {
            gridx = 0
            gridy = 1
            gridwidth = 20
            gridheight = 19
            fill = GridBagConstraints.BOTH
            weightx = 1.0
            weighty = 1.0
            anchor = GridBagConstraints.NORTH
        }
        gridBagLayout.setConstraints(textSplitPane, constraints)
        panel.add(textSplitPane)

        contentPane = panel
        pack()

        mainViewController.fileServiceEventsObservable
            .subscribe {
                when (it) {
                    is FileService.FileServiceEvent.FileChanged -> title = "Kompiler " + it.file?.absolutePath
                }
            }

        mainViewController.outputObservable
            .subscribe {
                outputTextArea.text = it
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