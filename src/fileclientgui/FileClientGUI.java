import javax.swing.*;
import java.io.*;
import java.net.*;

public class FileClientGUI extends JFrame {
    private JTextField txtFilePath, txtServerIP;
    private JButton btnBrowse, btnSend;
    private JTextArea logArea;

    private File selectedFile;

    public FileClientGUI() {
        setTitle("File Client");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lblIP = new JLabel("Server IP:");
        lblIP.setBounds(20, 20, 80, 25);
        add(lblIP);

        txtServerIP = new JTextField("127.0.0.1");
        txtServerIP.setBounds(100, 20, 150, 25);
        add(txtServerIP);

        JLabel lblFile = new JLabel("File:");
        lblFile.setBounds(20, 60, 80, 25);
        add(lblFile);

        txtFilePath = new JTextField();
        txtFilePath.setBounds(100, 60, 250, 25);
        txtFilePath.setEditable(false);
        add(txtFilePath);

        btnBrowse = new JButton("Browse");
        btnBrowse.setBounds(360, 60, 90, 25);
        add(btnBrowse);

        btnSend = new JButton("Send File");
        btnSend.setBounds(180, 100, 120, 30);
        add(btnSend);

        logArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBounds(20, 150, 440, 180);
        add(scrollPane);

        // Button actions
        btnBrowse.addActionListener(e -> browseFile());
        btnSend.addActionListener(e -> sendFile());
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    private void sendFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            log("File belum dipilih atau tidak ditemukan.");
            return;
        }

        String serverIP = txtServerIP.getText();
        new Thread(() -> {
            try (Socket socket = new Socket(serverIP, 5000)) {
                log("Terhubung ke server: " + serverIP);

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                FileInputStream fis = new FileInputStream(selectedFile);

                dos.writeUTF(selectedFile.getName());
                dos.writeLong(selectedFile.length());

                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, read);
                }

                log("File berhasil dikirim.");
                fis.close();
                dos.close();
                socket.close();
            } catch (IOException ex) {
                log("Gagal mengirim file: " + ex.getMessage());
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FileClientGUI().setVisible(true);
        });
    }
}
