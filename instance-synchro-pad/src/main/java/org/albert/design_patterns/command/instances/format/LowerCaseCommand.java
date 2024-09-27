package org.albert.design_patterns.command.instances.format;

import org.albert.design_patterns.command.contract.Command;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class LowerCaseCommand implements Command
{
    private final JTextArea textArea;

    public LowerCaseCommand(JTextArea textArea)
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

            // Convert selected text to lowercase
            String lowerText = selectedText.toLowerCase();
            if(selectedText.equals(lowerText)) return;

            // Replace only the necessary portion of the document
            if (doc instanceof javax.swing.text.AbstractDocument)
            {
                try
                {
                    ((javax.swing.text.AbstractDocument) doc).replace(start, end - start, lowerText, null);
                }
                catch (BadLocationException e)
                {
                    throw new RuntimeException(e);
                }
            }

            // Restore selection
            textArea.setSelectionStart(start);
            textArea.setSelectionEnd(start + lowerText.length());
        }
    }
}
