package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.models.DBConnection;
import common.models.GroupConversationInfo;
import common.models.MessageChat;
import common.models.SingleConversationInfo;

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
  
  public boolean createConversationSingle(int senderId, int receiverId, StringBuilder messageRes) {
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

        if (senderId <= 0 || receiverId <= 0) {
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
        messageRes.setLength(0);
        messageRes.append("user id does not exist");
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

  public int writeMessage(int senderId, int conversationID, String messagesChat) {
    String sql = """
      INSERT INTO messages (conversation_id, sender_id, type, content_text)
      VALUES (?, ?, 'text', ?)
      """;
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
      pStatement.setInt(1, conversationID);
      pStatement.setInt(2, senderId);
      pStatement.setString(3, messagesChat);
      int rows = pStatement.executeUpdate();
      if (rows > 0) {
        try (ResultSet keys = pStatement.getGeneratedKeys()) {
          if (keys.next()) {
            return keys.getInt(1);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return -1;
  }

  public String[] getUsernameInConversation(int conversationID) {
    String sql = """
      SELECT u.username
      FROM participants p
      JOIN users u ON p.user_id = u.user_id
      WHERE p.conversation_id = ?
      """;
    List<String> usernames = new ArrayList<>();
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, conversationID);
      try (ResultSet rs = pStatement.executeQuery()) {
        while (rs.next()) {
          usernames.add(rs.getString("username"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return usernames.toArray(new String[0]);
  }

  //Conservation_id, display name, user_id
  public ArrayList<SingleConversationInfo> getListSingleConversationById(int userId) {
    ArrayList<SingleConversationInfo> result = new ArrayList<>();
    if (userId <= 0) {
      return result;
    }

    String sql = """
      SELECT c.conversation_id, u.display_name, u.user_id
      FROM conversations c
      JOIN participants p_self ON p_self.conversation_id = c.conversation_id
      JOIN participants p_other ON p_other.conversation_id = c.conversation_id
      JOIN users u ON u.user_id = p_other.user_id
      WHERE c.type = 'Single'
        AND p_self.user_id = ?
        AND p_other.user_id <> p_self.user_id
      ORDER BY c.conversation_id
      """;

    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, userId);
      try (ResultSet rs = pStatement.executeQuery()) {
        while (rs.next()) {
          int conversationId = rs.getInt("conversation_id");
          String displayName = rs.getString("display_name");
          int otherUserId = rs.getInt("user_id");
          result.add(new SingleConversationInfo(conversationId, displayName, otherUserId));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return result;
  }

  public ArrayList<GroupConversationInfo> getListGroupConversationByUserId(int userId) {
    ArrayList<GroupConversationInfo> result = new ArrayList<>();
    if (userId <= 0) {
      return result;
    }

    String sql = """
      SELECT c.conversation_id, c.name, COUNT(p_all.user_id) AS member_count
      FROM conversations c
      JOIN participants p_self ON p_self.conversation_id = c.conversation_id
      JOIN participants p_all ON p_all.conversation_id = c.conversation_id
      WHERE c.type = 'Group'
        AND p_self.user_id = ?
      GROUP BY c.conversation_id, c.name
      ORDER BY c.conversation_id
      """;

    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, userId);
      try (ResultSet rs = pStatement.executeQuery()) {
        while (rs.next()) {
          int conversationId = rs.getInt("conversation_id");
          String groupName = rs.getString("name");
          int memberCount = rs.getInt("member_count");
          result.add(new GroupConversationInfo(conversationId, groupName, memberCount));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return result;
  }

  //Lấy tên user id của người còn lại trong cuộc trò truyên 1-1
  public int getTheOtherUserInSingleConversation(int conversationID, int userID) {
    String sql = """
      SELECT user_id 
      FROM participants 
      WHERE conversation_id = ? 
        AND user_id <> ?
      """;
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, conversationID);
      pStatement.setInt(2, userID);
      try (ResultSet rs = pStatement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("user_id");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public int createGroupConversation(ArrayList<Integer> listUserID, String nameGroup) {
    if (listUserID == null || listUserID.isEmpty()) {
      return -1;
    }

    String insertConversationSql = "INSERT INTO conversations (name, type) VALUES (?, 'Group')";
    String insertParticipantSql = "INSERT INTO participants (user_id, conversation_id) VALUES (?, ?)";

    try (Connection con = DBConnection.getConnection()) {
      con.setAutoCommit(false);
      try {
        int conversationId = -1;
        try (PreparedStatement pStatement = con.prepareStatement(insertConversationSql,
            PreparedStatement.RETURN_GENERATED_KEYS)) {
          pStatement.setString(1, nameGroup);
          pStatement.executeUpdate();
          try (ResultSet keys = pStatement.getGeneratedKeys()) {
            if (keys.next()) {
              conversationId = keys.getInt(1);
            }
          }
        }

        if (conversationId <= 0) {
          con.rollback();
          return -1;
        }

        try (PreparedStatement pStatement = con.prepareStatement(insertParticipantSql)) {
          for (Integer userId : listUserID) {
            if (userId == null || userId <= 0) {
              continue;
            }
            pStatement.setInt(1, userId);
            pStatement.setInt(2, conversationId);
            pStatement.addBatch();
          }
          pStatement.executeBatch();
        }

        con.commit();
        return conversationId;
      } catch (SQLException e) {
        con.rollback();
        throw e;
      } finally {
        con.setAutoCommit(true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }
  
  public String[] getListUserInGroupConversation(int conversationID) {
    String sql = """
      SELECT u.username
      FROM participants p
      JOIN users u ON p.user_id = u.user_id
      JOIN conversations c ON c.conversation_id = p.conversation_id
      WHERE p.conversation_id = ?
        AND c.type = 'Group'
      """;
    List<String> usernames = new ArrayList<>();
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, conversationID);
      try (ResultSet rs = pStatement.executeQuery()) {
        while (rs.next()) {
          usernames.add(rs.getString("username"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return usernames.toArray(new String[0]);
  }

  public ArrayList<MessageChat> getListMessageInConversation(int conversationID) {
    ArrayList<MessageChat> result = new ArrayList<>();
    if (conversationID <= 0) {
      return result;
    }

    String sql = """
      SELECT m.message_id, m.sender_id, m.type, m.content_text, m.file_name,
             u.display_name, u.username
      FROM messages m
      JOIN users u ON u.user_id = m.sender_id
      WHERE m.conversation_id = ?
      ORDER BY m.message_id
      """;

    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setInt(1, conversationID);
      try (ResultSet rs = pStatement.executeQuery()) {
        while (rs.next()) {
          int messageId = rs.getInt("message_id");
          int senderId = rs.getInt("sender_id");
          String type = rs.getString("type");
          String content = rs.getString("content_text");
          String filePath = rs.getString("file_name");
          String displayName = rs.getString("display_name");
          if (displayName == null || displayName.trim().isEmpty()) {
            displayName = rs.getString("username");
          }
          boolean isText = type == null || type.equalsIgnoreCase("text");
          MessageChat message = isText
              ? new MessageChat(messageId, senderId, content)
              : new MessageChat(messageId, senderId, content, filePath);
          if (!isText) {
            message.setType("file");
          }
          message.setSenderDisplayName(displayName);
          result.add(message);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return result;
  }
}
