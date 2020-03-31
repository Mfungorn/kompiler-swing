package presentation

import java.awt.FlowLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


internal class TextInputDialog(
    title: String,
    content: String,
    private val regex: Regex,
    onValidInput: (String) -> Unit
) : JDialog(JFrame(), title, true), DocumentListener {
    private val textField: JTextField = JTextField(20).apply {
        setSize(200, 40)
    }
    private val enterButton: JButton = JButton("Enter").apply {
        isEnabled = false
        addActionListener {
            onValidInput(textField.text)
            clearAndHide()
        }
    }

    init {
        layout = FlowLayout(FlowLayout.LEFT, 20, 20)

        add(textField)
        add(enterButton)
        setSize(300, 150)

        //Ensure the text field always gets the first focus.
        addComponentListener(object : ComponentAdapter() {
            override fun componentShown(ce: ComponentEvent) {
                textField.requestFocusInWindow()
            }
        })
        textField.document.addDocumentListener(this)
    }

    private fun clearAndHide() {
        textField.text = null
        isVisible = false
    }

    private fun onChange() {
        val text = textField.text
        enterButton.isEnabled = text.matches(regex)
    }

    override fun changedUpdate(e: DocumentEvent?) {
        onChange()
    }

    override fun insertUpdate(e: DocumentEvent?) {
        onChange()
    }

    override fun removeUpdate(e: DocumentEvent?) {
        onChange()
    }
}