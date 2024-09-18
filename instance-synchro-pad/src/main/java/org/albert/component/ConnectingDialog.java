package org.albert.component;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ConnectingDialog extends JDialog
{
    private final JButton cancelBtn;

    public ConnectingDialog(SynchroPad synchroPad, SwingWorker<Boolean, Void> worker)
    {
        // ModalityType.MODELESS makes this dialog nonblocking.
        super(synchroPad, "Connecting", ModalityType.MODELESS);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Prevent closing
//        this.setTitle("Connecting"); // Already set

        // Disable the close button (X)
//        this.addWindowListener(new java.awt.event.WindowAdapter() {
//            @Override
//            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
//                // Do nothing to prevent closing
//            }
//        });

        final URL loadingImg = ClassLoader.getSystemResource("loading.png");
        final Image scaledInstance = new ImageIcon(loadingImg.getPath()).getImage()
                .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel message = new JLabel(new ImageIcon(scaledInstance), JLabel.CENTER);
        this.add(message, BorderLayout.CENTER);

        final JPanel panel = new JPanel();

        cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(90, 30));
        cancelBtn.addActionListener(e -> {
            worker.cancel(true); // true allows interrupting the worker if necessary
            this.dispose(); // Close the dialog
        });
        panel.add(cancelBtn);
        this.add(panel, BorderLayout.SOUTH);

        this.setLocationRelativeTo(synchroPad);
        this.setSize(200, 150);
        this.setVisible(true);
    }
}
