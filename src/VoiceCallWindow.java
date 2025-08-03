import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

public class VoiceCallWindow extends JFrame {
    private final AtomicBoolean sending = new AtomicBoolean(false);
    private final AtomicBoolean receiving = new AtomicBoolean(false);
    private Thread sendThread;
    private Thread receiveThread;

    public VoiceCallWindow() {
        setTitle("Voice Call");
        setSize(500, 220);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField ipField = new JTextField("127.0.0.1");
        JTextField sendPortField = new JTextField("7000");
        JTextField receivePortField = new JTextField("7001");

        JButton startSendBtn = new JButton("Start Sending");
        JButton stopSendBtn = new JButton("Stop Sending");
        JButton startRecvBtn = new JButton("Start Receiving");
        JButton stopRecvBtn = new JButton("Stop Receiving");

        stopSendBtn.setEnabled(false);
        stopRecvBtn.setEnabled(false);

        JPanel grid = new JPanel(new GridLayout(6, 1, 5, 5));
        grid.add(new JLabel("Target IP (send to):"));
        grid.add(ipField);
        grid.add(new JLabel("Send Port (your mic output):"));
        grid.add(sendPortField);
        grid.add(new JLabel("Receive Port (listen on):"));
        grid.add(receivePortField);

        JPanel btnPanel = new JPanel();
        btnPanel.add(startSendBtn);
        btnPanel.add(stopSendBtn);
        btnPanel.add(startRecvBtn);
        btnPanel.add(stopRecvBtn);

        add(grid, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // --- SENDING AUDIO (MIC -> NETWORK) ---
        startSendBtn.addActionListener(e -> {
            if (!sending.get()) {
                sending.set(true);
                startSendBtn.setEnabled(false);
                stopSendBtn.setEnabled(true);
                String ip = ipField.getText().trim();
                int port = Integer.parseInt(sendPortField.getText().trim());
                sendThread = new Thread(() -> {
                    try {
                        VoiceCall.startSend(ip, port, sending::get);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Send error: " + ex.getMessage());
                    }
                });
                sendThread.start();
            }
        });

        stopSendBtn.addActionListener(e -> {
            sending.set(false);
            stopSendBtn.setEnabled(false);
            startSendBtn.setEnabled(true);
        });

        // --- RECEIVING AUDIO (NETWORK -> SPEAKERS) ---
        startRecvBtn.addActionListener(e -> {
            if (!receiving.get()) {
                receiving.set(true);
                startRecvBtn.setEnabled(false);
                stopRecvBtn.setEnabled(true);
                int port = Integer.parseInt(receivePortField.getText().trim());
                receiveThread = new Thread(() -> {
                    try {
                        VoiceCall.startReceive(port, receiving::get);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Receive error: " + ex.getMessage());
                    }
                });
                receiveThread.start();
            }
        });

        stopRecvBtn.addActionListener(e -> {
            receiving.set(false);
            stopRecvBtn.setEnabled(false);
            startRecvBtn.setEnabled(true);
        });

        setVisible(true);
    }
}
