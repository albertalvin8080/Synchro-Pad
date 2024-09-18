package org.albert.component;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class FontFormatDialog extends JDialog
{
    private final JComboBox<String> fontComboBox;
    private final JButton colorButton;
    private final JButton confirmButton;

    // Default values
    private Color newColor = Color.BLACK;
    private String newFont = "Arial";

    public FontFormatDialog(JFrame frame, JTextArea textArea)
    {
        super(frame, "Font Format Dialog", JDialog.ModalityType.APPLICATION_MODAL);

        // ------- COMBOBOX -------
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setSelectedItem("Arial");
        fontComboBox.addActionListener(e -> {
            newFont = (String) fontComboBox.getSelectedItem();
        });

        // ------- COLOR BUTTON -------
        colorButton = new JButton();
        colorButton.setBackground(Color.WHITE);
        int colorButtonWidth = 25;
        int colorButtonHeight = 25;
        colorButton.setPreferredSize(new Dimension(colorButtonWidth, colorButtonHeight));
        final URL colorsImg = ClassLoader.getSystemResource("colors.png");
        final Image scaledInstance = new ImageIcon(colorsImg.getPath()).getImage().getScaledInstance(colorButtonWidth, colorButtonHeight, Image.SCALE_SMOOTH);
        colorButton.setIcon(new ImageIcon(scaledInstance));
        colorButton.addActionListener(e -> {
            newColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
        });

        confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            textArea.setForeground(newColor);

            final Font font = textArea.getFont();
            textArea.setFont(new Font(newFont, font.getStyle(), font.getSize()));

            this.dispose();
        });

        final JPanel jPanel = new JPanel();
        jPanel.add(fontComboBox);
        jPanel.add(colorButton);
        this.add(jPanel);
        this.add(confirmButton);

        this.setLayout(new GridLayout(2, 1));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(frame);
        this.pack();
        this.setVisible(true);
    }
}
