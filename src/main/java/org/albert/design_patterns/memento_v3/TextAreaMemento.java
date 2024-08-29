package org.albert.design_patterns.memento_v3;

import org.albert.util.OperationType;

public class TextAreaMemento
{
    final int offset;
    final int length;
    final String text;
    final OperationType operationType;
    final int caretPosition;
    final boolean isReplacement;
    final String replacementText;

    public TextAreaMemento(int offset, int length, String text, OperationType operationType, int caretPosition, boolean isReplacement, String replacementText)
    {
        this.offset = offset;
        this.length = length;
        this.text = text;
        this.operationType = operationType;
        this.caretPosition = caretPosition;
        this.isReplacement = isReplacement;
        this.replacementText = replacementText;
    }
}