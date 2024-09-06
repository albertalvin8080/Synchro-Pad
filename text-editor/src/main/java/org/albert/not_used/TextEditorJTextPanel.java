//package org.albert.component;
//
//import org.albert.not_used.MenuBarEventHandlerUtil;
//
//import javax.swing.*;
//import javax.swing.text.*;
//import java.awt.*;
//
//public class TextEditorJTextPanel extends JFrame
//{
//    private final JTextPane textPane;
//    private final JScrollPane scrollPane;
//
//    private final JSpinner spinner;
//    private final JButton colorButton;
//    private final JButton boldButton;
//    private final JLabel spinnerLabel;
//    private final JComboBox<String> fontComboBox;
////    private final JCheckBox wordWrapCheckBox;
//
//    private final JMenuBar menuBar;
//    private final JMenu fileMenu;
//    private final JMenuItem saveMenuItem;
//    private final JMenuItem openMenuItem;
//    private final JMenuItem exitMenuItem;
//
//    public TextEditorJTextPanel()
//    {
//        // ------- TEXT PANE -------
//        textPane = new JTextPane();
////        textPane.setLineWrap(true);
////        textPane.setWrapStyleWord(true);
//        textPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
//
//        scrollPane = new JScrollPane(textPane);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
////        textPane.setEditorKit(new WrapEditorKit());
//
//        // ------- SPINNER -------
//        spinnerLabel = new JLabel("Font:");
//        spinner = new JSpinner();
//        spinner.setValue(20);
//        spinner.addChangeListener(e -> {
//            final Font font = textPane.getFont();
//            final int value = (int) spinner.getValue();
//            textPane.setFont(new Font(
//                    font.getFontName(),
//                    font.getStyle(),
//                    value
//            ));
//        });
//
//        // ------- COLOR BUTTON -------
//        colorButton = new JButton("Color");
//        colorButton.addActionListener(e -> {
//            final Color color = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
//            textPane.setForeground(color);
//        });
//
//        // ------- COMBOBOX -------
//        String[] fonts = GraphicsEnvironment
//                .getLocalGraphicsEnvironment()
//                .getAvailableFontFamilyNames();
//        fontComboBox = new JComboBox<>(fonts);
//        fontComboBox.setSelectedItem("Arial");
//        fontComboBox.addActionListener(e -> {
//            Font font = textPane.getFont();
//            textPane.setFont(new Font(
//                    (String) fontComboBox.getSelectedItem(),
//                    font.getStyle(),
//                    font.getSize()
//            ));
//        });
//
//        // ------- BOLD BUTTON -------
//        boldButton = new JButton("bold");
//        boldButton.addActionListener(e -> {
//            StyledDocument doc = textPane.getStyledDocument();
//            int start = textPane.getSelectionStart();
//            int end = textPane.getSelectionEnd();
//
////            System.out.println(start);
////            System.out.println(end);
//
//            if(start == end)
//            {
//                SimpleAttributeSet resetStyle = new SimpleAttributeSet();
//                doc.setCharacterAttributes(end, 1, resetStyle, true);
//                return;
//            }
//
//            // Ensure there is text selected
//            Element element = doc.getCharacterElement(start);
//            AttributeSet as = element.getAttributes();
//
//            // Check if the selected text is already bold
//            boolean isBold = StyleConstants.isBold(as);
//
//            // Toggle bold style
//            SimpleAttributeSet sas = new SimpleAttributeSet();
//            StyleConstants.setBold(sas, !isBold);
//            doc.setCharacterAttributes(start, end - start, sas, false);
//        });
//
//        // ------- WORD WRAP BUTTON -------
////        wordWrapCheckBox = new JCheckBox("Word Wrap");
//        // Decides if it should be initially selected or not.
////        wordWrapCheckBox.setSelected(scrollPane.getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
////        wordWrapCheckBox.addActionListener(e -> {
////            textPane.setLineWrap(wordWrapCheckBox.isSelected());
////            textPane.setWrapStyleWord(wordWrapCheckBox.isSelected());
////            scrollPane.setHorizontalScrollBarPolicy(wordWrapCheckBox.isSelected() ?
////                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER :
////                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
////            scrollPane.repaint();
////        });
//
//        // ------- MENU BAR -------
//        menuBar = new JMenuBar();
//        fileMenu = new JMenu("File");
//        saveMenuItem = new JMenuItem("Save");
//        openMenuItem = new JMenuItem("Open");
//        exitMenuItem = new JMenuItem("Exit");
//
//        final MenuBarEventHandlerUtil menuBarEventHandlerUtil = new MenuBarEventHandlerUtil(this, textPane);
//        saveMenuItem.addActionListener(menuBarEventHandlerUtil::Save);
//        openMenuItem.addActionListener(menuBarEventHandlerUtil::Open);
//        exitMenuItem.addActionListener(menuBarEventHandlerUtil::Exit);
//
//        fileMenu.add(saveMenuItem);
//        fileMenu.add(openMenuItem);
//        fileMenu.add(exitMenuItem);
//        menuBar.add(fileMenu);
//
//        this.setJMenuBar(menuBar);
//        // !------- MENU BAR -------
//
//        // ------- FRAME LAYOUT -------
//        this.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//
//        // Add spinnerLabel
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.weightx = 0;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        this.add(spinnerLabel, gbc);
//
//        // Add spinner
//        gbc.gridx = 1;
//        gbc.gridy = 0;
//        gbc.weightx = 0;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        this.add(spinner, gbc);
//
//        // Add colorButton
//        gbc.gridx = 2;
//        gbc.gridy = 0;
//        gbc.weightx = 0;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        this.add(colorButton, gbc);
//
//        // Add wordWrapButton
//        gbc.gridx = 3;
//        gbc.gridy = 0;
//        gbc.weightx = 0;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        this.add(boldButton, gbc);
//
//        // Add wordWrapButton
//        gbc.gridx = 3;
//        gbc.gridy = 0;
//        gbc.weightx = 0;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
////        this.add(wordWrapCheckBox, gbc);
//
//        // Add fontComboBox
//        gbc.gridx = 4;
//        gbc.gridy = 0;
//        gbc.weightx = 1;
//        gbc.gridwidth = GridBagConstraints.REMAINDER;
//        gbc.fill = GridBagConstraints.NONE;
////        gbc.fill = GridBagConstraints.BOTH;
//        this.add(fontComboBox, gbc);
//
//        // Add scrollPane
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        gbc.weightx = 1.0;
//        gbc.weighty = 1.0;
//        gbc.gridwidth = GridBagConstraints.REMAINDER;
//        gbc.fill = GridBagConstraints.BOTH;
//        this.add(scrollPane, gbc);
//        // !------- FRAME LAYOUT -------
//
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.setSize(600, 600);
//        this.setBackground(Color.LIGHT_GRAY);
//        this.setLocationRelativeTo(null);
//        this.setVisible(true);
//    }
//
//
//}
