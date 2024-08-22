package org.albert.design_patterns.memento;

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
        System.out.println("--------------------------");
        System.out.println("REPLACE");
        System.out.println("offset: " + offset);
        System.out.println("length: " + length);
        System.out.println("text: " + text);

        if (text.isEmpty())
        {
            super.replace(fb, offset, length, text, attrs);
            return;
        }

//        final char c = text.charAt(0);
        // Causes conflict when characters are not inserted at the end.
        // The presence of the whitespace characters is necessary to avoid it.
//        if (!Character.isWhitespace(c))
//        {
//            performStateChange(offset, length, text, OperationType.INSERT);
//            System.out.println("INSERTED");
//        }

        performStateChange(offset, length, text, OperationType.INSERT);
        System.out.println("INSERTED");

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
        System.out.println("--------------------------");
        System.out.println("REMOVE");
        System.out.println("offset: " + offset);
        System.out.println("length: " + length);
        performStateChange(offset, length, null, OperationType.DELETE);
        System.out.println("DELETED");
        super.remove(fb, offset, length);
    }

    private void performStateChange(int offset, int length, String text, OperationType operationType)
    {
//         Prevents saving the old state as a new state.
        if (textAreaCaretaker.getStateChange())
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

    //    @Override
//    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
//    {
//        System.out.println("INSERT");
//        performStateChange();
//        super.insertString(fb, offset, string, attr);
//    }
}