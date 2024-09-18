package org.albert.design_patterns.command.instances.edit;

import org.albert.design_patterns.command.contract.Command;

import javax.swing.*;

public class CutCommand implements Command
{
    private final JTextArea textArea;

    public CutCommand(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    @Override
    public void execute()
    {
        textArea.cut();
    }
}