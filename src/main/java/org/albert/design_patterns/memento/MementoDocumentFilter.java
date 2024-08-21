package org.albert.design_patterns.memento;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MementoDocumentFilter extends DocumentFilter
{
    private final TextAreaCaretaker textAreaCaretaker;
    private boolean justSaved;

    public MementoDocumentFilter(TextAreaCaretaker textAreaCaretaker)
    {
        this.textAreaCaretaker = textAreaCaretaker;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
        if (text.isEmpty())
        {
            super.replace(fb, offset, length, text, attrs);
            return;
        }

        final char c = text.charAt(0);
        if (text.length() > 1 || (!Character.isLetterOrDigit(c) && !justSaved))
        {
            performStateChange();
//            System.out.println("SAVED");
            justSaved = true;
        }
        // Prevents from loop saving due to repetitive non digit character
        else if (Character.isLetterOrDigit(c))
        {
            justSaved = false;
        }

        super.replace(fb, offset, length, text, attrs);
    }

    private void performStateChange()
    {
        // Prevents saving the old state as a new state.
        if (textAreaCaretaker.getStateChange())
        {
            textAreaCaretaker.setStateChange(false);
        }
        else
        {
            textAreaCaretaker.saveState();
        }
    }

    //    @Override
//    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
//    {
//        System.out.println("INSERT");
//        performStateChange();
//        super.insertString(fb, offset, string, attr);
//    }
//
//    @Override
//    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
//    {
//        System.out.println("REMOVE");
//        performStateChange();
//        super.remove(fb, offset, length);
//    }
}