package org.albert;

import org.albert.component.TextEditor;

import javax.swing.*;

public class App
{
    public static void main( String[] args )
    {
        SwingUtilities.invokeLater(TextEditor::new);
    }
}

