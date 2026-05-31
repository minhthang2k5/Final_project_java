package client;

import javax.swing.SwingUtilities;

import client.gui.MainFrame;

public class Client {
  public static void main(String[] args) {
    // Khởi tạo GUI, hiển thị ServerListPanel trước.
    // User chọn server → kết nối → hiển thị LoginPanel.
    SwingUtilities.invokeLater(() -> new MainFrame());
  }
}
