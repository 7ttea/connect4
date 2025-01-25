package game;

public class Game
{
    private static final int rows = 6, columns = 7;
    private final static int length_for_win = 4;
    private GameUI game_ui;

    public static int getRows() {return rows;}

    public static int getColumns() {return columns;}

    public static int getLength_for_win() {return length_for_win;}

    public Game() {
        GameLogic.setupGame_board();
        game_ui = new GameUI();
    }

    public GameUI getGameUI() {return game_ui;}

}
