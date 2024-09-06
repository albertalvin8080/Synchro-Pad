package org.albert.design_patterns.command.instances.file;


import org.albert.component.TextEditor;
import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.invoker.FilePathHolder;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveFileCommand implements Command
{
    private final TextEditor frame;
    private final JTextArea textArea;
    private final FilePathHolder filePathHolder;

    public SaveFileCommand(TextEditor frame, JTextArea textArea, FilePathHolder filePathHolder)
    {
        this.frame = frame;
        this.textArea = textArea;
        this.filePathHolder= filePathHolder;
    }

    @Override
    public void execute()
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        final int option;
        final File file;

        // If there's currently a multicast connection, this prevents overriding previous opened files
        // with the content of the connection.
        if(frame.isConnected())
            filePathHolder.setCurrentFilePath(null);

        // If there's already a select file.
        if (filePathHolder.getCurrentFilePath() != null)
        {
            option = JFileChooser.APPROVE_OPTION;
            file = new File(filePathHolder.getCurrentFilePath());
        }
        // If the file being saved doesn't yet exist.
        else
        {
            option = fileChooser.showSaveDialog(frame);
            final File selectedFile = fileChooser.getSelectedFile();
            // No file selected.
            if (selectedFile == null) return;

            file = new File(selectedFile.getAbsolutePath());
            filePathHolder.setCurrentFilePath(selectedFile.getAbsolutePath());

            // Prevents changing the title if there's currently a multicast connection.
            if(!frame.isConnected())
                frame.changeTitle(file.getName());
        }

        if (option == JFileChooser.APPROVE_OPTION)
        {
            try (FileWriter fr = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fr))
            {
                String content = textArea.getText();
                // This converts '\n' to the platform's line separator
                content = content.replaceAll("\n", System.lineSeparator());
                bw.write(content);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
