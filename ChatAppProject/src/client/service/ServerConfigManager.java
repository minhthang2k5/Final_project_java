package client.service;

import client.model.ServerInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý danh sách server, lưu/đọc từ file config (servers.cfg).
 * Dùng format text đơn giản: mỗi dòng = name|host|port
 */
public class ServerConfigManager {

  private static final String CONFIG_FILE = "servers.cfg";
  private final File configFile;
  private final List<ServerInfo> servers;

  public ServerConfigManager() {
    // Lưu file config cùng thư mục chạy app
    configFile = new File(System.getProperty("user.dir"), CONFIG_FILE);
    servers = new ArrayList<>();
    loadServers();

    // Nếu chưa có server nào, thêm mặc định
    if (servers.isEmpty()) {
      servers.add(new ServerInfo("Local Server", "localhost", 4321));
      saveServers();
    }
  }

  public List<ServerInfo> getServers() {
    return servers;
  }

  public ServerInfo getServer(int index) {
    if (index < 0 || index >= servers.size()) return null;
    return servers.get(index);
  }

  public void addServer(ServerInfo server) {
    servers.add(server);
    saveServers();
  }

  public void removeServer(int index) {
    if (index >= 0 && index < servers.size()) {
      servers.remove(index);
      saveServers();
    }
  }

  public void updateServer(int index, ServerInfo server) {
    if (index >= 0 && index < servers.size()) {
      servers.set(index, server);
      saveServers();
    }
  }

  /** Đọc danh sách server từ file config. */
  public void loadServers() {
    servers.clear();
    if (!configFile.exists()) {
      return;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        String[] parts = line.split("\\|", 3);
        if (parts.length == 3) {
          try {
            String name = parts[0].trim();
            String host = parts[1].trim();
            int port = Integer.parseInt(parts[2].trim());
            servers.add(new ServerInfo(name, host, port));
          } catch (NumberFormatException ignored) {
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Error loading server config: " + e.getMessage());
    }
  }

  /** Ghi danh sách server ra file config. */
  public void saveServers() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
      writer.println("# Chat App Server List");
      writer.println("# Format: name|host|port");
      for (ServerInfo server : servers) {
        writer.println(server.getName() + "|" + server.getHost() + "|" + server.getPort());
      }
    } catch (IOException e) {
      System.err.println("Error saving server config: " + e.getMessage());
    }
  }
}
