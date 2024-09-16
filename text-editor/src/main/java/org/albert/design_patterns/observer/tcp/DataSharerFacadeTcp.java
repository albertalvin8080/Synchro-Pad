package org.albert.design_patterns.observer.tcp;

import org.albert.design_patterns.observer.StateChangeObserver;
import org.albert.design_patterns.observer.multicast.DataSharerMulticast;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;

public class DataSharerFacadeTcp implements StateChangeObserver
{
    private static DataSharerFacadeTcp INSTANCE;

    public static DataSharerFacadeTcp getInstance(JTextArea textArea)
    {
        if (DataSharerFacadeTcp.INSTANCE == null)
            DataSharerFacadeTcp.INSTANCE = new DataSharerFacadeTcp(textArea);
        return DataSharerFacadeTcp.INSTANCE;
    }

    private final JTextArea textArea;
    private DataSharerTcp dataSharer;

    private DataSharerFacadeTcp(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    public UUID getUuid()
    {
        return this.dataSharer.getUuid();
    }

    public void openConnection()
    {
        if (this.dataSharer != null)
            return;

        try
        {
            dataSharer = new DataSharerTcp(textArea);
        }
        catch (IOException | InterruptedException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection()
    {
        if (this.dataSharer == null)
            return;
        dataSharer.destroy();
        dataSharer = null;
    }

    @Override
    public void onInsert(int offset, int length, String text)
    {
        if(dataSharer == null) return;
        dataSharer.onInsert(offset, length, text);
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        if(dataSharer == null) return;
        dataSharer.onDelete(offset, length, text);
    }
}
