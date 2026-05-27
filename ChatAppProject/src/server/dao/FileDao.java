package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.models.DBConnection;

public class FileDao {

  /**
   * Ghi một dòng tin nhắn dạng file vào bảng messages.
   * Sau khi upload hoàn tất, file vật lý sẽ được đặt tên theo dạng:
   * "[message_id]_[nameFile]" và lưu vào thư mục server/data.
   *
   * @param conversationId  ID cuộc trò chuyện
   * @param senderId        ID người gửi
   * @param nameFile        Tên file gốc của người dùng
   * @param fileSize        Kích thước file (bytes)
   * @return message_id vừa được sinh tự động, hoặc -1 nếu thất bại
   */
  public int insertFileMessage(int conversationId, int senderId, String nameFile, long fileSize) {
    String sql = """
        INSERT INTO messages (conversation_id, sender_id, type, file_name, file_size)
        VALUES (?, ?, 'file', ?, ?)
        """;
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
      pStatement.setInt(1, conversationId);
      pStatement.setInt(2, senderId);
      pStatement.setString(3, nameFile);
      pStatement.setLong(4, fileSize);
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

  /**
   * Cập nhật file_name trong bảng messages sau khi upload hoàn tất.
   * Tên file được lưu lại là tên thực tế trên đĩa: "[messageId]_[tênGốc]"
   *
   * @param messageId   ID của tin nhắn cần cập nhật
   * @param newFileName Tên file mới (ví dụ: "123_baocao.pdf")
   * @return true nếu cập nhật thành công
   */
  public boolean updateFileName(int messageId, String newFileName) {
    String sql = "UPDATE messages SET file_name = ? WHERE message_id = ?";
    try (Connection con = DBConnection.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setString(1, newFileName);
      pStatement.setInt(2, messageId);
      return pStatement.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }
}
