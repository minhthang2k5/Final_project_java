package server.gui;

import server.Server;
import server.handlers.ClientHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Giao diện quản lý Server: hiển thị config, đóng/mở server,
 * danh sách client đang kết nối, và log.
 */
public class ServerGUI extends JFrame implements Server.ServerListener {

  private final Server server;

  // Config panel
  private JTextField portField;
  private JLabel dbUrlLabel;
  private JLabel dbUserLabel;
  private JLabel statusLabel;
  private JButton toggleButton;

  // Client table
  private DefaultTableModel tableModel;
  private JTable clientTable;
  private JLabel clientCountLabel;

  // Log area
  private JTextArea logArea;

  public ServerGUI() {
    server = new Server();
    server.setListener(this);

    setTitle("Chat Server - Control Panel");
    setSize(750, 600);
    setMinimumSize(new Dimension(600, 450));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setLocationRelativeTo(null);

    // Icon
    java.net.URL iconUrl = getClass().getResource("/client/gui/asset/dialogue.png");
    if (iconUrl != null) {
      setIconImage(new ImageIcon(iconUrl).getImage());
    }

    setLayout(new BorderLayout(0, 0));
    getContentPane().setBackground(new Color(240, 242, 245));

    // ====== TOP: Config Panel ======
    JPanel configPanel = buildConfigPanel();
    add(configPanel, BorderLayout.NORTH);

    // ====== CENTER: Split between client table and log ======
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setResizeWeight(0.6);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setDividerSize(6);

    JPanel clientPanel = buildClientTablePanel();
    JPanel logPanel = buildLogPanel();
    splitPane.setTopComponent(clientPanel);
    splitPane.setBottomComponent(logPanel);
    add(splitPane, BorderLayout.CENTER);

    // ====== Window close ======
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        handleWindowClose();
      }
    });

    setVisible(true);
  }

  // =========================================================================
  // UI Builders
  // =========================================================================

  private JPanel buildConfigPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 220, 224)),
        BorderFactory.createEmptyBorder(14, 20, 14, 20)));

    // Left: Title + config fields
    JPanel leftSection = new JPanel();
    leftSection.setOpaque(false);
    leftSection.setLayout(new BoxLayout(leftSection, BoxLayout.Y_AXIS));

    JLabel titleLabel = new JLabel("Server Control Panel");
    titleLabel.setFont(new Font(null, Font.BOLD, 18));
    titleLabel.setForeground(new Color(30, 30, 30));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    leftSection.add(titleLabel);
    leftSection.add(Box.createVerticalStrut(10));

    // Config row
    JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    configRow.setOpaque(false);
    configRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    configRow.add(createConfigLabel("Port:"));
    portField = new JTextField("4321", 6);
    portField.setFont(new Font(null, Font.PLAIN, 13));
    portField.setPreferredSize(new Dimension(70, 28));
    configRow.add(portField);

    configRow.add(Box.createHorizontalStrut(8));
    configRow.add(createConfigLabel("DB:"));
    dbUrlLabel = new JLabel("jdbc:mysql://localhost:3306/chat_app");
    dbUrlLabel.setFont(new Font(null, Font.PLAIN, 12));
    dbUrlLabel.setForeground(new Color(100, 100, 100));
    configRow.add(dbUrlLabel);

    configRow.add(Box.createHorizontalStrut(8));
    configRow.add(createConfigLabel("User:"));
    dbUserLabel = new JLabel("root");
    dbUserLabel.setFont(new Font(null, Font.PLAIN, 12));
    dbUserLabel.setForeground(new Color(100, 100, 100));
    configRow.add(dbUserLabel);

    leftSection.add(configRow);
    panel.add(leftSection, BorderLayout.CENTER);

    // Right: Status + Toggle button
    JPanel rightSection = new JPanel();
    rightSection.setOpaque(false);
    rightSection.setLayout(new BoxLayout(rightSection, BoxLayout.Y_AXIS));

    statusLabel = new JLabel("● Stopped");
    statusLabel.setFont(new Font(null, Font.BOLD, 13));
    statusLabel.setForeground(new Color(200, 60, 60));
    statusLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    rightSection.add(statusLabel);
    rightSection.add(Box.createVerticalStrut(8));

    toggleButton = new JButton("Start Server");
    toggleButton.setFont(new Font(null, Font.BOLD, 13));
    toggleButton.setFocusable(false);
    toggleButton.setBackground(new Color(52, 168, 83));
    toggleButton.setForeground(Color.WHITE);
    toggleButton.setOpaque(true);
    toggleButton.setBorderPainted(false);
    toggleButton.setPreferredSize(new Dimension(130, 34));
    toggleButton.setMaximumSize(new Dimension(130, 34));
    toggleButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    toggleButton.addActionListener(e -> handleToggleServer());
    rightSection.add(toggleButton);

    panel.add(rightSection, BorderLayout.EAST);
    return panel;
  }

  private JPanel buildClientTablePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    // Header
    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
    header.setBackground(Color.WHITE);
    header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

    clientCountLabel = new JLabel("Connected Clients (0)");
    clientCountLabel.setFont(new Font(null, Font.BOLD, 14));
    clientCountLabel.setForeground(new Color(50, 50, 50));
    header.add(clientCountLabel);
    panel.add(header, BorderLayout.NORTH);

    // Table
    String[] columns = {"#", "Username", "IP Address", "Port", "Connected Since"};
    tableModel = new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    clientTable = new JTable(tableModel);
    clientTable.setFont(new Font(null, Font.PLAIN, 13));
    clientTable.setRowHeight(32);
    clientTable.setShowGrid(false);
    clientTable.setIntercellSpacing(new Dimension(0, 0));
    clientTable.getTableHeader().setFont(new Font(null, Font.BOLD, 12));
    clientTable.getTableHeader().setBackground(new Color(245, 245, 245));
    clientTable.getTableHeader().setForeground(new Color(80, 80, 80));
    clientTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
    clientTable.setSelectionBackground(new Color(232, 240, 254));
    clientTable.setSelectionForeground(Color.BLACK);

    // Column widths
    clientTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    clientTable.getColumnModel().getColumn(0).setMaxWidth(50);
    clientTable.getColumnModel().getColumn(1).setPreferredWidth(150);
    clientTable.getColumnModel().getColumn(2).setPreferredWidth(150);
    clientTable.getColumnModel().getColumn(3).setPreferredWidth(80);
    clientTable.getColumnModel().getColumn(4).setPreferredWidth(120);

    // Center align for # and Port columns
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    clientTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    clientTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

    JScrollPane scrollPane = new JScrollPane(clientTable);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getViewport().setBackground(Color.WHITE);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel buildLogPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);

    // Header
    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
    header.setBackground(Color.WHITE);
    header.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(230, 230, 230)));

    JLabel logTitle = new JLabel("Server Log");
    logTitle.setFont(new Font(null, Font.BOLD, 14));
    logTitle.setForeground(new Color(50, 50, 50));
    header.add(logTitle);

    JButton clearLogButton = new JButton("Clear");
    clearLogButton.setFont(new Font(null, Font.PLAIN, 11));
    clearLogButton.setFocusable(false);
    clearLogButton.setBackground(new Color(240, 240, 240));
    clearLogButton.setBorderPainted(false);
    clearLogButton.addActionListener(e -> logArea.setText(""));
    header.add(clearLogButton);
    panel.add(header, BorderLayout.NORTH);

    // Log text area
    logArea = new JTextArea();
    logArea.setEditable(false);
    logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
    logArea.setBackground(new Color(250, 250, 250));
    logArea.setForeground(new Color(60, 60, 60));
    logArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    logArea.setLineWrap(true);
    logArea.setWrapStyleWord(true);

    JScrollPane logScroll = new JScrollPane(logArea);
    logScroll.setBorder(BorderFactory.createEmptyBorder());
    panel.add(logScroll, BorderLayout.CENTER);

    return panel;
  }

  private static JLabel createConfigLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font(null, Font.BOLD, 12));
    label.setForeground(new Color(80, 80, 80));
    return label;
  }

  // =========================================================================
  // Event Handlers
  // =========================================================================

  private void handleToggleServer() {
    if (server.isRunning()) {
      int choice = JOptionPane.showConfirmDialog(this,
          "Are you sure you want to stop the server?\nAll connected clients will be disconnected.",
          "Stop Server", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (choice == JOptionPane.YES_OPTION) {
        server.stop();
      }
    } else {
      String portText = portField.getText().trim();
      int port;
      try {
        port = Integer.parseInt(portText);
        if (port < 1 || port > 65535) {
          throw new NumberFormatException();
        }
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid port number (1-65535)",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      server.start(port);
    }
  }

  private void handleWindowClose() {
    if (server.isRunning()) {
      int choice = JOptionPane.showConfirmDialog(this,
          "Server is still running. Stop and exit?",
          "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (choice != JOptionPane.YES_OPTION) {
        return;
      }
      server.stop();
    }
    dispose();
    System.exit(0);
  }

  // =========================================================================
  // Server.ServerListener implementation
  // =========================================================================

  @Override
  public void onClientConnected(ClientHandler handler) {
    refreshClientTable();
  }

  @Override
  public void onClientDisconnected(ClientHandler handler) {
    refreshClientTable();
  }

  @Override
  public void onLog(String message) {
    logArea.append(message + "\n");
    // Auto-scroll to bottom
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }

  @Override
  public void onStatusChanged(boolean running) {
    if (running) {
      statusLabel.setText("● Running");
      statusLabel.setForeground(new Color(52, 168, 83));
      toggleButton.setText("Stop Server");
      toggleButton.setBackground(new Color(200, 60, 60));
      portField.setEditable(false);
    } else {
      statusLabel.setText("● Stopped");
      statusLabel.setForeground(new Color(200, 60, 60));
      toggleButton.setText("Start Server");
      toggleButton.setBackground(new Color(52, 168, 83));
      portField.setEditable(true);
      refreshClientTable(); // Clear table
    }
  }

  // =========================================================================
  // Helpers
  // =========================================================================

  /** Cập nhật bảng client từ danh sách hiện tại của server. */
  private void refreshClientTable() {
    tableModel.setRowCount(0);
    int idx = 1;
    for (ClientHandler handler : server.getConnectedClients()) {
      String username = handler.getUsername();
      tableModel.addRow(new Object[]{
          idx++,
          username == null ? "(not logged in)" : username,
          handler.getClientIP(),
          handler.getClientPort(),
          handler.getConnectedTime()
      });
    }
    clientCountLabel.setText("Connected Clients (" + server.getConnectedClients().size() + ")");
  }
}
