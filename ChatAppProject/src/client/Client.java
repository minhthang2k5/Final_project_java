package client;
import java.io.*;
import java.net.*;

import client.gui.MainFrame;
import common.net.MessageObject;
import common.net.MessageType;
public class Client {
  public static void main(String[] args) {


    
    //Kết nối socket
    try {
      Socket s = new Socket("localhost",4321);
      //Khởi tạo giao diên
      new MainFrame(s);
      
      System.out.println(s.getPort());
      
      // MessageObject msg = new MessageObject(MessageType.LOGIN_REQUEST);
      // msg.setUsername("minhthang");
      // msg.setPassword("123456");
      // ObjectInputStream in = new ObjectInputStream(s.getInputStream());
      // ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
      // out.writeObject(msg);

      BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
      console.readLine();

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
