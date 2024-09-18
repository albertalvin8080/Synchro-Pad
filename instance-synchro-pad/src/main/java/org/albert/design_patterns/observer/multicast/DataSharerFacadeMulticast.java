package org.albert.design_patterns.observer.multicast;

import org.albert.design_patterns.observer.StateChangeObserver;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;

public class DataSharerFacadeMulticast implements StateChangeObserver
{
    private static DataSharerFacadeMulticast INSTANCE;

    public static DataSharerFacadeMulticast getInstance(JTextArea textArea)
    {
        if (DataSharerFacadeMulticast.INSTANCE == null)
            DataSharerFacadeMulticast.INSTANCE = new DataSharerFacadeMulticast(textArea);
        return DataSharerFacadeMulticast.INSTANCE;
    }

    private final JTextArea textArea;
    private DataSharerMulticast dataSharer;

    private DataSharerFacadeMulticast(JTextArea textArea)
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
            dataSharer = new DataSharerMulticast(textArea);
        }
        catch (IOException | InterruptedException e)
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
