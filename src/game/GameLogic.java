package game;

public class GameLogic
{
    private static int[][] game_board;
    private static int turn = 1;
    public final static int empty = 0, player1 = 1, player2 = 2;

    public static int[][] getGame_board() {return game_board;}

    public static void setupGame_board() {
        game_board = new int[Game.getRows()][Game.getColumns()];
        //fill game_board with zeros
        for (int row = 0; row < Game.getRows(); row++) {
            for (int column = 0; column < Game.getColumns(); column++) {
                game_board[row][column] = empty;
            }
        }
    }

    public static void setGame_board(int row, int column, int value) {game_board[row][column] = value;}

    public static int getTurn() {return turn;}

    public static void toggleTurn() {turn = (turn == 1)?  2 : 1;}

    public static boolean isGameOver() {
        //functions return 0 / player1 / player2
        return (connectedHorizontally() > 0
                || connectedVertically() > 0
                || connectedDiagonallyLeftToRight() > 0
                || connectedDiagonallyRightToLeft() > 0
                || boardIsFull());
    }

    private static int connectedHorizontally() {
        for (int row = Game.getRows()-1; row >= 0; row--) {
            int length_same_discs = 0;
            int previous_value = empty;

            for (int column = 0; column < Game.getColumns(); column++) {
                final int value = game_board[row][column];
                if (value == empty) {
                    length_same_discs = 0;
                    previous_value = empty;
                    continue;
                }

                length_same_discs = (value == previous_value)? length_same_discs+1 : 1;

                if (length_same_discs == Game.getLength_for_win()) {return value;}
                previous_value = value;
            }
        }
        return 0;
    }

    private static int connectedVertically() {
        for (int column = 0; column < Game.getColumns(); column++) {
            int length_same_discs = 0;
            int previous_value = 0;

            for (int row = Game.getRows()-1; row >= 0; row--) {
                final int value = game_board[row][column];
                if (value == empty) {break;}

                length_same_discs = (value == previous_value)? length_same_discs+1 : 1;

                if (length_same_discs == Game.getLength_for_win()) {return value;}
                previous_value = value;
            }
        }
        return 0;
    }

    private static int connectedDiagonallyLeftToRight() {
        for (int column = 0; column < Game.getColumns() - 1; column++) {
            for (int row = Game.getRows() - 1; row >= 0; row--) {
                int length_same_discs = 0;
                int previous_value = empty;
                for (int displacement = 0; row-displacement > 0
                                           && displacement+column < Game.getColumns(); displacement++) {
                    final int value = game_board[row-displacement][column+displacement];
                    if (value == empty) {
                        length_same_discs = 0;
                        previous_value = empty;
                        continue;
                    }

                    length_same_discs = (value == previous_value) ? length_same_discs + 1 : 1;

                    if (length_same_discs == Game.getLength_for_win()) {return value;}
                    previous_value = value;
                }
            }

        }
        return 0;
    }

    private static int connectedDiagonallyRightToLeft() {
        for (int column = Game.getColumns() - 1; column >= 0; column--) {
            for (int row = Game.getRows() - 1; row >= 0; row--) {
                int length_same_discs = 0;
                int previous_value = empty;

                for (int displacement = 0; row - displacement > 0
                                           && column - displacement >= 0; displacement++) {
                    final int value = game_board[row - displacement][column - displacement];
                    if (value == empty) {
                        length_same_discs = 0;
                        previous_value = empty;
                        continue;
                    }

                    length_same_discs = (value == previous_value) ? length_same_discs + 1 : 1;

                    if (length_same_discs == Game.getLength_for_win()) {
                        return value;
                    }
                    previous_value = value;
                }

            }
        }
        return 0;
    }

    private static boolean boardIsFull() {
        for (int[] row : game_board) {
            for (int value : row) {
                if (value == empty) {return false;}
            }
        }
        return true;
    }

}

