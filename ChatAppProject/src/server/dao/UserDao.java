package server.dao;
import java.sql.*;

import common.models.DBConnection;


public class UserDao {
  public boolean validateLogin(String username, String password) {
    String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setString(1, username);
      pStatement.setString(2, password);
      try (ResultSet rs = pStatement.executeQuery()) {
        if (rs.next()) {
        return true;
      }
        else {
        return false;
      }
    }

    } catch (SQLException e) {
      e.printStackTrace();
    }


    return false;
  }


}
