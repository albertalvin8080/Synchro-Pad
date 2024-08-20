package org.albert.design_patterns.command.instances.file;


import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.invoker.FilePathHolder;

import javax.swing.*;
import java.io.*;
import java.util.stream.Collectors;

public class OpenFileCommand implements Command
{
    private final JFrame frame;
    private final JTextArea textArea;
    private final FilePathHolder filePathHolder;

    public OpenFileCommand(JFrame frame, JTextArea textArea, FilePathHolder filePathHolder)
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

            try (FileReader fr = new FileReader(file);
                 BufferedReader br = new BufferedReader(fr))
            {
                final String collect = br.lines().collect(Collectors.joining(System.lineSeparator()));
                textArea.setText(collect);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
