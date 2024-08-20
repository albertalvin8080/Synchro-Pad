package org.albert.design_patterns.command.invoker;

import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.instances.file.ExitFileCommand;
import org.albert.design_patterns.command.instances.file.OpenFileCommand;
import org.albert.design_patterns.command.instances.file.SaveFileCommand;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class FileCommandInvoker
{
    private final Map<String, Command> commandMap;

    public FileCommandInvoker(JFrame frame, JTextArea textArea)
    {
        this.commandMap = new HashMap<>();
        final FilePathHolder filePathHolder = new FilePathHolder();
        commandMap.put("save", new SaveFileCommand(frame, textArea, filePathHolder));
        commandMap.put("open", new OpenFileCommand(frame, textArea, filePathHolder));
        commandMap.put("exit", new ExitFileCommand(frame));
    }

    public void execute(String key)
    {
        final Command command = commandMap.get(key);
        if (command != null) command.execute();
    }
}
