package org.albert.design_patterns.memento;

public class TextAreaMemento
{
    private final String text;
    private final int caretPosition;

    public TextAreaMemento(String text, int caretPosition)
    {
        this.text = text;
        this.caretPosition = caretPosition;
    }

    public String getText()
    {
        return text;
    }

    public int getCaretPosition()
    {
        return caretPosition;
    }
}