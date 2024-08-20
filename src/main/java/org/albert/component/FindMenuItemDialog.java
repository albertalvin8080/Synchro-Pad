package org.albert.component;

import org.albert.util.WordFinderUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FindMenuItemDialog extends JDialog
{
    private final WordFinderUtil wordFinderUtil;

    public FindMenuItemDialog(JFrame frame, JTextArea textArea)
    {
        super(frame, "Finder Dialog", JDialog.ModalityType.APPLICATION_MODAL);

        JCheckBox caseInsensitiveCheckBox = new JCheckBox("Cc");
        caseInsensitiveCheckBox.setToolTipText("Case insensitive search.");
        caseInsensitiveCheckBox.setFocusable(false);
        JCheckBox wholeWordCheckBox = new JCheckBox("W");
        wholeWordCheckBox.setToolTipText("Find whole words only.");
        wholeWordCheckBox.setFocusable(false);

        JTextField patternTextField = new JTextField();

        wordFinderUtil = new WordFinderUtil(this, textArea, patternTextField, caseInsensitiveCheckBox, wholeWordCheckBox);

        // ------- FIND BUTTON -------
        JButton findButton = new JButton("Find");
        // Triggers the button when "enter" is pressed.
        this.getRootPane().setDefaultButton(findButton);
        findButton.addActionListener(e -> wordFinderUtil.find());

        // ------- NEXT BUTTON -------
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> wordFinderUtil.next());

        // ------- PREVIOUS BUTTON -------
        JButton previousButton = new JButton("Previous");
        previousButton.addActionListener(e -> wordFinderUtil.previous());

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

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Removing the highlights before closing.
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                wordFinderUtil.removeHighlights();
            }
        });
        this.setLocationRelativeTo(frame);
//        this.setSize(400, 400);
        this.pack();
        this.setVisible(true);
    }

}
