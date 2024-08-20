package org.albert.component;

import org.albert.design_patterns.decorator.contract.PatternFinder;
import org.albert.design_patterns.decorator.decorated.PlainPatternFinder;
import org.albert.design_patterns.decorator.decorator.CaseInsensitivePatternFinderDecorator;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class FindMenuItemDialog extends JDialog
{
    private final JTextField patternTextField;
    private final JButton findButton;
    private final JButton nextButton;
    private final JButton previousButton;
    private final JCheckBox caseInsensitiveCheckBox;
    private final JCheckBox wholeWordCheckBox;
    private final JCheckBox regexCheckBox;

    private List<Integer> integerList;
    private String currentPattern;
    private int currentListIndex;

    public FindMenuItemDialog(JFrame frame, JTextArea textArea)
    {
        super(frame, "Finder Dialog", JDialog.ModalityType.APPLICATION_MODAL);

        caseInsensitiveCheckBox = new JCheckBox("Cc");
        caseInsensitiveCheckBox.setFocusable(false);
        wholeWordCheckBox = new JCheckBox("W");
        wholeWordCheckBox.setFocusable(false);
        regexCheckBox = new JCheckBox(".*");
        regexCheckBox.setFocusable(false);

        patternTextField = new JTextField();
        findButton = new JButton("Find");
        findButton.addActionListener(e -> {
            currentPattern = patternTextField.getText();

            PatternFinder patternFinder = new PlainPatternFinder();
            if (caseInsensitiveCheckBox.isSelected())
                patternFinder = new CaseInsensitivePatternFinderDecorator(patternFinder);

            integerList = patternFinder.find(textArea.getText(), currentPattern);

            if (!integerList.isEmpty())
            {
                int textMatchIndex = integerList.get(0);
                currentListIndex = 0;
                highlightMatches(textArea, textMatchIndex, currentListIndex++);
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Pattern not found!", "Find", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // ------- NEXT BUTTON -------
        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {
            if (integerList == null || integerList.isEmpty()) return;

            // Returns to the start if it's already the last index.
            if (currentListIndex >= integerList.size())
                currentListIndex = 0;

            int textMatchIndex = integerList.get(currentListIndex);
            highlightMatches(textArea, textMatchIndex, currentListIndex++);
        });

        // ------- PREVIOUS BUTTON -------
        previousButton = new JButton("Previous");
        previousButton.addActionListener(e -> {

        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // checkboxes
        final JPanel checkPanel = new JPanel();
        checkPanel.add(caseInsensitiveCheckBox);
        checkPanel.add(wholeWordCheckBox);
        checkPanel.add(regexCheckBox);
        checkPanel.setLayout(new FlowLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        this.add(checkPanel, gbc);

        // next and previous buttons
        final JPanel nextAndPreviousPanel = new JPanel();
        nextAndPreviousPanel.add(nextButton);
        nextAndPreviousPanel.add(previousButton);
        nextAndPreviousPanel.setLayout(new FlowLayout());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        this.add(nextAndPreviousPanel, gbc);

        // text field (for the pattern)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(patternTextField, gbc);

        // find button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        this.add(findButton, gbc);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Removing the highlights before closing.
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                removeHighlights(textArea);
            }
        });
        this.setLocationRelativeTo(frame);
//        this.setSize(400, 400);
        this.pack();
        this.setVisible(true);
    }

    private void highlightMatches(JTextArea textArea, int textMatchIndex, int localCurrentListIndex)
    {
        // Clear previous highlights
        removeHighlights(textArea);
        System.out.println(localCurrentListIndex);
        System.out.println(textMatchIndex);

        for (int index = 0; index < integerList.size(); ++index)
        {
            try
            {
                if(index != localCurrentListIndex)
                {
                    textArea.getHighlighter().addHighlight(
                            integerList.get(index),
                            integerList.get(index) + currentPattern.length(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW)
                    );
                }
                else // Highlights the current match in a different way
                {
                    textArea.getHighlighter().addHighlight(
                            integerList.get(index),
                            integerList.get(index) + currentPattern.length(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.RED)
                    );
                }
            }
            catch (BadLocationException ex)
            {
                ex.printStackTrace();
            }
        }

        textArea.setCaretPosition(textMatchIndex);
        textArea.select(textMatchIndex, textMatchIndex + currentPattern.length());
        textArea.grabFocus();
    }

    private void removeHighlights(JTextArea textArea)
    {
        Highlighter hilite = textArea.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (Highlighter.Highlight highlight : hilites)
        {
            if (highlight.getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter)
            {
                hilite.removeHighlight(highlight);
            }
        }
    }

}
