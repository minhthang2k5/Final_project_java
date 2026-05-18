package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.models.DBConnection;

public class ChatDao {
  public boolean checkExitsSingleConversationBetweenTwoUsers(String senderUsername, String receiverUsername) {
    String sql = """
    SELECT *
    FROM participants AS p1 
    JOIN users AS u1 ON p1.user_id = u1.user_id 
    JOIN conversations AS c1 ON p1.conversation_id = c1.conversation_id
    WHERE c1.type = "single" 
      AND u1.username = ?
      AND p1.conversation_id IN (
          SELECT p2.conversation_id
          FROM participants AS p2 
          JOIN users AS u2 ON p2.user_id = u2.user_id
          WHERE u2.username = ?
      );
    """;
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
        pStatement.setString(1, senderUsername);
        pStatement.setString(2, receiverUsername);
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
  
  public boolean createConversationSingle(String senderUsername,String receiverUsername) {
    String insertConversationSql = "INSERT INTO conversations (name, type) VALUES (NULL, 'Single')";
    String insertParticipantSql = "INSERT INTO participants (user_id, conversation_id) VALUES (?, ?)";

    try (Connection con = DBConnection.getConnection()) {
      con.setAutoCommit(false);
      try {
        int conversationId = -1;
        try (PreparedStatement pStatement = con.prepareStatement(insertConversationSql,
            PreparedStatement.RETURN_GENERATED_KEYS)) {
          pStatement.executeUpdate();
          try (ResultSet keys = pStatement.getGeneratedKeys()) {
            if (keys.next()) {
              conversationId = keys.getInt(1);
            }
          }
        }

        if (conversationId <= 0) {
          con.rollback();
          return false;
        }

        Integer senderId = fetchUserId(con, senderUsername);
        Integer receiverId = fetchUserId(con, receiverUsername);
        if (senderId == null || receiverId == null) {
          con.rollback();
          return false;
        }

        try (PreparedStatement pStatement = con.prepareStatement(insertParticipantSql)) {
          pStatement.setInt(1, senderId);
          pStatement.setInt(2, conversationId);
          pStatement.executeUpdate();

          pStatement.setInt(1, receiverId);
          pStatement.setInt(2, conversationId);
          pStatement.executeUpdate();
        }

        con.commit();
        return true;
      } catch (SQLException e) {
        con.rollback();
        throw e;
      } finally {
        con.setAutoCommit(true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private Integer fetchUserId(Connection con, String username) throws SQLException {
    String sql = "SELECT user_id FROM users WHERE username = ?";
    try (PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setString(1, username);
      try (ResultSet rs = pStatement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }

    return null;
  }

  public boolean writeMessage(String senderUsername,int conversationID,String messagesChat) {
    String sql = """
      INSERT INTO messages (conversation_id, sender_id, type, content_text)
      SELECT ?, user_id, 'text', ?
      FROM users
      WHERE username = ?
      """;
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, conversationID);
      pStatement.setString(2, messagesChat);
      pStatement.setString(3, senderUsername);
      int rows = pStatement.executeUpdate();
      return rows > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }
}
