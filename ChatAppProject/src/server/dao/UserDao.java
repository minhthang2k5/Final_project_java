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
  public boolean checkExistsUsername(String username) {
    String sql = "SELECT * FROM Users WHERE username = ?";
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setString(1, username);

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
  public boolean writeNewUser(String username,String password,
    String displayName,String email) {
    String sql = "INSERT INTO Users (username, password, display_name, email) VALUES (?, ?, ?, ?)";
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setString(1, username);
      pStatement.setString(2, password);
      pStatement.setString(3, displayName);
      pStatement.setString(4, email);
      int rows = pStatement.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }
}
