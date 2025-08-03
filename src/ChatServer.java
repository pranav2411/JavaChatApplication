import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatServer {
    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = new CopyOnWriteArraySet<>();

    public static void main(String[] args) throws IOException {
        new File("ChatFiles").mkdirs();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("ChatServer started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientName = in.readLine(); // First line: client name

                broadcast("SERVER: " + clientName + " joined the chat", null);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("FILE:")) {
                        receiveFile(message.substring(5));
                    } else {
                        String timestamp = new Date().toString();
                        String full = "[" + timestamp + "] " + clientName + ": " + message;
                        broadcast(full, this);
                    }
                }
            } catch (IOException e) {
                System.out.println(clientName + " disconnected.");
            } finally {
                clients.remove(this);
                broadcast("SERVER: " + clientName + " left the chat", null);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void broadcast(String message, ClientHandler exclude) {
            for (ClientHandler c : clients) {
                if (c != exclude) {
                    c.out.println(message);
                }
            }
        }

        private void receiveFile(String filename) throws IOException {
            // Next line is file size
            long fileSize = Long.parseLong(in.readLine());
            File outFile = new File("ChatFiles/" + filename);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                while (remaining > 0) {
                    int read = is.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                    if (read == -1) break;
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
            }

            broadcast("SERVER: " + clientName + " sent file: " + filename, this);
            relayFileToOthers(filename, fileSize);
        }

        private void relayFileToOthers(String filename, long size) {
            for (ClientHandler c : clients) {
                if (c != this) {
                    try {
                        c.out.println("FILE:" + filename);
                        c.out.println(size);
                        File file = new File("ChatFiles/" + filename);
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int count;
                            OutputStream os = c.socket.getOutputStream();
                            while ((count = fis.read(buffer)) > 0) {
                                os.write(buffer, 0, count);
                            }
                            os.flush();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
