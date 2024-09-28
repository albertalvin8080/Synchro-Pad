package org.albert.design_patterns.memento_v2;

import org.albert.util.OperationType;

public class TextAreaMemento
{
    final int offset;
    // The length can be retrieved using memento.text.length();
//    final int length;
    final String text;
    final OperationType operationType;
    // Unnecessary when using AbstractDocument.replace() instead of JTextArea.setText()
//    final int caretPosition;
    final String replacementText;

    public TextAreaMemento(int offset, String text, OperationType operationType, String replacementText)
    {
        this.offset = offset;
        this.text = text;
        this.operationType = operationType;
//        this.caretPosition = caretPosition;
        this.replacementText = replacementText;
    }
}