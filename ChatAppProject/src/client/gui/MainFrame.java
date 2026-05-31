package client.gui;

import javax.swing.*;

import client.ServerHandler;
import client.model.CurrentUser;
import client.model.ServerInfo;
import client.service.ClientAuthService;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainFrame extends JFrame {
  JPanel mainPanel;
  ServerListPanel serverListPanel;
  LoginPanel loginPanel;
  RegisterPanel registerPanel;
  DashBoardPanel dashBoardPanel;
  ClientAuthService clientAuthService;
  CurrentUser currentUser;

  // Kết nối hiện tại (null nếu chưa kết nối)
  private Socket currentSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ServerHandler serverHandler;
  private ServerInfo connectedServerInfo;

  /**
   * Constructor mới: KHÔNG kết nối ngay.
   * Hiển thị ServerListPanel trước, user chọn server rồi mới connect.
   */
  public MainFrame() {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BorderLayout());
    this.setSize(900, 700);
    this.setLocationRelativeTo(null);
    this.setTitle("Messenger");
    java.net.URL iconUrl = MainFrame.class.getResource("./asset/dialogue.png");
    if (iconUrl != null) {
      ImageIcon img = new ImageIcon(iconUrl);
      this.setIconImage(img.getImage());
    }

    mainPanel = new JPanel(new CardLayout());

    // Khởi tạo model
    currentUser = new CurrentUser();

    // Khởi tạo ServerListPanel (không cần in/out)
    serverListPanel = new ServerListPanel(this);
    mainPanel.add(serverListPanel, "serverListPanel");

    // DashBoardPanel khởi tạo sẵn (sẽ set serverHandler sau)
    dashBoardPanel = new DashBoardPanel();

    this.add(mainPanel, BorderLayout.CENTER);
    showServerListPanel();
    this.setVisible(true);
  }

  /**
   * Constructor cũ giữ nguyên để backward compatible.
   * Kết nối sẵn in/out, hiển thị LoginPanel ngay.
   */
  public MainFrame(ObjectInputStream in, ObjectOutputStream out) throws IOException {
    this.in = in;
    this.out = out;
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BorderLayout());
    this.setSize(900, 700);
    this.setLocationRelativeTo(null);
    this.setTitle("Messenger");
    java.net.URL iconUrl = MainFrame.class.getResource("./asset/dialogue.png");
    if (iconUrl != null) {
      ImageIcon img = new ImageIcon(iconUrl);
      this.setIconImage(img.getImage());
    }

    mainPanel = new JPanel(new CardLayout());

    // Khởi tạo service
    clientAuthService = new ClientAuthService(in, out);

    // Khởi tạo model
    currentUser = new CurrentUser();

    // Khởi tạo panel
    loginPanel = new LoginPanel(mainPanel, clientAuthService);
    registerPanel = new RegisterPanel(mainPanel, clientAuthService);
    dashBoardPanel = new DashBoardPanel();

    // Thêm vào panel chính
    mainPanel.add(loginPanel, "loginPanel");
    mainPanel.add(registerPanel, "registerPanel");
    mainPanel.add(dashBoardPanel, "dashBoardPanel");

    this.add(mainPanel, BorderLayout.CENTER);
    this.setVisible(true);
  }

  /**
   * Kết nối đến server đã chọn.
   * Gọi bởi ServerListPanel khi user nhấn Connect.
   */
  public void connectToServer(ServerInfo serverInfo) throws IOException {
    // Đóng kết nối cũ nếu có
    disconnectFromServer();

    // Tạo socket mới
    Socket s = new Socket(serverInfo.getHost(), serverInfo.getPort());
    ObjectOutputStream newOut = new ObjectOutputStream(s.getOutputStream());
    newOut.flush();
    ObjectInputStream newIn = new ObjectInputStream(s.getInputStream());

    this.currentSocket = s;
    this.in = newIn;
    this.out = newOut;
    this.connectedServerInfo = serverInfo;

    // Khởi tạo services
    clientAuthService = new ClientAuthService(newIn, newOut);

    // Khởi tạo Login/Register nếu chưa có hoặc tạo lại
    if (loginPanel != null) {
      mainPanel.remove(loginPanel);
    }
    if (registerPanel != null) {
      mainPanel.remove(registerPanel);
    }

    loginPanel = new LoginPanel(mainPanel, clientAuthService);
    registerPanel = new RegisterPanel(mainPanel, clientAuthService);

    mainPanel.add(loginPanel, "loginPanel");
    mainPanel.add(registerPanel, "registerPanel");

    // Đảm bảo dashBoardPanel trong mainPanel
    boolean hasDashBoard = false;
    for (Component c : mainPanel.getComponents()) {
      if (c == dashBoardPanel) {
        hasDashBoard = true;
        break;
      }
    }
    if (!hasDashBoard) {
      mainPanel.add(dashBoardPanel, "dashBoardPanel");
    }

    // Cập nhật server info trên DashBoard
    dashBoardPanel.setConnectedServerInfo(serverInfo);

    // Khởi tạo ServerHandler
    serverHandler = new ServerHandler(newIn, newOut, this, clientAuthService);
    loginPanel.setServerHandler(serverHandler);
    registerPanel.setServerHandler(serverHandler);
    dashBoardPanel.setServerHandler(serverHandler);
    serverHandler.execute();

    // Chuyển sang LoginPanel
    SwingUtilities.invokeLater(this::showLoginPanel);
  }

  /** Ngắt kết nối khỏi server hiện tại. */
  public void disconnectFromServer() {
    if (serverHandler != null) {
      serverHandler.cancel(true);
      serverHandler = null;
    }
    if (currentSocket != null) {
      try {
        currentSocket.close();
      } catch (IOException ignored) {
      }
      currentSocket = null;
    }
    connectedServerInfo = null;
    currentUser.setUsername(null);
    currentUser.setUserId(0);
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

  public ServerInfo getConnectedServerInfo() {
    return connectedServerInfo;
  }

  public void showServerListPanel() {
    CardLayout cl = (CardLayout) mainPanel.getLayout();
    cl.show(mainPanel, "serverListPanel");
  }

  public void showDashBoardPanel() {
    CardLayout cl = (CardLayout) mainPanel.getLayout();
    cl.show(mainPanel, "dashBoardPanel");
  }

  public void showLoginPanel() {
    CardLayout cl = (CardLayout) mainPanel.getLayout();
    cl.show(mainPanel, "loginPanel");
  }

  /**
   * Sign out: reset dashboard, quay về ServerListPanel (thay vì LoginPanel).
   */
  public void handleSignOutAndDisconnect() {
    dashBoardPanel.resetForSignOut();
    disconnectFromServer();
    showServerListPanel();
  }
}
