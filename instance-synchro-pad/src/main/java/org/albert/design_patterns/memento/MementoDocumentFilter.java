package org.albert.design_patterns.memento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MementoDocumentFilter extends DocumentFilter
{
    private static final Logger logger = LoggerFactory.getLogger(MementoDocumentFilter.class);

    private final TextAreaCaretaker textAreaCaretaker;
    private boolean justSaved;
    private int deletedCount;

    public MementoDocumentFilter(TextAreaCaretaker textAreaCaretaker)
    {
        this.textAreaCaretaker = textAreaCaretaker;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
//        logger.debug("--------------------------");
//        logger.debug("REPLACE");
//        logger.debug("offset: {}", offset);
//        logger.debug("length: {}", length);
//        logger.debug("text: {}", text);

        if (text.isEmpty())
        {
            super.replace(fb, offset, length, text, attrs);
            return;
        }

        final char c = text.charAt(0);
        if (text.length() > 1 || (!Character.isLetterOrDigit(c) && !justSaved))
        {
            performStateChange();
//            logger.info("SAVED");
            justSaved = true;
        }
        // Prevents from loop saving due to repetitive non digit character
        else if (Character.isLetterOrDigit(c))
        {
            justSaved = false;
        }

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
//        logger.debug("--------------------------");
//        logger.debug("REMOVE");
//        logger.debug("offset: {}", offset);
//        logger.debug("length: {}", length);


        if (length > 1 || deletedCount > 5)
        {
            performStateChange();
//            logger.info("DELETED");
            deletedCount = 0;
        }
        else ++deletedCount;
        super.remove(fb, offset, length);
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
//        logger.info("INSERT");
//        performStateChange();
//        super.insertString(fb, offset, string, attr);
//    }
//
}