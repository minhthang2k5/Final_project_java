package client.gui;

import javax.swing.*;

import client.service.ClientAuthService;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class MainFrame extends JFrame {
  JPanel mainPanel;
  LoginPanel loginPanel;
  public MainFrame(Socket socket) throws IOException {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BorderLayout());
    this.setSize(900,700);
    this.setLocationRelativeTo(null);
    this.setTitle("Messenger");
    java.net.URL iconUrl = MainFrame.class.getResource("./asset/dialogue.png");
    if (iconUrl != null) {
      ImageIcon img = new ImageIcon(iconUrl);
      this.setIconImage(img.getImage());
    }

    mainPanel = new JPanel(new CardLayout());

    //Khởi tạo service
    ClientAuthService clientAuthService = new ClientAuthService(socket);


    //Khởi tạo panel
    loginPanel = new LoginPanel(mainPanel,clientAuthService);

    //Thêm vào panel chính
    mainPanel.add(loginPanel,"loginPanel");



    this.add(mainPanel, BorderLayout.CENTER);
    this.setVisible(true);
  }
}
