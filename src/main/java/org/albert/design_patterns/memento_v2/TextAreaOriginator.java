package org.albert.design_patterns.memento_v2;

import org.albert.util.OperationType;

import javax.swing.*;

public class TextAreaOriginator
{
    private final JTextArea textArea;

    public TextAreaOriginator(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    public TextAreaMemento createMemento(int offset, int length, String text, OperationType operationType)
    {
        if(operationType == OperationType.INSERT)
        {
            length = text.length();
        }
        // `&& text == null` check is in case the memento being saved is a previous redo/undo one.
        else if(operationType == OperationType.DELETE && text == null)
        {
            // Necessary because remove() from the filter doesn't return the string being removed.
            text = textArea.getText().substring(offset, offset + length);
        }

        return new TextAreaMemento(
                offset, length, text, operationType, textArea.getCaretPosition()
        );
    }

    public void restoreMemento(TextAreaMemento memento)
    {
        // WARNING: if the operation was INSERT, then you need to remove from the textArea.
        // WARNING: if the operation was DELETE, then you need to insert into the textArea.
        if (memento.operationType == OperationType.INSERT)
        {
            final StringBuilder sb = new StringBuilder(textArea.getText());
            final int offset = memento.offset;
            final int length = memento.length;
            sb.delete(offset, offset + length);
            textArea.setText(sb.toString());
        }
        else if(memento.operationType == OperationType.DELETE)
        {
            final StringBuilder sb = new StringBuilder(textArea.getText());
            final int offset = memento.offset;
            final String text = memento.text;
            sb.insert(offset, text);
            textArea.setText(sb.toString());
        }
        textArea.setCaretPosition(memento.caretPosition);
    }
}
