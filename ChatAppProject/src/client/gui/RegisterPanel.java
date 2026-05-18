package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import client.service.ClientAuthService;
import common.net.MessageObject;

public class RegisterPanel extends JPanel implements ActionListener {
  JButton signupButton;
  JButton signinButton;
  JLabel usernameLabel;
  JLabel displayNameLabel;
  JLabel emailLabel;
  JLabel passwordLabel;
  JLabel confirmPasswordLabel;
  JLabel statusLabel;
  JTextField usernameText;
  JTextField displayNameText;
  JTextField emailText;
  JPasswordField passwordText;
  JPasswordField confirmPasswordText;
  JPanel mainPanel;
  ClientAuthService clientAuthService;

  RegisterPanel(JPanel mainPanel, ClientAuthService clientAuthService) {
    this.mainPanel = mainPanel;
    this.clientAuthService = clientAuthService;

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

    JLabel signUpTitle = new JLabel("Sign up");
    signUpTitle.setFont(new Font(null, Font.BOLD, 32));
    signUpTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setOpaque(false);
    formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    GridBagConstraints formGbc = new GridBagConstraints();
    formGbc.gridx = 0;
    formGbc.anchor = GridBagConstraints.WEST;
    formGbc.fill = GridBagConstraints.HORIZONTAL;

    Dimension fieldSize = new Dimension(260, 30);

    usernameLabel = new JLabel("Username");
    usernameText = new JTextField();
    usernameLabel.setFont(new Font(null,Font.PLAIN,14));
    usernameText.setPreferredSize(fieldSize);
    usernameText.setMaximumSize(fieldSize);
    usernameText.setFont(new Font(null,Font.PLAIN,16));
    usernameLabel.setHorizontalAlignment(SwingConstants.LEFT);

    displayNameLabel = new JLabel("Display name");
    displayNameText = new JTextField();
    displayNameLabel.setFont(new Font(null,Font.PLAIN,14));
    displayNameText.setPreferredSize(fieldSize);
    displayNameText.setMaximumSize(fieldSize);
    displayNameText.setFont(new Font(null,Font.PLAIN,16));
    displayNameLabel.setHorizontalAlignment(SwingConstants.LEFT);

    emailLabel = new JLabel("Email");
    emailText = new JTextField();
    emailLabel.setFont(new Font(null,Font.PLAIN,14));
    emailText.setPreferredSize(fieldSize);
    emailText.setMaximumSize(fieldSize);
    emailText.setFont(new Font(null,Font.PLAIN,16));
    emailLabel.setHorizontalAlignment(SwingConstants.LEFT);

    passwordLabel = new JLabel("Password");
    passwordText = new JPasswordField();
    passwordLabel.setFont(new Font(null,Font.PLAIN,14));
    passwordText.setPreferredSize(fieldSize);
    passwordText.setMaximumSize(fieldSize);
    passwordText.setFont(new Font(null,Font.PLAIN,16));
    passwordLabel.setHorizontalAlignment(SwingConstants.LEFT);

    confirmPasswordLabel = new JLabel("Confirm password");
    confirmPasswordText = new JPasswordField();
    confirmPasswordLabel.setFont(new Font(null,Font.PLAIN,14));
    confirmPasswordText.setPreferredSize(fieldSize);
    confirmPasswordText.setMaximumSize(fieldSize);
    confirmPasswordText.setFont(new Font(null,Font.PLAIN,16));
    confirmPasswordLabel.setHorizontalAlignment(SwingConstants.LEFT);

    formGbc.gridy = 0;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(usernameLabel, formGbc);
    formGbc.gridy = 1;
    formGbc.insets = new Insets(0, 0, 12, 0);
    formPanel.add(usernameText, formGbc);

    formGbc.gridy = 2;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(displayNameLabel, formGbc);
    formGbc.gridy = 3;
    formGbc.insets = new Insets(0, 0, 12, 0);
    formPanel.add(displayNameText, formGbc);

    formGbc.gridy = 4;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(emailLabel, formGbc);
    formGbc.gridy = 5;
    formGbc.insets = new Insets(0, 0, 12, 0);
    formPanel.add(emailText, formGbc);

    formGbc.gridy = 6;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(passwordLabel, formGbc);
    formGbc.gridy = 7;
    formGbc.insets = new Insets(0, 0, 12, 0);
    formPanel.add(passwordText, formGbc);

    formGbc.gridy = 8;
    formGbc.insets = new Insets(0, 0, 6, 0);
    formPanel.add(confirmPasswordLabel, formGbc);
    formGbc.gridy = 9;
    formGbc.insets = new Insets(0, 0, 0, 0);
    formPanel.add(confirmPasswordText, formGbc);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    buttonPanel.setOpaque(false);

    signupButton = new JButton("sign-up");
    signupButton.setFocusable(false);
    signupButton.setPreferredSize(new Dimension(260, 32));
    signupButton.setBackground(new Color(0x63, 0x66, 0xF1));
    signupButton.setForeground(Color.WHITE);
    signupButton.setFont(new Font(null, Font.BOLD, 14));
    signupButton.setOpaque(true);
    signupButton.setContentAreaFilled(true);
    signupButton.setBorderPainted(false);
    buttonPanel.add(signupButton);

    JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
    footerPanel.setOpaque(false);
    JLabel footerLabel = new JLabel("Already have an account?");
    footerLabel.setFont(new Font(null, Font.PLAIN, 12));
    signinButton = new JButton("Sign in");
    signinButton.setFont(new Font(null, Font.PLAIN, 12));
    signinButton.setBorderPainted(false);
    signinButton.setContentAreaFilled(false);
    signinButton.setFocusPainted(false);
    signinButton.setForeground(new Color(65, 88, 208));
    footerPanel.add(footerLabel);
    footerPanel.add(signinButton);

    statusLabel = new JLabel(" ");
    statusLabel.setFont(new Font(null, Font.PLAIN, 12));
    statusLabel.setForeground(new Color(220, 53, 69));
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    subContent.add(signUpTitle);
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

    signupButton.addActionListener(this);
    signinButton.addActionListener(this);
  }

