package org.albert.design_patterns.observer.tcp;

import org.albert.component.TextEditor;
import org.albert.design_patterns.observer.StateChangeObserver;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;

public class DataSharerFacadeTcp implements StateChangeObserver
{
    private static DataSharerFacadeTcp INSTANCE;

    public static DataSharerFacadeTcp getInstance(TextEditor textEditor, JTextArea textArea)
    {
        if (DataSharerFacadeTcp.INSTANCE == null)
            DataSharerFacadeTcp.INSTANCE = new DataSharerFacadeTcp(textEditor, textArea);
        return DataSharerFacadeTcp.INSTANCE;
    }

    private final TextEditor textEditor;
    private final JTextArea textArea;
    private DataSharerTcp dataSharer;

    private DataSharerFacadeTcp(TextEditor textEditor, JTextArea textArea)
    {
        this.textEditor = textEditor;
        this.textArea = textArea;
    }

    public UUID getUuid()
    {
        return this.dataSharer.getUuid();
    }

    public void openConnection(String serverIp) throws IOException, InterruptedException, ClassNotFoundException
    {
        if (this.dataSharer != null)
            return;
        dataSharer = new DataSharerTcp(textArea, serverIp);
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
        if (dataSharer == null) return;
        try
        {
            dataSharer.onInsert(offset, length, text);
        }
        catch (IOException e) // Socket Closed, for example
        {
            handleSocketClosed();
        }
    }

    @Override
    public void onDelete(int offset, int length, String text)
    {
        if (dataSharer == null) return;
        try
        {
            dataSharer.onDelete(offset, length, text);
        }
        catch (IOException e) // Socket Closed, for example
        {
            handleSocketClosed();
        }
    }

    private void handleSocketClosed()
    {
        System.out.println("SOCKET CLOSED FORCEFULLY");
        JOptionPane.showMessageDialog(
                textEditor, "Disconnected from server", "Error", JOptionPane.ERROR_MESSAGE
        );
        textEditor.disconnect();
    }

}
