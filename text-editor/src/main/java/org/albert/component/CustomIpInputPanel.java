package org.albert.component;

import org.albert.document.IpDocumentFilter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CustomIpInputPanel extends JPanel
{
    private final JTextField[] ipFields = new JTextField[4];  // 4 fields for the 4 IP octets

    public CustomIpInputPanel()
    {
        setLayout(new FlowLayout());

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
                    if (ipFields[currentIndex].getText().length() == 3 && currentIndex < 3)
                    {
                        ipFields[currentIndex + 1].requestFocus();  // Move focus to the next field
                    }
                }
            });

            add(ipFields[i]);

            if (i < 3)
            {
                add(new JLabel("."));  // Add a dot between fields
            }
        }

        // NOT WORKING: Request focus for the first JTextField after the panel is fully initialized
//        SwingUtilities.invokeLater(() -> {
//            System.out.println("UE");
//            ipFields[0].requestFocusInWindow();
//        });
        // WORKING: Add a focus listener to ensure the first field gains focus when the panel is displayed
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                ipFields[0].requestFocusInWindow();  // Request focus when the panel is added to the window
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Do nothing
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // Do nothing
            }
        });
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

    public static void main(String[] args)
    {
        CustomIpInputPanel ipPanel = new CustomIpInputPanel();

        int result = JOptionPane.showConfirmDialog(
                null, ipPanel, "Enter IP Address", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION)
        {
            String serverIp = ipPanel.getIpAddress();
            System.out.println("Server IP: " + serverIp);
        }
    }
}
