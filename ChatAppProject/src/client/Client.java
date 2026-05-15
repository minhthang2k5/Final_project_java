package client;
import java.io.*;
import java.net.*;
public class Client {
  public static void main(String[] args) {

    //Kết nối socket
    try {
      Socket s = new Socket("localhost",4321);
      System.out.println(s.getPort());
      BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
      console.readLine();

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
