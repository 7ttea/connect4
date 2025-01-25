package game;

public class Main
{
    private static Game game;

    public static void main(String[] args) {
        game = new Game();
    }

    public static Game getGame() {return game;}
}
