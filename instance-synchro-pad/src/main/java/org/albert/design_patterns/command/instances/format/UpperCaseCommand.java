package org.albert.design_patterns.command.instances.format;

import org.albert.design_patterns.command.contract.Command;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
            Document doc = textArea.getDocument();
            String selectedText = textArea.getSelectedText();

            String upperText = selectedText.toUpperCase();
            if(selectedText.equals(upperText)) return;

            if (doc instanceof javax.swing.text.AbstractDocument)
            {
                try
                {
                    ((javax.swing.text.AbstractDocument) doc).replace(start, end - start, upperText, null);
                }
                catch (BadLocationException e)
                {
                    throw new RuntimeException(e);
                }
            }

            textArea.setSelectionStart(start);
            textArea.setSelectionEnd(start + upperText.length());
        }
    }
}
