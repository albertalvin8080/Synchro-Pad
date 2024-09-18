package org.albert.design_patterns.observer;

import org.albert.CompilerProperties;
import org.albert.InstanceMain;
import org.albert.design_patterns.observer.tcp.DataSharerFacadeTcp;
import org.albert.util.OperationType;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.io.EOFException;
import java.net.SocketException;

public class DataSharerDocumentFilter extends DocumentFilter
{
    private final DataSharerFacadeTcp dataSharerFacadeTcp;

    public DataSharerDocumentFilter(DataSharerFacadeTcp dataSharerFacadeTcp)
    {
        this.dataSharerFacadeTcp = dataSharerFacadeTcp;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
//        System.out.println("--------------------------");
//        System.out.println("REPLACE");
//        System.out.println("offset: " + offset);
//        System.out.println("length: " + length);
//        System.out.println("text: " + text);

        final boolean permission = dataSharerFacadeTcp.requestWritePermission();
        if(!permission) return;

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

        final boolean permission = dataSharerFacadeTcp.requestWritePermission();
        if(!permission) return;

        performStateChange(offset, length, null, OperationType.DELETE);
//        System.out.println("DELETED");

        super.remove(fb, offset, length);
    }

    private void performStateChange(int offset, int length, String text, OperationType operationType)
    {
        if (CompilerProperties.DEBUG)
            System.out.println("DocumentFilter Thread -> " + Thread.currentThread().getName());
        // Only the AWT-EventQueue-0 thread handles user input.
        // Any other thread is just receiving data from the server.
        if (!Thread.currentThread().getName().equals("AWT-EventQueue-0")) return;

        shareData(offset, length, text, operationType);
    }

    private void shareData(int offset, int length, String text, OperationType operationType)
    {
        if (operationType == OperationType.INSERT) dataSharerFacadeTcp.onInsert(offset, length, text);
        else if (operationType == OperationType.DELETE) dataSharerFacadeTcp.onDelete(offset, length, text);
    }
}