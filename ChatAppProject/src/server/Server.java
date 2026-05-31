package server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.SwingUtilities;

import common.models.DBConnection;
import server.gui.ServerGUI;
import server.handlers.ClientHandler;

public class Server {

  /** Callback interface để GUI nhận thông báo từ server. */
  public interface ServerListener {
    void onClientConnected(ClientHandler handler);
    void onClientDisconnected(ClientHandler handler);
    void onLog(String message);
    void onStatusChanged(boolean running);
  }

  public static ConcurrentHashMap<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

  private ServerSocket serverSocket;
  private int port;
  private volatile boolean running = false;
  private Thread acceptThread;
  private ServerListener listener;

  // Danh sách tất cả client handler (kể cả chưa login)
  private final CopyOnWriteArrayList<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();

  public Server() {
    this(4321);
  }

  public Server(int port) {
    this.port = port;
  }

  public void setListener(ServerListener listener) {
    this.listener = listener;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isRunning() {
    return running;
  }

  public CopyOnWriteArrayList<ClientHandler> getConnectedClients() {
    return connectedClients;
  }

  /** Kiểm tra kết nối database. Trả về true nếu OK. */
  public boolean testDatabaseConnection() {
    try {
      Connection conn = DBConnection.getConnection();
      if (conn != null) {
        conn.close();
        log("Database connection OK");
        return true;
      } else {
        log("Database connection failed: null");
        return false;
      }
    } catch (SQLException e) {
      log("Database connection failed: " + e.getMessage());
      return false;
    }
  }

  /** Bắt đầu server: mở ServerSocket và accept client trong thread riêng. */
  public void start(int port) {
    if (running) {
      log("Server is already running");
      return;
    }
    this.port = port;

    // Kiểm tra DB trước
    if (!testDatabaseConnection()) {
      log("Cannot start server: database connection failed");
      return;
    }

    try {
      serverSocket = new ServerSocket(port);
      running = true;
      log("Server started on port " + port);
      if (listener != null) {
        listener.onStatusChanged(true);
      }

      acceptThread = new Thread(() -> {
        while (running && !serverSocket.isClosed()) {
          try {
            Socket clientSocket = serverSocket.accept();
            String clientAddr = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
            log("Client connected: " + clientAddr);

            ClientHandler handler = new ClientHandler(clientSocket, this);
            connectedClients.add(handler);

            if (listener != null) {
              SwingUtilities.invokeLater(() -> listener.onClientConnected(handler));
            }

            Thread thread = new Thread(handler);
            thread.setDaemon(true);
            thread.start();

          } catch (IOException e) {
            if (running) {
              log("Error accepting client: " + e.getMessage());
            }
            // Nếu !running → serverSocket đã bị đóng bởi stop(), bình thường
          }
        }
      }, "server-accept");
      acceptThread.setDaemon(true);
      acceptThread.start();

    } catch (IOException e) {
      log("Failed to start server: " + e.getMessage());
      running = false;
      if (listener != null) {
        listener.onStatusChanged(false);
      }
    }
  }

  /** Dừng server: đóng ServerSocket và tất cả client. */
  public void stop() {
    if (!running) {
      log("Server is not running");
      return;
    }
    running = false;

    // Đóng server socket → acceptThread sẽ thoát
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
      log("Error closing server socket: " + e.getMessage());
    }

    // Đóng tất cả client
    for (ClientHandler handler : connectedClients) {
      handler.closeConnection();
    }
    connectedClients.clear();
    onlineUsers.clear();

    log("Server stopped");
    if (listener != null) {
      listener.onStatusChanged(false);
    }
  }

  /** Được gọi bởi ClientHandler khi client ngắt kết nối. */
  public void notifyClientDisconnected(ClientHandler handler) {
    connectedClients.remove(handler);
    if (listener != null) {
      SwingUtilities.invokeLater(() -> listener.onClientDisconnected(handler));
    }
  }

  /** Được gọi bởi ClientHandler khi user login thành công (username thay đổi). */
  public void notifyClientUpdated(ClientHandler handler) {
    if (listener != null) {
      SwingUtilities.invokeLater(() -> listener.onClientConnected(handler));
    }
  }

  /** Ghi log qua listener. */
  public void log(String message) {
    String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    String fullMessage = "[" + timestamp + "] " + message;
    System.out.println(fullMessage);
    if (listener != null) {
      SwingUtilities.invokeLater(() -> listener.onLog(fullMessage));
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ServerGUI());
  }
}
