package server;
import java.sql.Connection;
import java.sql.SQLException;

import common.models.DBConnection;
public class Server {
  public static void main(String[] args) {
    try {
      Connection conn = DBConnection.getConnection();
      if (conn != null) {
        System.out.println("Finish to connect database");
      }
      else {
        System.out.println("Fail to connect database");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
