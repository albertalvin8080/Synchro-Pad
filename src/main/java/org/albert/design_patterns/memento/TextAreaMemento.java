package org.albert.design_patterns.memento;

import org.albert.util.OperationType;

public class TextAreaMemento
{
    private final int offset;
    private final int length;
    private final String text;
    private final OperationType operationType;
    private final int caretPosition;

    public TextAreaMemento(int offset, int length, String text, OperationType operationType, int caretPosition)
    {
        this.offset = offset;
        this.length = length;
        this.text = text;
        this.operationType = operationType;
        this.caretPosition = caretPosition;
    }

    public int getCaretPosition()
    {
        return caretPosition;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getLength()
    {
        return length;
    }

    public String getText()
    {
        return text;
    }

    public OperationType getOperationType()
    {
        return operationType;
    }
}