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

import java.util.LinkedList;
import java.util.Queue;

public class DataSharerDocumentFilter extends DocumentFilter
{
    private final DataSharerFacadeTcp dataSharerFacadeTcp;
    private volatile boolean awaitingPermission = false;
    private final Queue<Runnable> pendingOperations = new LinkedList<>(); // To store pending operations

    public DataSharerDocumentFilter(DataSharerFacadeTcp dataSharerFacadeTcp)
    {
        this.dataSharerFacadeTcp = dataSharerFacadeTcp;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
        if (text.isEmpty())
        {
            super.replace(fb, offset, length, text, attrs);
            return;
        }

        if (Thread.currentThread().getName().equals("AWT-EventQueue-0"))
        {
            if (awaitingPermission)
            {
                // Buffer the operation until permission is granted
                pendingOperations.add(() -> {
                    performStateChange(offset, length, text, OperationType.INSERT);
                    try
                    {
                        super.replace(fb, offset, length, text, attrs);
                    }
                    catch (BadLocationException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
                return;
            }

            awaitingPermission = true;
            dataSharerFacadeTcp.requestWritePermissionAsync((granted) -> {
                awaitingPermission = false;
                if (granted)
                {
                    try
                    {
                        super.replace(fb, offset, length, text, attrs);
                    }
                    catch (BadLocationException e)
                    {
                        throw new RuntimeException(e);
                    }
                    performStateChange(offset, length, text, OperationType.INSERT);
                    // Process any pending operations
                    processPendingOperations();
                }
            });
        }
        else
        {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
    {
        if (Thread.currentThread().getName().equals("AWT-EventQueue-0"))
        {
            if (awaitingPermission)
            {
                // Buffer the operation until permission is granted
                pendingOperations.add(() -> {
                    try
                    {
                        super.remove(fb, offset, length);
                    }
                    catch (BadLocationException e)
                    {
                        throw new RuntimeException(e);
                    }
                    performStateChange(offset, length, null, OperationType.DELETE);
                });
                return;
            }

            awaitingPermission = true;
            dataSharerFacadeTcp.requestWritePermissionAsync((granted) -> {
                awaitingPermission = false;
                if (granted)
                {
                    try
                    {
                        super.remove(fb, offset, length);
                    }
                    catch (BadLocationException e)
                    {
                        throw new RuntimeException(e);
                    }
                    performStateChange(offset, length, null, OperationType.DELETE);
                    // Process any pending operations
                    processPendingOperations();
                }
            });
        }
        else
        {
            super.remove(fb, offset, length);
        }

    }

    private void performStateChange(int offset, int length, String text, OperationType operationType)
    {
        if (CompilerProperties.DEBUG)
            System.out.println("DocumentFilter Thread -> " + Thread.currentThread().getName());

        shareData(offset, length, text, operationType);
    }

    private void shareData(int offset, int length, String text, OperationType operationType)
    {
        if (operationType == OperationType.INSERT) dataSharerFacadeTcp.onInsert(offset, length, text);
        else if (operationType == OperationType.DELETE) dataSharerFacadeTcp.onDelete(offset, length, text);
    }

    private void processPendingOperations()
    {
        while (!pendingOperations.isEmpty())
        {
            pendingOperations.poll().run();
        }
    }
}
