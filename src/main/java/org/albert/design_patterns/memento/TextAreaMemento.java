package org.albert.design_patterns.memento;

import org.albert.util.OperationType;

public class TextAreaMemento
{
    final int offset;
    final int length;
    final String text;
    final OperationType operationType;
    final int caretPosition;

    public TextAreaMemento(int offset, int length, String text, OperationType operationType, int caretPosition)
    {
        this.offset = offset;
        this.length = length;
        this.text = text;
        this.operationType = operationType;
        this.caretPosition = caretPosition;
    }
}