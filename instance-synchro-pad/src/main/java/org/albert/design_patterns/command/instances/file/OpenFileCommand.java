package org.albert.design_patterns.command.instances.file;


import org.albert.component.SynchroPad;
import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.invoker.FilePathHolder;

import javax.swing.*;
import java.io.*;

public class OpenFileCommand implements Command
{
    private final SynchroPad frame;
    private final JTextArea textArea;
    private final FilePathHolder filePathHolder;

    public OpenFileCommand(SynchroPad frame, JTextArea textArea, FilePathHolder filePathHolder)
    {
        this.frame = frame;
        this.textArea = textArea;
        this.filePathHolder = filePathHolder;
    }

    @Override
    public void execute()
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        final int option = fileChooser.showOpenDialog(frame);
        final File file;

        if (option == JFileChooser.APPROVE_OPTION)
        {
            file = new File(fileChooser.getSelectedFile().getAbsolutePath());
            filePathHolder.setCurrentFilePath(file.getAbsolutePath());
            frame.changeTitle(file.getName());

            try (FileReader fr = new FileReader(file);
                 BufferedReader br = new BufferedReader(fr))
            {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    content.append(line).append("\n"); // Avoiding Windows's "\r\n"
                }
                textArea.setText(content.toString());
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
