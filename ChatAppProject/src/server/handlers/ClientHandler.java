package server.handlers;

import java.net.*;

import common.net.MessageObject;
import common.net.MessageType;
import server.Server;
import server.Service.AuthService;

import java.io.*;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String username;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }


  @Override
  public void run() {
    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      while (true) {
        MessageObject request = (MessageObject) in.readObject();
        if (request == null) break;
        if (request.getType() == MessageType.LOGIN_REQUEST) {
          AuthService aService = new AuthService();
          MessageObject response = aService.authenticate(request);
          //System.out.println(request.getUsername()); //test
          out.writeObject(response);
          out.flush();
          if (response.isSuccess()) {
            this.username = request.getUsername();
            //Add user
            Server.onlineUsers.put(username,socket);
            System.out.println(username + " online");
          }
        }
        if (request.getType() == MessageType.REGISTER_REQUEST) {
          AuthService aService = new AuthService();
          MessageObject response = aService.checkConditionForRegisterAndAdd(request);
          System.out.println("Register from port: " + socket.getPort()); //test
          out.writeObject(response);
          out.flush();
        }
      }

    } catch (java.io.EOFException | java.net.SocketException e) {
      // client disconnected normally
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        if (in != null) in.close();
      } catch (IOException e) {}
      try {
        if (out != null) out.close();
      } catch (IOException e) {}
      try {
        if (socket != null && !socket.isClosed()) socket.close();
      } catch (IOException e) {}
      if (username != null) {
                Server.onlineUsers.remove(username);
                System.out.println(username + " offline");
      }
    }
  }
  
}
