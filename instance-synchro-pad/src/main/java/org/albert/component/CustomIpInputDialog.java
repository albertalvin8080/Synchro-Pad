package org.albert.component;

import org.albert.document.IpDocumentFilter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CustomIpInputDialog extends JDialog
{
    private final JTextField[] ipFields = new JTextField[4];  // 4 fields for the 4 IP octets

    // Default behavior: cancel operation
    private int option = JOptionPane.CANCEL_OPTION;

    public CustomIpInputDialog(SynchroPad synchroPad)
    {
        super(synchroPad, "Enter server's IP address", ModalityType.APPLICATION_MODAL);
        this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        final JPanel ipFieldsPanel = new JPanel(new FlowLayout());
        for (int i = 0; i < 4; i++)
        {
            ipFields[i] = new JTextField(3);
            // Set the character limit
            ((AbstractDocument) ipFields[i].getDocument()).setDocumentFilter(new IpDocumentFilter(3));

            int currentIndex = i;  // Store index for focus switching
            ipFields[i].addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyReleased(KeyEvent e)
                {
                    final char keyChar = e.getKeyChar();
                    if(!Character.isDigit(keyChar)) return;

                    if (ipFields[currentIndex].getText().length() == 3 && currentIndex < 3)
                    {
                        ipFields[currentIndex + 1].requestFocus();  // Move focus to the next field
                    }
                }
            });

            ipFieldsPanel.add(ipFields[i]);

            if (i < 3)
            {
                ipFieldsPanel.add(new JLabel("."));  // Add a dot between fields
            }
        }

        // Default IP value
        ipFields[0].setText("192");
        ipFields[1].setText("168");
        ipFields[2].setText("1");
        ipFields[3].setText("6");

        // -------------- BUTTONS --------------
        final JPanel btnPanel = new JPanel(new FlowLayout());

        final JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            option = JOptionPane.OK_OPTION;
            this.dispose();
        });
        // Enter will trigger okBtn even without the focus
        this.getRootPane().setDefaultButton(okBtn);

        final JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.addActionListener(e -> {
            option = JOptionPane.CANCEL_OPTION;
            this.dispose();
        });

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        // !-------------- BUTTONS --------------

        this.add(ipFieldsPanel);
        this.add(btnPanel);

        this.setLocationRelativeTo(synchroPad);
        this.pack();
        this.setVisible(true);
    }

    public String getIpAddress()
    {
        StringBuilder ip = new StringBuilder();
        for (int i = 0; i < 4; i++)
        {
            ip.append(ipFields[i].getText());
            if (i < 3)
            {
                ip.append(".");
            }
        }
        return ip.toString();
    }

    public int getOption()
    {
        return option;
    }

    // This were inside the constructor
    // NOT WORKING: Request focus for the first JTextField after the panel is fully initialized
//        SwingUtilities.invokeLater(() -> {
//            System.out.println("UE");
//            ipFields[0].requestFocusInWindow();
//        });
    // NOT WORKING ON ARCH: Add a focus listener to ensure the first field gains focus when the panel is displayed
//        addAncestorListener(new AncestorListener() {
//            @Override
//            public void ancestorAdded(AncestorEvent event) {
//                ipFields[0].requestFocusInWindow();  // Request focus when the panel is added to the window
//            }
//
//            @Override
//            public void ancestorRemoved(AncestorEvent event) {
//                // Do nothing
//            }
//
//            @Override
//            public void ancestorMoved(AncestorEvent event) {
//                // Do nothing
//            }
//        });
}
