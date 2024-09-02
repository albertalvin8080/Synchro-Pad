package org.albert.component;

import org.albert.design_patterns.command.invoker.MenuBarCommandInvoker;
import org.albert.design_patterns.memento_v2.MementoDocumentFilter;
import org.albert.design_patterns.memento_v2.TextAreaCaretaker;
import org.albert.design_patterns.memento_v2.TextAreaOriginator;
import org.albert.design_patterns.observer.DataSharerFacade;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.KeyEvent;

public class TextEditor extends JFrame
{
    private final JTextArea textArea;
    private final JScrollPane scrollPane;

    private final JSpinner spinner;
    private final JLabel spinnerLabel;
    private final JCheckBox wordWrapCheckBox;

    private final JMenuBar menuBar;
    private final JMenu fileMenu;
    private final JMenuItem saveMenuItem;
    private final JMenuItem openMenuItem;
    private final JMenuItem exitMenuItem;
    private final JMenu editMenu;
    private final JMenuItem copyMenuItem;
    private final JMenuItem cutMenuItem;
    private final JMenuItem pasteMenuItem;
    private final JMenuItem findMenuItem;
    private final JMenuItem undoMenuItem;
    private final JMenuItem redoMenuItem;
    private final JMenu formatMenu;
    private final JMenuItem fontFormatMenuItem;
    private final JMenuItem upperCaseMenuItem;
    private final JMenuItem lowerCaseMenuItem;
    private final JMenu multicastMenu;
    private final JMenuItem connectMenuItem;
    private final JMenuItem disconnectMenuItem;

    private final MementoDocumentFilter mementoDocumentFilter;
    private final DataSharerFacade dataSharerFacade;

    private final TextAreaCaretaker textAreaCaretaker;
    private final String baseTitle = "Swing Editor";

    private enum TitleStates
    {
        NOT_MODIFIED, MODIFIED
    }

    private TitleStates titleState = TitleStates.MODIFIED;

