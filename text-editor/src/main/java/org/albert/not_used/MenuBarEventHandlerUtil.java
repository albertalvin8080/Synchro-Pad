package org.albert.not_used;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.stream.Collectors;

public class MenuBarEventHandlerUtil
{
    private final JFrame frame;
    private final JTextArea textArea;
    private String currentFilePath;

    public MenuBarEventHandlerUtil(JFrame frame, JTextArea textPane)
    {
        this.frame = frame;
        this.textArea = textPane;
//        final MenuBarEventHandlerUtil menuBarEventHandlerUtil = new MenuBarEventHandlerUtil(this, textArea);
//        saveMenuItem.addActionListener(menuBarEventHandlerUtil::Save);
//        openMenuItem.addActionListener(menuBarEventHandlerUtil::Open);
//        exitMenuItem.addActionListener(menuBarEventHandlerUtil::Exit);
    }

    public void Exit(ActionEvent e)
    {
        final int op = JOptionPane.showConfirmDialog(frame, "Are you sure?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION)
        {
            System.exit(0);
        }
    }

    public void Save(ActionEvent e)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        final int option;
        final File file;

        // If there's already a select file.
        if (currentFilePath != null)
        {
            option = JFileChooser.APPROVE_OPTION;
            file = new File(currentFilePath);
        }
        // If the file being saved doesn't yet exist.
        else
        {
            option = fileChooser.showSaveDialog(frame);
            final File selectedFile = fileChooser.getSelectedFile();
            // No file selected.
            if (selectedFile == null) return;

            file = new File(selectedFile.getAbsolutePath());
            currentFilePath = selectedFile.getAbsolutePath();
        }

        if (option == JFileChooser.APPROVE_OPTION)
        {
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
    }

    public void Open(ActionEvent e)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        final int option = fileChooser.showOpenDialog(frame);
        final File file;

        if (option == JFileChooser.APPROVE_OPTION)
        {
            file = new File(fileChooser.getSelectedFile().getAbsolutePath());
            currentFilePath = file.getAbsolutePath();

            try (FileReader fr = new FileReader(file);
                 BufferedReader br = new BufferedReader(fr))
            {
                final String collect = br.lines().collect(Collectors.joining(System.lineSeparator()));
                textArea.setText(collect);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}