package game;

import game.server.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;

public class GameUI
{
    private JFrame frame;
    JPanel main_container;
    private final Color player1 = new Color(166, 56, 190), player2 = new Color(80, 200, 200);
    private final DiscButton[][] discs = new DiscButton[Game.getRows()][Game.getColumns()];
    private final Color bg_color = new Color(69, 69, 69),
                  fg_color = new Color(220, 220, 220);
    private Mode mode;
    private Host host;

    public GameUI() {
        setupFrame();
        setupMainContainer();

        setupMenu();

        frame.add(main_container);
        frame.setVisible(true);
    }

    enum Mode {
        HOST,
        CLIENT,
        LOCAL
    }

    private void setupFrame() {
        String title = "connect4";
        Dimension size = new Dimension(Game.getColumns()*100, Game.getRows()*100);

        frame = new JFrame(title);
        frame.setBackground(bg_color);
        frame.setMaximumSize(new Dimension(1680, 1050));
        frame.setSize(size);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setIconImage();
    }

    private void setupMainContainer() {
        main_container = new JPanel();
        main_container.setBackground(new Color(62, 62, 62));
        main_container.setLayout(new BorderLayout());
    }

    private void setupGameBoard() {
        int horizontal_gap = 20, vertical_gap = 20;

        JPanel container = new JPanel();
        container.setBackground(bg_color);
        container.setLayout(new GridLayout(Game.getRows(), Game.getColumns(), horizontal_gap, vertical_gap));

        for (int row = 0; row < Game.getRows(); row++) {
            for (int column = 0; column < Game.getColumns(); column++) {

                DiscButton disc = new DiscButton(new Color(128, 128, 128));
                disc.setName(row + ":" + column);
                disc.addActionListener(e -> handleDiscPress(disc));

                discs[row][column] = disc;
                container.add(disc);
            }
        }
        main_container.add(container, BorderLayout.CENTER);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void setupMenuButton() {
        JButton menu_button = new JButton("menu");
        menu_button.setSize(50, 20);
        menu_button.setBackground(new Color(128, 128, 128));
        menu_button.setText("menu");
        menu_button.addActionListener(e -> {setupMenu();});
        main_container.add(menu_button, BorderLayout.NORTH);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void setupMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new FlowLayout());
        menu.setBackground(bg_color);
        main_container.removeAll();

        menu.add(hostButton());
        menu.add(localButton());
        menu.add(joinButton());
        main_container.add(menu, BorderLayout.CENTER);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void displayIPAndPort(String ip, String port) {
        JTextField text = new JTextField();

        text.setText(ip + "                                                                                 " +  port);
        text.setForeground(fg_color);
        text.setBackground(bg_color);
        text.setEditable(false);
        text.setHorizontalAlignment(0);
        main_container.add(text, BorderLayout.SOUTH);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private JButton localButton() {
        JButton local_button = new JButton();
        local_button.addActionListener(e ->{
            main_container.removeAll();
            GameLogic.setupGame_board();
            setupMenuButton();
            setupGameBoard();
            disableImpossibleDiscs();
            mode = Mode.LOCAL;
        });
        local_button.setText("local game");
        local_button.setBackground(new Color(70, 70, 70));
        local_button.setForeground(fg_color);
        local_button.setSize(200, 80);
        return local_button;
    }
    private JButton hostButton() {
        JButton host_button = new JButton();
        host_button.addActionListener(e ->{
            main_container.removeAll();
            GameLogic.setupGame_board();
            setupMenuButton();
            setupGameBoard();
            disableImpossibleDiscs();
            try {if (host == null) {host = new Host();}}
            catch (Exception ex) {throw new RuntimeException(ex);}

            try {displayIPAndPort(host.getAddress().toString().split("/")[1],
                                  String.valueOf(host.getPort()));}
            catch (UnknownHostException ex) {throw new RuntimeException(ex);}
            mode = Mode.HOST;
        });
        host_button.setText("host game");
        host_button.setBackground(new Color(70, 70, 70));
        host_button.setForeground(fg_color);
        host_button.setSize(200, 80);
        return host_button;
    }
    private JButton joinButton() {
        JButton join_button = new JButton();
        join_button.addActionListener(e ->{
            main_container.removeAll();

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.setBackground(bg_color);

            JTextField ip_text_field = new JTextField(20);
            ip_text_field.setText("IP_address");
            JTextField port_text_field = new JTextField(10);
            port_text_field.setText("port");

            panel.add(ip_text_field);
            panel.add(port_text_field);

            final JButton connect = new JButton("connect");
            panel.add(connect);
            connect.addActionListener(e1 -> {
                String ip = ip_text_field.getText();
                String port = port_text_field.getText();

                main_container.removeAll();

                GameLogic.setupGame_board();
                setupMenuButton();
                setupGameBoard();
                disableImpossibleDiscs();
                try {new Client(ip, Integer.parseInt(port));}
                catch (IOException ex) {throw new RuntimeException(ex);}
                mode = Mode.CLIENT;
            });
            main_container.add(panel);
            SwingUtilities.updateComponentTreeUI(frame);
        });
        join_button.setText("join game");
        join_button.setBackground(new Color(70, 70, 70));
        join_button.setForeground(fg_color);
        join_button.setSize(200, 80);
        return join_button;
    }

    private void handleDiscPress(DiscButton disc) {
        int row = Integer.parseInt(disc.getName().split(":")[0]);
        int column = Integer.parseInt(disc.getName().split(":")[1]);
        int player_value;


        if(GameLogic.getGame_board()[row][column] == GameLogic.player1
           || GameLogic.getGame_board()[row][column] == GameLogic.player2) {return;}


        switch (mode) {
            case Mode.HOST -> {
                if (!Host.host_turn) {return;}
                Host.host_turn = false;
                Host.setRow(row);
                Host.setColumn(column);
                player_value = GameLogic.player1;
            }
            case Mode.CLIENT -> {
                if (!Client.client_turn) {return;}
                Client.client_turn = false;
                Client.setRow(row);
                Client.setColumn(column);
                player_value = GameLogic.player2;}
            case Mode.LOCAL -> {player_value = GameLogic.getTurn();}
            default -> {player_value = GameLogic.empty;}
        }

        GameLogic.setGame_board(row, column, player_value);
        GameLogic.toggleTurn();
        renderGameBoard(row, column, disc);
        disableImpossibleDiscs();
        if (GameLogic.isGameOver()) {disableAllDiscs();}
    }

    private void renderGameBoard(int row, int column, DiscButton disc) {
        int[][] game_board = GameLogic.getGame_board();
        int value = game_board[row][column];

        switch (value) {
            case GameLogic.player1 -> disc.setBackground(player1);
            case GameLogic.player2 -> disc.setBackground(player2);
        }
    }

    public void renderGameBoard(int row, int column) {
        int[][] game_board = GameLogic.getGame_board();
        int value = game_board[row][column];

        DiscButton disc = discs[row][column];

        switch (value) {
            case GameLogic.player1 -> disc.setBackground(player1);
            case GameLogic.player2 -> disc.setBackground(player2);
        }

        disableImpossibleDiscs();
        if (GameLogic.isGameOver()) {disableAllDiscs();}
    }

    private void disableImpossibleDiscs() {
        int[][] game_board = GameLogic.getGame_board();
        for (int column = 0; column < Game.getColumns(); column++) {
            int previous_value = 2;
            for (int row = Game.getRows()-1; row >= 0; row--) {
                DiscButton disc = discs[row][column];
                disc.setEnabled(previous_value > 0);

                previous_value = game_board[row][column];
            }
        }
    }

    private void disableAllDiscs() {
        for (int column = 0; column < Game.getColumns(); column++) {
            for (int row = Game.getRows()-1; row >= 0; row--) {
                DiscButton disc = discs[row][column];
                disc.setEnabled(false);
            }
        }
    }

}
