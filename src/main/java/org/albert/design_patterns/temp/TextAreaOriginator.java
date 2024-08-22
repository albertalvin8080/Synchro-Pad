package org.albert;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class TextAreaOriginator
{
    private final JTextArea textArea;
//    private byte[] lastTextBytes;

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
        textArea.setText(memento.getText());
        textArea.setText(new String(memento.getTextBytes(), StandardCharsets.UTF_8));
        textArea.setCaretPosition(memento.getCaretPosition());
    }
}