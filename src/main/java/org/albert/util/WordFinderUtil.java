package org.albert.util;

import org.albert.component.FindMenuItemDialog;
import org.albert.design_patterns.decorator.contract.PatternFinder;
import org.albert.design_patterns.decorator.decorated.PlainPatternFinder;
import org.albert.design_patterns.decorator.decorator.CaseInsensitivePatternFinderDecorator;
import org.albert.design_patterns.decorator.decorator.WholeWordPatternFinderDecorator;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.List;

public class WordFinderUtil
{
    private List<WordIndex> wordIndexList;
    private String currentPattern;
    private int currentListIndex;

    private final FindMenuItemDialog findMenuItemDialog;
    private final JTextArea textArea;
    private final JTextField patternTextField;
    private final JCheckBox caseInsensitiveCheckBox;
    private final JCheckBox wholeWordCheckBox;

    public WordFinderUtil(FindMenuItemDialog findMenuItemDialog, JTextArea textArea, JTextField patternTextField, JCheckBox caseInsensitiveCheckBox, JCheckBox wholeWordCheckBox)
    {
        this.findMenuItemDialog = findMenuItemDialog;
        this.textArea = textArea;
        this.patternTextField = patternTextField;
        this.caseInsensitiveCheckBox = caseInsensitiveCheckBox;
        this.wholeWordCheckBox = wholeWordCheckBox;
    }

    public void find()
    {
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
            JOptionPane.showMessageDialog(findMenuItemDialog, "Pattern not found!", "Find", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void next()
    {
        if (wordIndexList == null || wordIndexList.isEmpty()) return;

        // Returns to the start if it's already the last index.
        if(currentListIndex + 1 >= wordIndexList.size())
        {
            currentListIndex = -1;
        }

        WordIndex wordIndex = wordIndexList.get(++currentListIndex);
        highlightMatches(textArea, wordIndex, currentListIndex);
    }

    public void previous()
    {
        if (wordIndexList == null || wordIndexList.isEmpty()) return;

        // Goes to the end if it's currently at the first index.
        if(currentListIndex - 1 < 0)
        {
            currentListIndex = wordIndexList.size();
        }

        WordIndex wordIndex = wordIndexList.get(--currentListIndex);
        highlightMatches(textArea, wordIndex, currentListIndex);
    }

    private void highlightMatches(JTextArea textArea, WordIndex wordIndex, int localCurrentListIndex)
    {
        // Clear previous highlights
        removeHighlights();
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

    public void removeHighlights()
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
