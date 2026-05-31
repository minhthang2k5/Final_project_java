package client.model;

import java.io.Serializable;

/**
 * Model lưu thông tin một server để kết nối.
 */
public class ServerInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String name;
  private String host;
  private int port;

  public ServerInfo(String name, String host, int port) {
    this.name = name;
    this.host = host;
    this.port = port;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public String toString() {
    return name + " (" + host + ":" + port + ")";
  }
}
