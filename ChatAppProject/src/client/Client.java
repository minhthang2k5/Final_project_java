package client;
import java.io.*;
import java.net.*;

import javax.swing.JFrame;

import client.gui.MainFrame;
public class Client {
  public static void main(String[] args) throws ClassNotFoundException {


    
    //Kết nối socket
    try {
      Socket s = new Socket("localhost",4321);
      ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
      ObjectInputStream in = new ObjectInputStream(s.getInputStream());
      //Khởi tạo giao diên
      MainFrame mainFrame = new MainFrame(in, out);

      //Khởi tạo handler
        ServerHandler serverHandler = new ServerHandler(in, out, mainFrame,
          mainFrame.getClientAuthService());
      mainFrame.getLoginPanel().setServerHandler(serverHandler);
      mainFrame.getRegisterPanel().setServerHandler(serverHandler);
      mainFrame.getDashBoardPanel().setServerHandler(serverHandler);
      serverHandler.execute();
      
      //System.out.println("Client local port: " + s.getLocalPort());
      //System.out.println("Connected to: " + s.getInetAddress() + ":" + s.getPort());
      
      
      // MessageObject msg = new MessageObject(MessageType.SEND_SINGLE_CHAT_MESSAGE_REQUEST);
      // msg.setUsername("ilovetuyetnhungverymuch");
      // msg.setPassword("1aaaaaa");
      // msg.setConfirmPassword("1aaaaaa");
      // msg.setDisplayName("Thang");
      // msg.setEmail("minhthang@gmail.com");
      // ObjectInputStream in = new ObjectInputStream(s.getInputStream());
      // ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
      // out.writeObject(msg);
      // MessageObject response = (MessageObject) in.readObject();
      // System.out.println(response.getMessage());
      // msg.setSenderId(1);
      // msg.setConversationId(1);
      // msg.setChatMsg("Hello from client test");
      // ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
      // out.writeObject(msg);
      // out.flush();



      //Wait console
      // BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
      // console.readLine();

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
