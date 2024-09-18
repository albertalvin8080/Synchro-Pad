package org.albert.design_patterns.command.invoker;

import org.albert.component.SynchroPad;
import org.albert.design_patterns.command.contract.Command;
import org.albert.design_patterns.command.instances.edit.CopyCommand;
import org.albert.design_patterns.command.instances.edit.CutCommand;
import org.albert.design_patterns.command.instances.edit.PasteCommand;
import org.albert.design_patterns.command.instances.file.ExitFileCommand;
import org.albert.design_patterns.command.instances.file.OpenFileCommand;
import org.albert.design_patterns.command.instances.file.SaveFileCommand;
import org.albert.design_patterns.command.instances.format.LowerCaseCommand;
import org.albert.design_patterns.command.instances.format.UpperCaseCommand;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class MenuBarCommandInvoker
{
    private final Map<String, Command> commandMap;

    public MenuBarCommandInvoker(SynchroPad frame, JTextArea textArea)
    {
        this.commandMap = new HashMap<>();
        final FilePathHolder filePathHolder = new FilePathHolder();
        commandMap.put("copy", new CopyCommand(textArea));
        commandMap.put("cut", new CutCommand(textArea));
        commandMap.put("paste", new PasteCommand(textArea));
        commandMap.put("save", new SaveFileCommand(frame, textArea, filePathHolder));
        commandMap.put("open", new OpenFileCommand(frame, textArea, filePathHolder));
        commandMap.put("exit", new ExitFileCommand(frame));
        commandMap.put("lowercase", new LowerCaseCommand(textArea));
        commandMap.put("uppercase", new UpperCaseCommand(textArea));
    }

    public void execute(String key)
    {
        final Command command = commandMap.get(key);
        if (command != null) command.execute();
    }
}
