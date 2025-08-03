import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final JTextArea chatArea;
    private final String username;

    public ChatClient(String host, int port, String username, JTextArea chatArea) throws IOException {
        this.username = username;
        this.chatArea = chatArea;
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(username); // send username immediately
        new File("ChatFiles").mkdirs();
    }

    public void start() {
        new Thread(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("FILE:")) {
                        receiveFile(message.substring(5));
                    } else {
                        chatArea.append(message + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                }
            } catch (IOException e) {
                chatArea.append("Connection closed.\n");
            }
        }).start();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void sendFile(File file) throws IOException {
        out.println("FILE:" + file.getName());
        out.println(file.length());
        out.flush();

        try (FileInputStream fis = new FileInputStream(file)) {
            OutputStream os = socket.getOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
    }

    private void receiveFile(String filename) throws IOException {
        long size = Long.parseLong(in.readLine());
        File outFile = new File("ChatFiles/" + filename);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[4096];
            long remaining = size;
            while (remaining > 0) {
                int read = is.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                if (read == -1) break;
                fos.write(buffer, 0, read);
                remaining -= read;
            }
        }
        chatArea.append("File received: ChatFiles/" + filename + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
