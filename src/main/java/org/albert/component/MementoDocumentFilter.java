package org.albert.component;

import org.albert.design_patterns.memento.TextAreaCaretaker;

import javax.swing.text.*;

public class MementoDocumentFilter extends DocumentFilter
{
    private final TextAreaCaretaker textAreaCaretaker;
//    private int replaceCount = 0;

    public MementoDocumentFilter(TextAreaCaretaker textAreaCaretaker)
    {
        this.textAreaCaretaker = textAreaCaretaker;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
        performStateChange();
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