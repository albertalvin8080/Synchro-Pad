package org.albert.design_patterns.memento;

public class TextAreaMemento
{
    public final byte[] textBytes;
    public final int caretPosition;

    public TextAreaMemento(byte[] textBytes, int caretPosition)
    {
        this.textBytes = textBytes;
        this.caretPosition = caretPosition;
    }
}