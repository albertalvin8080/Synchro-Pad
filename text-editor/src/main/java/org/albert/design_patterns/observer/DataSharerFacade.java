package org.albert.design_patterns.observer;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;

public class DataSharerFacade implements StateChangeObserver
{
    private static DataSharerFacade INSTANCE;

    public static DataSharerFacade getInstance(JTextArea textArea)
    {
        if (DataSharerFacade.INSTANCE == null)
            DataSharerFacade.INSTANCE = new DataSharerFacade(textArea);
        return DataSharerFacade.INSTANCE;
    }

    private final JTextArea textArea;
    private DataSharerStateChangeObserver dataSharer;

    private DataSharerFacade(JTextArea textArea)
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
            dataSharer = new DataSharerStateChangeObserver(textArea);
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection()
    {
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
