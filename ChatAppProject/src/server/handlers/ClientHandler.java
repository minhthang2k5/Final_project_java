package server.handlers;

import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {
  private Socket socket;
  
  public ClientHandler(Socket socket) {
    this.socket = socket;
  }


  @Override
  public void run() {
    
  }
  
}
