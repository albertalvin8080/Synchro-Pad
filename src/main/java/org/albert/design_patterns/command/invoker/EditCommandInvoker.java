package org.albert.design_patterns.command.invoker;

import org.albert.design_patterns.command.instances.edit.CopyCommand;
import org.albert.design_patterns.command.instances.edit.CutCommand;
import org.albert.design_patterns.command.instances.edit.PasteCommand;
import org.albert.design_patterns.command.contract.Command;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class EditCommandInvoker
{
    private final Map<String, Command> commandMap;

    public EditCommandInvoker(JTextArea textArea)
    {
        this.commandMap = new HashMap<>();
        commandMap.put("copy", new CopyCommand(textArea));
        commandMap.put("cut", new CutCommand(textArea));
        commandMap.put("paste", new PasteCommand(textArea));
    }

    public void execute(String key)
    {
        final Command command = commandMap.get(key);
        if (command != null) command.execute();
    }
}
