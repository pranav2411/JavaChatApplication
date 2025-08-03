import java.awt.*;
import java.io.File;
import javax.swing.*;

public class ChatClientGUI extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private ChatClient client;

    public ChatClientGUI() {
        setTitle("Java Chat Client");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // At first, only show dialog to get connection details and connect
        connectDialogAndSetupUI();
    }

    private void connectDialogAndSetupUI() {
        JTextField ipField = new JTextField("127.0.0.1");
        JTextField portField = new JTextField("5000");
        JTextField nameField = new JTextField();

        Object[] fields = {
            "IP Address:", ipField,
            "Port:", portField,
            "Username:", nameField
        };

        int option = JOptionPane.showConfirmDialog(
                this, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String ip = ipField.getText().trim();
            int port;
            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Port must be a number");
                connectDialogAndSetupUI();  // ask again
                return;
            }
            String username = nameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username cannot be empty");
                connectDialogAndSetupUI();  // ask again
                return;
            }

            try {
                // Initialize GUI components after successful connection
                initChatUI();

                // Create client connection
                client = new ChatClient(ip, port, username, chatArea);
                client.start();

                chatArea.append("Connected as " + username + "\n");
                setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage());
                connectDialogAndSetupUI();  // ask again
            }
        } else {
            // User canceled - close app
            System.exit(0);
        }
    }

    private void initChatUI() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JButton sendFileButton = new JButton("Send File");
        sendFileButton.addActionListener(e -> sendFile());

        JButton callButton = new JButton("Voice Call");
        callButton.addActionListener(e -> startVoiceCall());

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(sendFileButton);
        buttonPanel.add(callButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Clear any existing content and add components
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // Refresh the frame
        validate();
        repaint();
    }

    private void sendMessage() {
        if (client == null) return;

        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;

        // Show sent message with timestamp locally
        String timestamp = new java.util.Date().toString();
        chatArea.append("[" + timestamp + "] Me: " + msg + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());

        client.sendMessage(msg);
        messageField.setText("");
    }

    private void sendFile() {
        if (client == null) return;

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            new Thread(() -> {
                try {
                    client.sendFile(file);
                    SwingUtilities.invokeLater(() ->
                        chatArea.append("File sent: " + file.getName() + "\n")
                    );
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Failed to send file: " + e.getMessage())
                    );
                }
            }).start();
        }
    }

    private void startVoiceCall() {
        SwingUtilities.invokeLater(VoiceCallWindow::new);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
