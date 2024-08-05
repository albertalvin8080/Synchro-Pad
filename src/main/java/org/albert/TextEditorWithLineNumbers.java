package org.albert;

import javax.swing.*;
import java.awt.*;

public class TextEditorWithLineNumbers extends JFrame
{
    private final JTextArea textArea;
    private final JTextArea lines;
    private final JScrollPane scrollPane;

    private final JSpinner spinner;
    private final JButton colorButton;
    private final JLabel spinnerLabel;

    private final JComboBox<String> fontComboBox;

    public TextEditorWithLineNumbers()
    {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));

        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(450, 450));

        lines = new JTextArea("1");
        textArea.getDocument().addDocumentListener(
                new LineNumberDocumentListener(lines, textArea, scrollPane)
        );

        spinner = new JSpinner();
        spinner.setValue(20);
        spinner.addChangeListener(e -> {
            final Font font = textArea.getFont();
            final int value = (int) spinner.getValue();
            textArea.setFont(new Font(
                    font.getFontName(),
                    font.getStyle(),
                    value
            ));
            lines.setFont(new Font(
                    font.getFontName(),
                    font.getStyle(),
                    value
            ));
            // Makes the width of the line header grow alongside the text.
            // value - value%
//            lines.setPreferredSize(new Dimension(value - (value*20/100), 0));
        });

        spinnerLabel = new JLabel("Font:");

        colorButton = new JButton("Color");
        colorButton.addActionListener(e -> {
            final Color color = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
            textArea.setForeground(color);
        });

        String[] fonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setSelectedItem("Arial");
        fontComboBox.addActionListener(e -> {
            Font font = textArea.getFont();
            textArea.setFont(new Font(
                    (String) fontComboBox.getSelectedItem(),
                    font.getStyle(),
                    font.getSize()
            ));
            lines.setFont(new Font(
                    (String) fontComboBox.getSelectedItem(),
                    font.getStyle(),
                    font.getSize()
            ));
        });

        this.add(spinnerLabel);
        this.add(spinner);
        this.add(colorButton);
        this.add(fontComboBox);
        this.add(scrollPane);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());
        this.setSize(500, 500);
        this.setBackground(Color.LIGHT_GRAY);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
