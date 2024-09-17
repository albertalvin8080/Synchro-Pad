import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CustomIpInputPanel extends JPanel
{
    private JTextField[] ipFields = new JTextField[4];  // 4 fields for the 4 IP octets

    public CustomIpInputPanel()
    {
        setLayout(new FlowLayout());  // Layout for arranging the fields

        // Initialize the 4 fields and add dots between them
        for (int i = 0; i < 4; i++)
        {
            ipFields[i] = new JTextField(3);  // 3 characters max per field
            ((AbstractDocument) ipFields[i].getDocument()).setDocumentFilter(new IpDocumentFilter(3));  // Set the character limit

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

    // Custom DocumentFilter to limit input length
    class IpDocumentFilter extends DocumentFilter
    {
        private int maxLength;

        public IpDocumentFilter(int maxLength)
        {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
        {
            if (fb.getDocument().getLength() + string.length() <= maxLength)
            {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
        {
            if (fb.getDocument().getLength() - length + text.length() <= maxLength)
            {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    public static void main(String[] args)
    {
        // Create the custom IP panel
        CustomIpInputPanel ipPanel = new CustomIpInputPanel();

        // Show a JOptionPane with the custom panel
        int result = JOptionPane.showConfirmDialog(
                null, ipPanel, "Enter IP Address", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION)
        {
            // Get the IP address entered by the user
            String serverIp = ipPanel.getIpAddress();
            System.out.println("Server IP: " + serverIp);
        }
        System.out.println(ipPanel.getIpAddress());
    }
}