  public JButton getSignUpButton() {
    return signupButton;
  }

  public JButton getSignInButton() {
    return signinButton;
  }

  public String getUsernameInput() {
    return usernameText.getText();
  }

  public String getDisplayNameInput() {
    return displayNameText.getText();
  }

  public String getEmailInput() {
    return emailText.getText();
  }

  public String getPasswordInput() {
    return new String(passwordText.getPassword());
  }

  public String getConfirmPasswordInput() {
    return new String(confirmPasswordText.getPassword());
  }

  public void clearInput() {
    usernameText.setText("");
    displayNameText.setText("");
    emailText.setText("");
    passwordText.setText("");
    confirmPasswordText.setText("");
  }

  public void setStatusMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      statusLabel.setForeground(new Color(220, 53, 69));
      statusLabel.setText(" ");
      return;
    }
    if ("successful".equalsIgnoreCase(message.trim())) {
      statusLabel.setForeground(new Color(40, 167, 69));
    } else {
      statusLabel.setForeground(new Color(220, 53, 69));
    }
    statusLabel.setText(message);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // handled by controller
    if (e.getSource() == signinButton) {
      clearInput();
      CardLayout cl = (CardLayout) mainPanel.getLayout();
      cl.show(mainPanel, "loginPanel");
    }
    else if (e.getSource() == signupButton) {
      final String username = getUsernameInput();
      final String password = getPasswordInput();
      final String confirmPassword = getConfirmPasswordInput();
      final String displayName = getDisplayNameInput();
      final String email = getEmailInput();

      signupButton.setEnabled(false);
      SwingWorker<MessageObject, Void> registerWorker = new SwingWorker<MessageObject,Void>() {

        @Override
        protected MessageObject doInBackground() throws Exception {
          clientAuthService.sendRegisterInformation(username, password, confirmPassword, displayName, email);
          return clientAuthService.receiveRespond();

        }
        @Override
        protected void done() {
          try {
            MessageObject response = get();
            if (response != null && response.isSuccess()) {
              System.out.println("Successfully register");
              setStatusMessage("Successful");
            } else if (response != null) {
              System.out.println("Fail to sign up");
              setStatusMessage(response.getMessage());
            } else {
              setStatusMessage("Register failed");
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          } finally {
            signupButton.setEnabled(true);
          }
        }
      };
      registerWorker.execute();
      
      

    }
  }
}
