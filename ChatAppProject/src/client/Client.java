package client;
import java.io.*;
import java.net.*;

import client.gui.DashBoardPanel;
import client.gui.MainFrame;
import common.net.MessageObject;
import common.net.MessageType;
public class Client {
  public static void main(String[] args) throws ClassNotFoundException {


    
    //Kết nối socket
    try {
      Socket s = new Socket("localhost",4321);
      //Khởi tạo giao diên
      new MainFrame(s);

      
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
      BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
      console.readLine();

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
