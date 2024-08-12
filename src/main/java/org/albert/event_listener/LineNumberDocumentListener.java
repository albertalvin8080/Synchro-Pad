package org.albert.event_listener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.*;

public class LineNumberDocumentListener implements DocumentListener
{
    private final JTextArea textArea;
    private final JTextArea lines;
    private final JScrollPane scrollPane;

    public LineNumberDocumentListener(JTextArea lines, JTextArea textArea, JScrollPane scrollPane)
    {
//        lines = new JTextArea("1");
        this.lines = lines;
        this.lines.setEditable(false);
        this.lines.setBackground(Color.LIGHT_GRAY);

        this.textArea = textArea;

        // Make the initial font size to be the same as of textArea
        final Font font = textArea.getFont();
        this.lines.setFont(new Font(
                font.getFontName(),
                font.getStyle(),
                font.getSize()
        ));
//        this.lines.setPreferredSize(new Dimension(font.getSize() - 5, 0));

        this.scrollPane = scrollPane;
        this.scrollPane.getViewport().add(textArea);
        this.scrollPane.setRowHeaderView(lines);

        // It still uses the caret anyway.
//        this.textArea.addPropertyChangeListener("lineWrap", evt -> lines.setText(getText()));
    }

    public String getText()
    {
        int caretPosition = textArea.getDocument().getLength();
        Element root = textArea.getDocument().getDefaultRootElement();
        StringBuilder text = new StringBuilder("1").append(System.lineSeparator());
        for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++)
        {
            text.append(i).append(System.lineSeparator());
        }
        return text.toString();
    }

    @Override
    public void changedUpdate(DocumentEvent de)
    {
        lines.setText(getText());
    }

    @Override
    public void insertUpdate(DocumentEvent de)
    {
        lines.setText(getText());
    }

    @Override
    public void removeUpdate(DocumentEvent de)
    {
        lines.setText(getText());
    }
}
