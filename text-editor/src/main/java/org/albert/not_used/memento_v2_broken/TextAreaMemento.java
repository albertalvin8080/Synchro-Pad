package org.albert.not_used.memento_v2_broken;

import org.albert.util.OperationType;

public class TextAreaMemento
{
    final int offset;
    final int length;
    final String text;
    final OperationType operationType;
    final int caretPosition;
    final TextAreaMemento replacement;

    public TextAreaMemento(int offset, int length, String text, OperationType operationType, int caretPosition, TextAreaMemento replacement)
    {
        this.offset = offset;
        this.length = length;
        this.text = text;
        this.operationType = operationType;
        this.caretPosition = caretPosition;
        this.replacement = replacement;
    }
}