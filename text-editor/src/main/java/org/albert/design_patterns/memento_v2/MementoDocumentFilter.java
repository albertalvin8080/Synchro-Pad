package org.albert.design_patterns.memento_v2;

import org.albert.util.OperationType;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MementoDocumentFilter extends DocumentFilter
{
    private final TextAreaCaretaker textAreaCaretaker;

    public MementoDocumentFilter(TextAreaCaretaker textAreaCaretaker)
    {
        this.textAreaCaretaker = textAreaCaretaker;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
//        System.out.println("--------------------------");
//        System.out.println("REPLACE");
//        System.out.println("offset: " + offset);
//        System.out.println("length: " + length);
//        System.out.println("text: " + text);

        if (text.isEmpty())
        {
            super.replace(fb, offset, length, text, attrs);
            return;
        }

        performStateChange(offset, length, text, OperationType.INSERT);
//        System.out.println("INSERTED");

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
//        System.out.println("--------------------------");
//        System.out.println("REMOVE");
//        System.out.println("offset: " + offset);
//        System.out.println("length: " + length);

        performStateChange(offset, length, null, OperationType.DELETE);
//        System.out.println("DELETED");

        super.remove(fb, offset, length);
    }

    private void performStateChange(int offset, int length, String text, OperationType operationType)
    {
        final boolean stateChange = textAreaCaretaker.getStateChange();

//         Prevents saving the old state as a new state.
        if (stateChange)
        {
            textAreaCaretaker.setStateChange(false);
        }
        else
        {
            textAreaCaretaker.saveState(offset, length, text, operationType);
        }
    }

    public TextAreaCaretaker getTextAreaCaretaker()
    {
        return textAreaCaretaker;
    }
}