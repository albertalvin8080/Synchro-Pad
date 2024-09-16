//package org.albert.design_patterns.observer.multicast;
//
//import com.formdev.flatlaf.FlatLightLaf;
//import org.albert.design_patterns.command.invoker.MenuBarCommandInvoker;
//import org.albert.design_patterns.memento_v2.MementoDocumentFilter;
//import org.albert.design_patterns.memento_v2.TextAreaCaretaker;
//import org.albert.design_patterns.memento_v2.TextAreaOriginator;
//import org.albert.design_patterns.observer.DataSharerDocumentFilter;
//import org.albert.design_patterns.observer.tcp.DataSharerFacadeTcp;
//
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.text.AbstractDocument;
//import javax.swing.text.Document;
//import java.awt.*;
//import java.awt.event.KeyEvent;
//
//public class TextEditor extends JFrame
//{
//    private final JTextArea textArea;
//    private final JScrollPane scrollPane;
//
//    private final JSpinner spinner;
//    private final JLabel spinnerLabel;
//    private final JCheckBox wordWrapCheckBox;
//
//    private final JMenuBar menuBar;
//    private final JMenu fileMenu;
//    private final JMenuItem saveMenuItem;
//    private final JMenuItem openMenuItem;
//    private final JMenuItem exitMenuItem;
//    private final JMenu editMenu;
//    private final JMenuItem copyMenuItem;
//    private final JMenuItem cutMenuItem;
//    private final JMenuItem pasteMenuItem;
//    private final JMenuItem findMenuItem;
//    private final JMenuItem undoMenuItem;
//    private final JMenuItem redoMenuItem;
//    private final JMenu formatMenu;
//    private final JMenuItem fontFormatMenuItem;
//    private final JMenuItem upperCaseMenuItem;
//    private final JMenuItem lowerCaseMenuItem;
//    private final JMenu multicastMenu;
//    private final JMenuItem connectMenuItem;
//    private final JMenuItem disconnectMenuItem;
//
//    private final TitleChangeDocumentListener titleChangeDocumentListener;
//    private final MementoDocumentFilter mementoDocumentFilter;
//    private final DataSharerFacadeTcp dataSharerFacadeTcp;
//    private boolean connected;
//
//    private final TextAreaCaretaker textAreaCaretaker;
//    private final String baseTitle = "Swing Editor";
//
//    private enum TitleStates
//    {
//        NOT_MODIFIED, MODIFIED
//    }
//
//    private TitleStates titleState = TitleStates.MODIFIED;
//
//    public TextEditor()
//    {
//        try
//        {
//            UIManager.setLookAndFeel(new FlatLightLaf());
//            // UIManager.setLookAndFeel(new FlatDarkLaf());
//        }
//        catch (UnsupportedLookAndFeelException e)
//        {
//            e.printStackTrace();
//        }
//
//        // ------- TEXTAREA -------
//        textArea = new JTextArea();
//        textArea.setTabSize(2);
//        textArea.setLineWrap(true);
//        textArea.setWrapStyleWord(true);
//        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
//
//        scrollPane = new JScrollPane(textArea);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//
//        // ------- SPINNER -------
//        spinnerLabel = new JLabel("FS");
//        spinner = new JSpinner();
//        spinner.setValue(20);
//        spinner.addChangeListener(e -> {
//            final Font font = textArea.getFont();
//            final int value = (int) spinner.getValue();
//            textArea.setFont(new Font(font.getFontName(), font.getStyle(), value));
//        });
//
//        // ------- WORD WRAP BUTTON -------
//        wordWrapCheckBox = new JCheckBox("Word Wrap");
//        // Decides if it should be initially selected or not.
//        wordWrapCheckBox.setSelected(textArea.getLineWrap() && textArea.getWrapStyleWord());
//        wordWrapCheckBox.addActionListener(e -> {
//            textArea.setLineWrap(wordWrapCheckBox.isSelected());
//            textArea.setWrapStyleWord(wordWrapCheckBox.isSelected());
//        });
//
//        // ------- MENU BAR -------
//        menuBar = new JMenuBar();
//
//        fileMenu = new JMenu("File");
//        saveMenuItem = new JMenuItem("Save");
//        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
//        openMenuItem = new JMenuItem("Open");
//        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
//        exitMenuItem = new JMenuItem("Exit");
//
//        final MenuBarCommandInvoker menuBarCommandInvoker = new MenuBarCommandInvoker(this, textArea);
//        // File Menu
//        saveMenuItem.addActionListener(e -> {
//            menuBarCommandInvoker.execute("save");
////            System.out.println("Connected: " + connected);
//            // Prevents changing the title if there's a multicast connection.
//            if(connected) return;
//
//            if (this.getTitle().contains("*"))
//            {
//                this.setTitle(this.getTitle().substring(1));
//            }
//            titleState = TitleStates.NOT_MODIFIED;
//        });
//
//        dataSharerFacadeTcp = DataSharerFacadeTcp.getInstance(textArea);
//        textAreaCaretaker = new TextAreaCaretaker(new TextAreaOriginator(textArea, dataSharerFacadeTcp));
//
//        // Problem: the DocumentListener executes AFTER the textArea has been updated.
////        textArea.getDocument().addDocumentListener(new MementoDocumentListener(textAreaCaretaker));
//        final Document document = textArea.getDocument();
//        final AbstractDocument abstractDocument = (AbstractDocument) document;
//        // Now it's NOT possible to perform undo/redo while connected.
////        mementoDocumentFilter = new MementoDocumentFilter(textAreaCaretaker, dataSharerFacade);
//        mementoDocumentFilter = new MementoDocumentFilter(textAreaCaretaker);
//        abstractDocument.setDocumentFilter(mementoDocumentFilter);
//        titleChangeDocumentListener = new TitleChangeDocumentListener();
//        document.addDocumentListener(titleChangeDocumentListener);
//
//        openMenuItem.addActionListener(e -> {
//            if(connected)
//            {
//                disconnect(document, abstractDocument);
//            }
//
//            menuBarCommandInvoker.execute("open");
//            if (this.getTitle().contains("*"))
//            {
//                this.setTitle(this.getTitle().substring(1));
//            }
//            titleState = TitleStates.NOT_MODIFIED;
//            // Prevents errors due to undo/redo of previous versions of the document before opening a new one.
//            mementoDocumentFilter.getTextAreaCaretaker().clearAll();
//        });
//        exitMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("exit"));
//
//        fileMenu.add(saveMenuItem);
//        fileMenu.add(openMenuItem);
//        // Add a horizontal separator
//        fileMenu.add(new JSeparator());
//        fileMenu.add(exitMenuItem);
//
//        editMenu = new JMenu("Edit");
//        copyMenuItem = new JMenuItem("Copy");
//        // Causes key conflict with existing intellij classes for these keys. Still usable.
//        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
//        cutMenuItem = new JMenuItem("Cut");
//        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
//        pasteMenuItem = new JMenuItem("Paste");
//        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
//
//        // Edit Menu
//        copyMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("copy"));
//        cutMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("cut"));
//        pasteMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("paste"));
//
//        editMenu.add(copyMenuItem);
//        editMenu.add(cutMenuItem);
//        editMenu.add(pasteMenuItem);
//
//        editMenu.add(new JSeparator());
//        undoMenuItem = new JMenuItem("Undo");
//        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
//        undoMenuItem.addActionListener(e -> {
//            if (!connected)
//                textAreaCaretaker.undo();
//        });
//
//        redoMenuItem = new JMenuItem("Redo");
//        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
//        redoMenuItem.addActionListener(e -> {
//            if (!connected)
//                textAreaCaretaker.redo();
//        });
//
//        editMenu.add(undoMenuItem);
//        editMenu.add(redoMenuItem);
//
//        editMenu.add(new JSeparator());
//        // Find Menu Item (PatternFinder)
//        findMenuItem = new JMenuItem("Find");
//        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
//        findMenuItem.addActionListener(e -> {
//            new FindMenuItemDialog(this, textArea);
//        });
//        editMenu.add(findMenuItem);
//
//        // Format Menu
//        formatMenu = new JMenu("Format");
//
//        fontFormatMenuItem = new JMenuItem("Font");
//        fontFormatMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK
//        ));
//        fontFormatMenuItem.addActionListener(e -> {
//            new FontFormatDialog(this, textArea);
//        });
//        formatMenu.add(fontFormatMenuItem);
//
//        upperCaseMenuItem = new JMenuItem("UpperCase");
//        upperCaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));
//        upperCaseMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("uppercase"));
//        formatMenu.add(upperCaseMenuItem);
//
//        lowerCaseMenuItem = new JMenuItem("LowerCase");
//        lowerCaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
//        lowerCaseMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("lowercase"));
//        formatMenu.add(lowerCaseMenuItem);
//
//        // Multicast Menu
//        multicastMenu = new JMenu("Multicast");
//        connectMenuItem = new JMenuItem("Connect");
//        connectMenuItem.addActionListener(e -> {
//            document.removeDocumentListener(titleChangeDocumentListener);
//            abstractDocument.setDocumentFilter(new DataSharerDocumentFilter(dataSharerFacadeTcp));
//
//            dataSharerFacadeTcp.openConnection();
//            connected = true;
//            this.setTitle(dataSharerFacadeTcp.getUuid().toString());
//        });
//        multicastMenu.add(connectMenuItem);
//
//        disconnectMenuItem = new JMenuItem("Disconnect");
//        disconnectMenuItem.addActionListener(e -> {
//            disconnect(document, abstractDocument);
//        });
//        multicastMenu.add(disconnectMenuItem);
//
//        menuBar.add(fileMenu);
//        menuBar.add(editMenu);
//        menuBar.add(formatMenu);
//        menuBar.add(multicastMenu);
//        this.setJMenuBar(menuBar);
//        // !------- MENU BAR -------
//
//        // ------- FRAME LAYOUT -------
//        this.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//
//        final JPanel panel = new JPanel();
////        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        panel.add(spinnerLabel);
//        panel.add(spinner);
//        panel.add(wordWrapCheckBox);
//
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.weightx = 1.0;
//        gbc.weighty = 0;
//        gbc.gridwidth = GridBagConstraints.REMAINDER;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        this.add(panel, gbc);
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
//        // ------- STYLE -------
//        textArea.setBackground(new Color(245, 245, 245));  // Cor de fundo mais suave
//        textArea.setForeground(Color.DARK_GRAY);  // Cor do texto
////        menuBar.setBackground(new Color(230, 230, 230));  // Cor do menu
//        menuBar.setForeground(Color.BLACK);
//
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.setBackground(Color.LIGHT_GRAY);
////        this.setLocationRelativeTo(null);
//        this.setSize(700, 700);
//        this.setTitle(baseTitle);
//        this.setVisible(true);
//    }
//
//    private void disconnect(Document document, AbstractDocument abstractDocument)
//    {
//        dataSharerFacadeTcp.closeConnection();
//        connected = false;
//
//        document.addDocumentListener(titleChangeDocumentListener);
//        abstractDocument.setDocumentFilter(mementoDocumentFilter);
//
//        this.setTitle(baseTitle);
//    }
//
//    public void changeTitle(String fileName)
//    {
//        this.setTitle(fileName + " - " + baseTitle);
//    }
//
//    public void updateTitle()
//    {
//        final String temp = this.getTitle();
//        // Find the index of "- " (which separates the filename from the editor name)
//        final int index = temp.indexOf("- ");
//
//        if (index != -1)
//        {
//            StringBuilder newTitle = new StringBuilder(temp);
//            newTitle.insert(0, '*');
//            this.setTitle(newTitle.toString());
//        }
//    }
//
//    private class TitleChangeDocumentListener implements DocumentListener
//    {
//        @Override
//        public void insertUpdate(DocumentEvent e)
//        {
//            updateTitleIfNeeded();
//        }
//
//        @Override
//        public void removeUpdate(DocumentEvent e)
//        {
//            updateTitleIfNeeded();
//        }
//
//        @Override
//        public void changedUpdate(DocumentEvent e)
//        {
//            updateTitleIfNeeded();
//        }
//
//        private void updateTitleIfNeeded()
//        {
//            if (titleState == TitleStates.NOT_MODIFIED)
//            {
//                updateTitle();
//                titleState = TitleStates.MODIFIED;
//            }
//        }
//    }
//
//    public boolean isConnected()
//    {
//        return connected;
//    }
//}