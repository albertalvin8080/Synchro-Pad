package org.albert.design_patterns.memento;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class TextAreaOriginator
{
    private final JTextArea textArea;

    public TextAreaOriginator(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    public TextAreaMemento createMemento()
    {
        return new TextAreaMemento(
                textArea.getText().getBytes(StandardCharsets.UTF_8),
                textArea.getCaretPosition()
        );
    }

    public void restoreMemento(TextAreaMemento memento)
    {
        textArea.setText(new String(memento.textBytes, StandardCharsets.UTF_8));
        textArea.setCaretPosition(memento.caretPosition);
    }
}