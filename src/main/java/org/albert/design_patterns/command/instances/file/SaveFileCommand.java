package org.albert.design_patterns.command.instances.file;


import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.invoker.FilePathHolder;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveFileCommand implements Command
{
    private final JFrame frame;
    private final JTextArea textArea;
    private final FilePathHolder filePathHolder;

    public SaveFileCommand(JFrame frame, JTextArea textArea, FilePathHolder filePathHolder)
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
        }

        if (option == JFileChooser.APPROVE_OPTION)
        {
            try (FileWriter fr = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fr))
            {
                bw.write(textArea.getText());
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