    public TextEditor()
    {
        // ------- TEXTAREA -------
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));

        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // ------- SPINNER -------
        spinnerLabel = new JLabel("FS");
        spinner = new JSpinner();
        spinner.setValue(20);
        spinner.addChangeListener(e -> {
            final Font font = textArea.getFont();
            final int value = (int) spinner.getValue();
            textArea.setFont(new Font(font.getFontName(), font.getStyle(), value));
        });

        // ------- WORD WRAP BUTTON -------
        wordWrapCheckBox = new JCheckBox("Word Wrap");
        // Decides if it should be initially selected or not.
        wordWrapCheckBox.setSelected(textArea.getLineWrap() && textArea.getWrapStyleWord());
        wordWrapCheckBox.addActionListener(e -> {
            textArea.setLineWrap(wordWrapCheckBox.isSelected());
            textArea.setWrapStyleWord(wordWrapCheckBox.isSelected());
        });

        // ------- MENU BAR -------
        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        openMenuItem = new JMenuItem("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        exitMenuItem = new JMenuItem("Exit");

        MenuBarCommandInvoker menuBarCommandInvoker = new MenuBarCommandInvoker(this, textArea);
        // File Menu
        saveMenuItem.addActionListener(e -> {
            menuBarCommandInvoker.execute("save");
            if (this.getTitle().contains("*"))
            {
                this.setTitle(this.getTitle().substring(1));
            }
            titleState = TitleStates.NOT_MODIFIED;
        });

        dataSharerFacade = DataSharerFacade.getInstance(textArea);
        textAreaCaretaker = new TextAreaCaretaker(new TextAreaOriginator(textArea, dataSharerFacade));

        // Problem: the DocumentListener executes AFTER the textArea has been updated.
//        textArea.getDocument().addDocumentListener(new MementoDocumentListener(textAreaCaretaker));
        AbstractDocument doc = (AbstractDocument) textArea.getDocument();
        mementoDocumentFilter = new MementoDocumentFilter(textAreaCaretaker, dataSharerFacade);
        doc.setDocumentFilter(mementoDocumentFilter);
        textArea.getDocument().addDocumentListener(new TitleChangeDocumentListener());

        openMenuItem.addActionListener(e -> {
            menuBarCommandInvoker.execute("open");
            if (this.getTitle().contains("*"))
            {
                this.setTitle(this.getTitle().substring(1));
            }
            titleState = TitleStates.NOT_MODIFIED;
            // Prevents errors due to undo/redo of previous versions of the document before opening a new one.
            mementoDocumentFilter.getTextAreaCaretaker().clearAll();
        });
        exitMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("exit"));

        fileMenu.add(saveMenuItem);
        fileMenu.add(openMenuItem);
        // Add a horizontal separator
        fileMenu.add(new JSeparator());
        fileMenu.add(exitMenuItem);

        editMenu = new JMenu("Edit");
        copyMenuItem = new JMenuItem("Copy");
        // Causes key conflict with existing intellij classes for these keys. Still usable.
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));

        // Edit Menu
        copyMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("copy"));
        cutMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("cut"));
        pasteMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("paste"));

        editMenu.add(copyMenuItem);
        editMenu.add(cutMenuItem);
        editMenu.add(pasteMenuItem);

        editMenu.add(new JSeparator());
        undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        undoMenuItem.addActionListener(e -> textAreaCaretaker.undo());

        redoMenuItem = new JMenuItem("Redo");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        redoMenuItem.addActionListener(e -> textAreaCaretaker.redo());

        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);

        editMenu.add(new JSeparator());
        // Find Menu Item (PatternFinder)
        findMenuItem = new JMenuItem("Find");
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        findMenuItem.addActionListener(e -> {
            new FindMenuItemDialog(this, textArea);
        });
        editMenu.add(findMenuItem);

        // Format Menu
        formatMenu = new JMenu("Format");

        fontFormatMenuItem = new JMenuItem("Font");
        fontFormatMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK
        ));
        fontFormatMenuItem.addActionListener(e -> {
            new FontFormatDialog(this, textArea);
        });
        formatMenu.add(fontFormatMenuItem);

        upperCaseMenuItem = new JMenuItem("UpperCase");
        upperCaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));
        upperCaseMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("uppercase"));
        formatMenu.add(upperCaseMenuItem);

        lowerCaseMenuItem = new JMenuItem("LowerCase");
        lowerCaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        lowerCaseMenuItem.addActionListener(e -> menuBarCommandInvoker.execute("lowercase"));
        formatMenu.add(lowerCaseMenuItem);

        // Multicast Menu
        multicastMenu = new JMenu("Multicast");
        connectMenuItem = new JMenuItem("Connect");
        connectMenuItem.addActionListener(e -> dataSharerFacade.openConnection());
        multicastMenu.add(connectMenuItem);

        disconnectMenuItem = new JMenuItem("Disconnect");
        disconnectMenuItem.addActionListener(e -> dataSharerFacade.closeConnection());
        multicastMenu.add(disconnectMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(multicastMenu);
        this.setJMenuBar(menuBar);
        // !------- MENU BAR -------

        // ------- FRAME LAYOUT -------
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        final JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(spinnerLabel);
        panel.add(spinner);
        panel.add(wordWrapCheckBox);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(panel, gbc);

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
        this.setBackground(Color.LIGHT_GRAY);
//        this.setLocationRelativeTo(null);
        this.setSize(700, 700);
        this.setTitle(baseTitle);
        this.setVisible(true);
    }

    public void changeTitle(String fileName)
    {
        this.setTitle(fileName + " - " + baseTitle);
    }

    public void updateTitle()
    {
        final String temp = this.getTitle();
        // Find the index of "- " (which separates the filename from the editor name)
        final int index = temp.indexOf("- ");

        if (index != -1)
        {
            StringBuilder newTitle = new StringBuilder(temp);
            newTitle.insert(0, '*');
            this.setTitle(newTitle.toString());
        }
    }

    private class TitleChangeDocumentListener implements DocumentListener
    {
        @Override
        public void insertUpdate(DocumentEvent e)
        {
            updateTitleIfNeeded();
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
            updateTitleIfNeeded();
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
            updateTitleIfNeeded();
        }

        private void updateTitleIfNeeded()
        {
            if (titleState == TitleStates.NOT_MODIFIED)
            {
                updateTitle();
                titleState = TitleStates.MODIFIED;
            }
        }
    }
}