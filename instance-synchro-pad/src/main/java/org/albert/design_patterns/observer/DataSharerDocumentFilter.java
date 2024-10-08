package org.albert.design_patterns.observer;

import org.albert.util.CompilerProperties;
import org.albert.design_patterns.observer.tcp.DataSharerFacadeTcp;
import org.albert.util.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DataSharerDocumentFilter extends DocumentFilter
{
    private static final Logger logger = LoggerFactory.getLogger(DataSharerDocumentFilter.class);
    private final DataSharerFacadeTcp dataSharerFacadeTcp;

    public DataSharerDocumentFilter(DataSharerFacadeTcp dataSharerFacadeTcp)
    {
        this.dataSharerFacadeTcp = dataSharerFacadeTcp;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
//        logger.info("--------------------------");
//        logger.info("REPLACE");
//        logger.info("offset: {}", offset);
//        logger.info("length: {}", length);
//        logger.info("text: {}", text);

        if (text.isEmpty())
        {
            super.replace(fb, offset, length, text, attrs);
            return;
        }

        if (Thread.currentThread().getName().equals("AWT-EventQueue-0"))
        {
            final boolean permission = dataSharerFacadeTcp.requestWritePermission();
            if (!permission) return;
            performStateChange(offset, length, text, OperationType.INSERT);
        }

        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
//        logger.info("--------------------------");
//        logger.info("REMOVE");
//        logger.info("offset: {}", offset);
//        logger.info("length: {}", length);

        if (Thread.currentThread().getName().equals("AWT-EventQueue-0"))
        {
            final boolean permission = dataSharerFacadeTcp.requestWritePermission();
            if (!permission) return;
            performStateChange(offset, length, null, OperationType.DELETE);
        }

        super.remove(fb, offset, length);
    }

    private void performStateChange(int offset, int length, String text, OperationType operationType)
    {
        // Only the AWT-EventQueue-0 thread handles user input.
        // Any other thread is just receiving data from the server.
        if (CompilerProperties.DEBUG)
            logger.info("DocumentFilter Thread -> {}", Thread.currentThread().getName());

        // In case you want to implement undo/redo afterward
        shareData(offset, length, text, operationType);
    }

    private void shareData(int offset, int length, String text, OperationType operationType)
    {
        if (operationType == OperationType.INSERT) dataSharerFacadeTcp.onInsert(offset, length, text);
        else if (operationType == OperationType.DELETE) dataSharerFacadeTcp.onDelete(offset, length, text);
    }
}