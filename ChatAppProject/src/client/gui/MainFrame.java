package client.gui;

import javax.swing.*;

import client.model.CurrentUser;
import client.service.ClientAuthService;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MainFrame extends JFrame {
  JPanel mainPanel;
  LoginPanel loginPanel;
  RegisterPanel registerPanel;
  DashBoardPanel dashBoardPanel;
  ClientAuthService clientAuthService;
  CurrentUser currentUser;
  public MainFrame(ObjectInputStream in, ObjectOutputStream out) throws IOException {
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
    clientAuthService = new ClientAuthService(in, out);
    
    //Khởi tạo model
    currentUser = new CurrentUser();


    //Khởi tạo panel
    loginPanel = new LoginPanel(mainPanel,clientAuthService);
    registerPanel = new RegisterPanel(mainPanel, clientAuthService);
    dashBoardPanel = new DashBoardPanel();

    
    //Thêm vào panel chính
    mainPanel.add(loginPanel,"loginPanel");
    mainPanel.add(registerPanel,"registerPanel");
    mainPanel.add(dashBoardPanel,"dashBoardPanel");


    this.add(mainPanel, BorderLayout.CENTER);
    this.setVisible(true);
  }

  public DashBoardPanel getDashBoardPanel() {
    return dashBoardPanel;
  }

  public LoginPanel getLoginPanel() {
    return loginPanel;
  }

  public RegisterPanel getRegisterPanel() {
    return registerPanel;
  }

  public ClientAuthService getClientAuthService() {
    return clientAuthService;
  }

  public CurrentUser getCurrentUser() {
    return currentUser;
  }

  public void showDashBoardPanel() {
    CardLayout cl = (CardLayout) mainPanel.getLayout();
    cl.show(mainPanel, "dashBoardPanel");
  }
}
