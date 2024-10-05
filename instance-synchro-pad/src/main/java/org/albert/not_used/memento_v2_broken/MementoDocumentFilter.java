package org.albert.not_used.memento_v2_broken;

import org.albert.util.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MementoDocumentFilter extends DocumentFilter
{
    private static final Logger logger = LoggerFactory.getLogger(MementoDocumentFilter.class);

    private final TextAreaCaretaker textAreaCaretaker;

    public MementoDocumentFilter(TextAreaCaretaker textAreaCaretaker)
    {
        this.textAreaCaretaker = textAreaCaretaker;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
        logger.info("--------------------------");
        logger.info("REPLACE");
        logger.info("offset: " + offset);
        logger.info("length: " + length);
        logger.info("text: " + text);

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
//            logger.info("INSERTED");
//        }

        performStateChange(offset, length, text, OperationType.INSERT);
        logger.info("INSERTED");

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
        logger.info("--------------------------");
        logger.info("REMOVE");
        logger.info("offset: " + offset);
        logger.info("length: " + length);
        performStateChange(offset, length, null, OperationType.DELETE);
        logger.info("DELETED");
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
//        logger.info("INSERT");
//        performStateChange();
//        super.insertString(fb, offset, string, attr);
//    }
}