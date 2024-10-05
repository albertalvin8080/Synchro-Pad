package org.albert.design_patterns.memento_v2;

import org.albert.util.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MementoDocumentFilter extends DocumentFilter
{
    private static final Logger logger = LoggerFactory.getLogger(org.albert.design_patterns.memento_v2.MementoDocumentFilter.class);
    private final TextAreaCaretaker textAreaCaretaker;

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

        performStateChange(offset, length, text, OperationType.INSERT);
//        logger.info("INSERTED");

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
//        logger.debug("--------------------------");
//        logger.debug("REMOVE");
//        logger.debug("offset: {}", offset);
//        logger.debug("length: {}", length);

        performStateChange(offset, length, null, OperationType.DELETE);
//        logger.info("DELETED");

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