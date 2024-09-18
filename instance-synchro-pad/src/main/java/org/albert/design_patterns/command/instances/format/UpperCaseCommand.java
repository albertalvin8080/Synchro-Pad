package org.albert.design_patterns.command.instances.format;

import org.albert.design_patterns.command.contract.Command;

import javax.swing.*;

public class UpperCaseCommand implements Command
{
    private final JTextArea textArea;

    public UpperCaseCommand(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    @Override
    public void execute()
    {
        int start = textArea.getSelectionStart();
        int end = textArea.getSelectionEnd();

        // Ensures that there is a selection
        if (start != end)
        {
            String selectedText = textArea.getSelectedText();

            String upperText = selectedText.toUpperCase();
            StringBuilder text = new StringBuilder(textArea.getText());
            textArea.setText(text.toString());
            text.replace(start, end, upperText);
            textArea.setText(text.toString());
            textArea.setSelectionStart(start);
            textArea.setSelectionEnd(start + upperText.length());
        }
    }
}
