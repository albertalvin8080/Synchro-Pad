package org.albert;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.stream.Collectors;

public class TextEditor extends JFrame
{
    private final JTextArea textArea;
    private final JScrollPane scrollPane;

    private final JSpinner spinner;
    private final JButton colorButton;
    private final JLabel spinnerLabel;

    private final JComboBox<String> fontComboBox;

    private final JMenuBar menuBar;
    private final JMenu fileMenu;
    private final JMenuItem saveMenuItem;
    private final JMenuItem openMenuItem;
    private final JMenuItem exitMenuItem;

    public TextEditor()
    {
        // ------- TEXTAREA -------
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));

        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // ------- SPINNER -------
        spinnerLabel = new JLabel("Font:");
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
        });

        // ------- COLOR BUTTON -------
        colorButton = new JButton("Color");
        colorButton.addActionListener(e -> {
            final Color color = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
            textArea.setForeground(color);
        });

        // ------- COMBOBOX -------
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
        });

        // ------- MENU BAR -------
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        saveMenuItem = new JMenuItem("Save");
        openMenuItem = new JMenuItem("Open");
        exitMenuItem = new JMenuItem("Exit");

        saveMenuItem.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            final int i = fileChooser.showSaveDialog(this);
            if (i == JFileChooser.APPROVE_OPTION)
            {
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                try (FileWriter fr = new FileWriter(file);
                     BufferedWriter bw = new BufferedWriter(fr))
                {
                    bw.write(textArea.getText());
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });

        openMenuItem.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            final int i = fileChooser.showOpenDialog(this);
            if (i == JFileChooser.APPROVE_OPTION)
            {
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                try (FileReader fr = new FileReader(file);
                     BufferedReader bw = new BufferedReader(fr))
                {
                    final String collect = bw.lines().collect(Collectors.joining(System.lineSeparator()));
                    textArea.setText(collect);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });

        exitMenuItem.addActionListener(e -> {
            final int op = JOptionPane.showConfirmDialog(this, "Are you sure?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op == JOptionPane.OK_OPTION)
            {
                System.exit(0);
            }
        });

        fileMenu.add(saveMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        this.setJMenuBar(menuBar);
        // !------- MENU BAR -------

        // ------- FRAME LAYOUT -------
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add spinnerLabel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(spinnerLabel, gbc);

        // Add spinner
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(spinner, gbc);

        // Add colorButton
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(colorButton, gbc);

        // Add fontComboBox
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(fontComboBox, gbc);

        // Add scrollPane
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, gbc);
        // !------- FRAME LAYOUT -------

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 530);
        this.setBackground(Color.LIGHT_GRAY);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(TextEditor::new);
    }
}
