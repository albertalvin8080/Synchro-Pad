package org.albert.component;

import org.albert.design_patterns.decorator.contract.PatternFinder;
import org.albert.design_patterns.decorator.decorated.PlainPatternFinder;
import org.albert.design_patterns.decorator.decorator.CaseInsensitivePatternFinderDecorator;
import org.albert.design_patterns.decorator.decorator.WholeWordPatternFinderDecorator;
import org.albert.util.WordIndex;

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

    private List<WordIndex> wordIndexList;
    private String currentPattern;
    private int currentListIndex;

    public FindMenuItemDialog(JFrame frame, JTextArea textArea)
    {
        super(frame, "Finder Dialog", JDialog.ModalityType.APPLICATION_MODAL);

        caseInsensitiveCheckBox = new JCheckBox("Cc");
        caseInsensitiveCheckBox.setFocusable(false);
        wholeWordCheckBox = new JCheckBox("W");
        wholeWordCheckBox.setFocusable(false);

        patternTextField = new JTextField();
        findButton = new JButton("Find");
        findButton.addActionListener(e -> {
            currentPattern = patternTextField.getText();

            PatternFinder patternFinder = new PlainPatternFinder();
            if (caseInsensitiveCheckBox.isSelected())
                patternFinder = new CaseInsensitivePatternFinderDecorator(patternFinder);
            if (wholeWordCheckBox.isSelected())
                patternFinder = new WholeWordPatternFinderDecorator(patternFinder);

            wordIndexList = patternFinder.find(textArea.getText(), currentPattern);

            if (!wordIndexList.isEmpty())
            {
                WordIndex wordIndex = wordIndexList.get(0);
                currentListIndex = 0;
                highlightMatches(textArea, wordIndex, currentListIndex);
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Pattern not found!", "Find", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // ------- NEXT BUTTON -------
        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {
            if (wordIndexList == null || wordIndexList.isEmpty()) return;

            // Returns to the start if it's already the last index.
            if(currentListIndex + 1 >= wordIndexList.size())
            {
                currentListIndex = -1;
            }

            WordIndex wordIndex = wordIndexList.get(++currentListIndex);
            highlightMatches(textArea, wordIndex, currentListIndex);
        });
        // !------- NEXT BUTTON -------

        // ------- PREVIOUS BUTTON -------
        previousButton = new JButton("Previous");
        previousButton.addActionListener(e -> {
            if (wordIndexList == null || wordIndexList.isEmpty()) return;

            // Goes to the end if it's currently at the first index.
            if(currentListIndex - 1 < 0)
            {
                currentListIndex = wordIndexList.size();
            }

            WordIndex wordIndex = wordIndexList.get(--currentListIndex);
            highlightMatches(textArea, wordIndex, currentListIndex);
        });
        // !------- PREVIOUS BUTTON -------

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // checkboxes
        final JPanel checkPanel = new JPanel();
        checkPanel.add(caseInsensitiveCheckBox);
        checkPanel.add(wholeWordCheckBox);
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

    private void highlightMatches(JTextArea textArea, WordIndex wordIndex, int localCurrentListIndex)
    {
        // Clear previous highlights
        removeHighlights(textArea);
        System.out.println(localCurrentListIndex);
        System.out.println(wordIndex.getStart());
        System.out.println(wordIndex.getEnd());

        for (int index = 0; index < wordIndexList.size(); ++index)
        {
            try
            {
                final WordIndex temp = wordIndexList.get(index);
                if(index != localCurrentListIndex)
                {
                    textArea.getHighlighter().addHighlight(
                            temp.getStart(),
                            temp.getEnd(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW)
                    );
                }
                else // Highlights the current match in a different way
                {
                    textArea.getHighlighter().addHighlight(
                            temp.getStart(),
                            temp.getEnd(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.RED)
                    );
                }
            }
            catch (BadLocationException ex)
            {
                ex.printStackTrace();
            }
        }

        textArea.setCaretPosition(wordIndex.getStart());
        textArea.select(wordIndex.getStart(), wordIndex.getEnd());
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
