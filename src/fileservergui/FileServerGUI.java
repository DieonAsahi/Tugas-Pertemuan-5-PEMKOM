import javax.swing.*;
import java.io.*;
import java.net.*;

public class FileServerGUI extends JFrame {
    private JTextArea logArea;
    private JButton startButton;

    public FileServerGUI() {
        setTitle("File Server");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        logArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBounds(20, 20, 440, 280);
        add(scrollPane);

        startButton = new JButton("Start Server");
        startButton.setBounds(170, 320, 150, 30);
        add(startButton);

        startButton.addActionListener(e -> startServer());
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5000)) {
                log("Server started on port 5000. Waiting for connection...");

                Socket socket = serverSocket.accept();
                log("Client connected: " + socket.getInetAddress());

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();

                FileOutputStream fos = new FileOutputStream("received_" + fileName);
                byte[] buffer = new byte[4096];
                int read;
                long remaining = fileSize;

                while ((read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }

                log("File '" + fileName + "' received successfully.");
                fos.close();
                dis.close();
                socket.close();
            } catch (IOException ex) {
                log("Error: " + ex.getMessage());
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FileServerGUI().setVisible(true);
        });
    }
}
