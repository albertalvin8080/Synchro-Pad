package org.albert.design_patterns.memento;

import javax.swing.*;

public class TextAreaOriginator
{
    private final JTextArea textArea;

    public TextAreaOriginator(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    public TextAreaMemento createMemento()
    {
        return new TextAreaMemento(textArea.getText(), textArea.getCaretPosition());
    }

    public void restoreMemento(TextAreaMemento memento)
    {
        textArea.setText(memento.getText());
        textArea.setCaretPosition(memento.getCaretPosition());
    }
}
