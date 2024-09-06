package org.albert.design_patterns.observer;

import org.albert.design_patterns.memento_v2.TextAreaCaretaker;
import org.albert.util.OperationType;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DataSharerDocumentFilter extends DocumentFilter
{
    private final DataSharerFacade dataSharerFacade;

    public DataSharerDocumentFilter(DataSharerFacade dataSharerFacade)
    {
        this.dataSharerFacade = dataSharerFacade;
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
        if(!Thread.currentThread().getName().equals("AWT-EventQueue-0")) return;

        shareData(offset, length, text, operationType);
    }

    private void shareData(int offset, int length, String text, OperationType operationType)
    {
        if (operationType == OperationType.INSERT)
        {
            dataSharerFacade.onInsert(offset, length, text);
        }
        else if (operationType == OperationType.DELETE)
        {
            dataSharerFacade.onDelete(offset, length, text);
        }
    }
}