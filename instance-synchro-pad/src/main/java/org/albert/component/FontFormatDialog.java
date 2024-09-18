package org.albert.component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
        colorButton.addActionListener(e -> {
            newColor = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
        });
        try (InputStream imgStream = ClassLoader.getSystemResourceAsStream("img/colors.png"))
        {
            if (imgStream != null)
            {
                // Read the image from the input stream
                final BufferedImage img = ImageIO.read(imgStream);
                final Image scaledInstance = img.getScaledInstance(colorButtonWidth, colorButtonHeight, Image.SCALE_SMOOTH);
                colorButton.setIcon(new ImageIcon(scaledInstance));
            }
            else System.err.println("Image not found: img/colors.png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

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
