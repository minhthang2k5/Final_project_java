package client.gui;

import javax.swing.*;

import client.service.ClientAuthService;
import common.net.MessageObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginPanel extends JPanel implements ActionListener {
  JButton signinButton;
  JButton signupButton;
  JLabel usernameLabel;
  JLabel passwordLabel;
  JLabel statusLabel;
  JTextField usernameText;
  JPasswordField passwordText;
  JPanel mainPanel;
  ClientAuthService clientAuthService;
  LoginPanel(JPanel mainPanel,ClientAuthService clientAuthService) {
    //Khai báo
    this.clientAuthService = clientAuthService;


    //Thiết kế giao diện
    this.setBounds(0,0,900,700);
    this.setLayout(new BorderLayout());
    this.setBackground(new Color(240, 240, 240));

    JPanel centerPanel = new JPanel(new GridBagLayout());
    centerPanel.setOpaque(false);

    JPanel subContent = new JPanel();
    subContent.setBackground(Color.white);
    subContent.setOpaque(true);
    subContent.setLayout(new BoxLayout(subContent, BoxLayout.Y_AXIS));
    subContent.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

    JLabel signInTitle = new JLabel("Sign in");
    signInTitle.setFont(new Font(null, Font.BOLD, 32));
    signInTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setOpaque(false);
    formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    GridBagConstraints formGbc = new GridBagConstraints();
    formGbc.gridx = 0;
    formGbc.anchor = GridBagConstraints.WEST;
    formGbc.fill = GridBagConstraints.HORIZONTAL;

    Dimension fieldSize = new Dimension(260, 30);

    //username field
    usernameLabel = new JLabel("Username");
    usernameText = new JTextField();
    usernameLabel.setFont(new Font(null,Font.PLAIN,14));
    usernameText.setPreferredSize(fieldSize);
    usernameText.setMaximumSize(fieldSize);
    usernameText.setFont(new Font(null,Font.PLAIN,16));
    usernameLabel.setHorizontalAlignment(SwingConstants.LEFT);

    //password field
    passwordLabel = new JLabel("Password");
    passwordText = new JPasswordField();
    passwordLabel.setFont(new Font(null,Font.PLAIN,14));
    passwordText.setPreferredSize(fieldSize);
    passwordText.setMaximumSize(fieldSize);
    passwordText.setFont(new Font(null,Font.PLAIN,16));
    passwordLabel.setHorizontalAlignment(SwingConstants.LEFT);

    formGbc.gridy = 0;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(usernameLabel, formGbc);
    formGbc.gridy = 1;
    formGbc.insets = new Insets(0, 0, 12, 0);
    formPanel.add(usernameText, formGbc);
    formGbc.gridy = 2;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(passwordLabel, formGbc);
    formGbc.gridy = 3;
    formGbc.insets = new Insets(0, 0, 0, 0);
    formPanel.add(passwordText, formGbc);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    buttonPanel.setOpaque(false);

    //sign-in button
    signinButton = new JButton("sign-in");
    signinButton.setFocusable(false);
    signinButton.setPreferredSize(new Dimension(260, 32));
    signinButton.setBackground(new Color(0x63, 0x66, 0xF1));
    signinButton.setForeground(Color.WHITE);
    signinButton.setFont(new Font(null, Font.BOLD, 14));
    signinButton.setOpaque(true);
    signinButton.setContentAreaFilled(true);
    signinButton.setBorderPainted(false);
    buttonPanel.add(signinButton);

    JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
    footerPanel.setOpaque(false);
    JLabel footerLabel = new JLabel("Don't have an account?");
    footerLabel.setFont(new Font(null, Font.PLAIN, 12));
    signupButton = new JButton("Sign up");
    signupButton.setFont(new Font(null, Font.PLAIN, 12));
    signupButton.setBorderPainted(false);
    signupButton.setContentAreaFilled(false);
    signupButton.setFocusPainted(false);
    signupButton.setForeground(new Color(65, 88, 208));
    footerPanel.add(footerLabel);
    footerPanel.add(signupButton);

    statusLabel = new JLabel(" ");
    statusLabel.setFont(new Font(null, Font.PLAIN, 12));
    statusLabel.setForeground(new Color(220, 53, 69));
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    subContent.add(signInTitle);
    subContent.add(Box.createVerticalStrut(12));
    subContent.add(formPanel);
    subContent.add(Box.createVerticalStrut(16));
    subContent.add(buttonPanel);
    subContent.add(Box.createVerticalStrut(12));
    subContent.add(footerPanel);
    subContent.add(Box.createVerticalStrut(8));
    subContent.add(statusLabel);

    centerPanel.add(subContent);
    this.add(centerPanel,BorderLayout.CENTER);

    this.mainPanel = mainPanel;

    
    //Add event
    signinButton.addActionListener(this);
    signupButton.addActionListener(this);
    
    //In panel 
    CardLayout cl = (CardLayout) mainPanel.getLayout();
    cl.show(mainPanel, "loginPanel");
    
  }

  public JButton getSignInButton() {
    return signinButton;
  }
  public JButton getSignUpButton() {
    return signupButton;
  }
  public JLabel getUsernameLabel() {
    return usernameLabel;
  }
  public JLabel getPasswordLabel() {
    return passwordLabel;
  }
  public String getUsernameInput() {
    return usernameText.getText();
  }
  public String getPasswordInput() {
    return new String(passwordText.getPassword());
  }

  public void clearInput() {
    usernameText.setText("");
    passwordText.setText("");
  }

  public void setStatusMessage(String message, boolean isError) {
    statusLabel.setForeground(new Color(220, 53, 69));
    if (message == null || message.trim().isEmpty()) {
      statusLabel.setText(" ");
      return;
    }
    statusLabel.setText(message);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == signinButton) {
      String username = usernameText.getText();
      String password = passwordText.getText();
      //Xử lý request
      try {
        clientAuthService.sendAuthentication(username, password);
      } catch (IOException e1) {
        e1.printStackTrace();
      }


      //Xử lý response
      try {
        MessageObject response = clientAuthService.receiveRespond();
        if (response.isSuccess()) {
          System.out.println("Successfully login");
          setStatusMessage(response.getMessage(), true);
        }
        else {
          System.out.println("Fail to login");
          setStatusMessage(response.getMessage(), false);
        }



      } catch (ClassNotFoundException e1) {

        e1.printStackTrace();
      } catch (IOException e1) {

        e1.printStackTrace();
      }
    }
    else if (e.getSource() == signupButton) {
      //In panel 
      clearInput();
      CardLayout cl = (CardLayout) mainPanel.getLayout();
      cl.show(mainPanel, "registerPanel");
    }


  }
}
