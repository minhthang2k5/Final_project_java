package server;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.*;
import java.net.*;
import common.models.DBConnection;
import server.handlers.ClientHandler;
public class Server {
  public static void main(String[] args) {
    //Kết nối database
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

  
    //mở server socket
    try {
      ServerSocket s = new ServerSocket(4321);
      do {
        //Kết nối với client
        System.out.println("Waiting for users");
        Socket clientSocket = s.accept();
        System.out.println("Client connected: " + clientSocket.getInetAddress());

        Thread thread = new Thread(new ClientHandler(clientSocket));
        thread.start();


      } while (true);



    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
