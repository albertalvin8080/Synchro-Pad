package org.albert.design_patterns.command.instances.file;


import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.invoker.FilePathHolder;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class ExitFileCommand implements Command
{
    private final JFrame frame;

    public ExitFileCommand(JFrame frame)
    {
        this.frame = frame;
    }

    @Override
    public void execute()
    {
        final int op = JOptionPane.showConfirmDialog(frame, "Are you sure?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION)
        {
            System.exit(0);
        }
    }
}
