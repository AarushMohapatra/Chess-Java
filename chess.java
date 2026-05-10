import java.util.Scanner;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
public class chess {
    static boolean aimadness = false;
    static char[][] board = new char[8][8];
    static char[][] boardWhite = new char[8][8];
    static char[][] boardBlack = new char[8][8];
    static char[][] legalMoveWhite = new char[8][8];
    static int[][] legalMoveBlack = new int[8][8];
    static int[] legalPawnMovesWhite = new int[8];
    static int[] legalPawnMovesBlack = new int[8];
    static int lastPawnMove2Square;
    static int moves = 0;
    static int difficulty = 1;
    static Scanner sc = new Scanner(System.in);
    static char[] blackPiecesLib = new char[6];
    static char[] whitePiecesLib = new char[6];
    static int ai_Thinking = 500;
    static int move_r = 0, move_c = 0, oldmove_r = 0, oldmove_c = 0;
    static boolean isCoolConsole = false;
    static PrintStream out;
    static JButton[][] buttons = new JButton[8][8];
    static JPanel screen1 = new JPanel(new GridLayout(8, 8));
    static JPanel screen2 = new JPanel(new GridLayout(1, 1));
    static JLabel label = new JLabel("   Difficulty: ");
    static boolean pieceSelected = false;
    static int selectedR, selectedC;
    static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    static final Color DARK_SQUARE = new Color(181, 136, 99);
    static final Color HIGHLIGHT_MOVE = new Color(100, 200, 100);
    static final Color HIGHLIGHT_SELECTED = new Color(100, 149, 237);
    static BufferedImage icon;
    //static boolean[][] buttonActive = new boolean[8][8];
    static {
        try {
            out = new PrintStream(System.out, true, "UTF-8");
            isCoolConsole = true;
        } catch (UnsupportedEncodingException e) {
            out = System.out;
        }
    }
    public static void main(String[] args) throws IOException {
        isCoolConsole = false;
        init(); //instructions();renderBoard(5);//gameRuntime(2);
        //showBoard();
        try {
            File musicPath = new File("background_music.wav");
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                icon = ImageIO.read(new File("Chess_Icon.png"));
                SwingUtilities.invokeLater(() -> window());//remember future aarush for using indenter, fix arrow used to resolve errors after indenter used
                Timers();
            } else {
                System.out.println("cant find music file noooooooooo");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean hasLegalMove(int side, int r, int c) {
        boolean check = false;
        for (int new_c = 0; new_c < board.length; new_c++) {
            for (int new_r = 0; new_r < board.length; new_r++) {
                if (isLegalMove(side, r, c, new_r, new_c) == true) {
                    check = true;
                }
            }
        }
        return check;
    }
    public static void highlightMoves(int r, int c) {
        buttons[r][c].setBackground(HIGHLIGHT_SELECTED); // highlight the selected piece
        for (int new_c = 0; new_c < 8; new_c++) {
            for (int new_r = 0; new_r < 8; new_r++) {
                if (isLegalMove(1, r, c, new_r, new_c)) {
                    buttons[new_r][new_c].setBackground(HIGHLIGHT_MOVE);
                }
            }
        }
    }
    public static void handleClick(int r, int c) {
        if (!pieceSelected) {
            if (boardWhite[r][c] == '+') {
                return;
            }
            selectedR = r;
            selectedC = c;
            pieceSelected = true;
            highlightMoves(r, c);
        } else {
            refreshBoard();
            if (isLegalMove(1, selectedR, selectedC, r, c)) {
                makeMove(1, selectedR, selectedC, r, c);
                refreshBoard();
                if (isCheckMate(3)) {
                    JOptionPane.showMessageDialog(null, "Checkmate! White wins! Black forcefully challenges you again since he is an ai and they might rule the world soon..\n\n\nso yeah click any option to play again\nthe computer will now do more moves per turn");
                    init();
                    difficulty += 2;
                    refreshBoard();
                    return;
                }
                if (difficulty >= 1) {
                    for (int i = 0; i < difficulty; i++) {
                        int x = (int)(Math.random() * 2) + 1;
                        switch (x) {
                            case 1:
                                userInput(3);
                                break;

                            case 2:
                                userInput(6);
                                break;

                            case 3:
                                userInput(7);
                                break;

                            default:
                                break;
                        }
                        refreshBoard();
                    }
                } else {
                    difficulty = 1;
                    int x = (int)(Math.random() * 2) + 1;
                    switch (x) {
                        case 1:
                            userInput(3);
                            break;

                        case 2:
                            userInput(6);
                            break;

                        case 3:
                            userInput(7);
                            break;

                        default:
                            break;
                    }
                }

                refreshBoard();
                if (isCheckMate(1)) {
                    JOptionPane.showMessageDialog(null, "Checkmate! Black wins!\nBlack wants you to play again.\nSince you lost, you might as well try to redeem yourself\n...also the computer is gonna go easy on you unless you win");
                    init();
                    difficulty--;
                    refreshBoard();
                    return;
                }
            } else if (boardWhite[r][c] != '+') {
                selectedR = r;
                selectedC = c;
                pieceSelected = true;
                highlightMoves(r, c);
                return;
            }
            pieceSelected = false;
        }
    }
    public static void refreshBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                buttons[r][c].setText("");
                buttons[r][c].setToolTipText(null);
                buttons[r][c].setBackground((r + c) % 2 == 0 ? DARK_SQUARE : LIGHT_SQUARE);
                buttons[r][c].setOpaque(true);
                buttons[r][c].setBorderPainted(false);
                if (boardBlack[r][c] == 'K') {
                    buttons[r][c].setText("\u265A");
                    buttons[r][c].setToolTipText("Black's King");
                } else if (boardBlack[r][c] == 'Q') {
                    buttons[r][c].setText("\u265B");
                    buttons[r][c].setToolTipText("Black's Queen");
                } else if (boardBlack[r][c] == 'R') {
                    buttons[r][c].setText("\u265C");
                    buttons[r][c].setToolTipText("Black's Rook");
                } else if (boardBlack[r][c] == 'B') {
                    buttons[r][c].setText("\u265D");
                    buttons[r][c].setToolTipText("Black's Bishop");
                } else if (boardBlack[r][c] == 'n') {
                    buttons[r][c].setText("\u265E");
                    buttons[r][c].setToolTipText("Black's Knight");
                } else if (boardBlack[r][c] == 'P') {
                    buttons[r][c].setText("\u265F");
                    buttons[r][c].setToolTipText("Black's Pawn");
                } else if (boardWhite[r][c] == 'K') {
                    buttons[r][c].setText("\u2654");
                    buttons[r][c].setToolTipText("White's King");
                } else if (boardWhite[r][c] == 'Q') {
                    buttons[r][c].setText("\u2655");
                    buttons[r][c].setToolTipText("White's Queen");
                } else if (boardWhite[r][c] == 'R') {
                    buttons[r][c].setText("\u2656");
                    buttons[r][c].setToolTipText("White's Rook");
                } else if (boardWhite[r][c] == 'B') {
                    buttons[r][c].setText("\u2657");
                    buttons[r][c].setToolTipText("White's Bishop");
                } else if (boardWhite[r][c] == 'n') {
                    buttons[r][c].setText("\u2658");
                    buttons[r][c].setToolTipText("White's Knight");
                } else if (boardWhite[r][c] == 'P') {
                    buttons[r][c].setText("\u2659");
                    buttons[r][c].setToolTipText("White's Pawn");
                } else {
                    buttons[r][c].setToolTipText("Empty square full of doom and despair-");
                }
            }
        }
    }
    public static void init() {
        moves = 0;
        Time = 300;
        for (int c = 0; c < board.length; c++) {
            for (int r = 0; r < board[c].length; r++) {
                board[r][c] = '+';
            }
        }
        for (int c = 0; c < boardBlack.length; c++) {
            for (int r = 0; r < boardBlack[c].length; r++) {
                boardBlack[r][c] = '+';
            }
        }
        for (int c = 0; c < boardWhite.length; c++) {
            for (int r = 0; r < boardWhite[c].length; r++) {
                boardWhite[r][c] = '+';
            }
        }
        int i;
        for (i = 0; i < 8; i++) {
            board[i][1] = 'P';
            boardWhite[i][1] = 'P';
            legalPawnMovesWhite[i] = 0;
        }
        for (i = 0; i < 8; i++) {
            board[i][6] = 'P';
            boardBlack[i][6] = 'P';
            legalPawnMovesBlack[i] = 0;
        }
        board[0][0] = 'R';
        board[1][0] = 'n';
        board[2][0] = 'B';
        board[3][0] = 'Q';
        board[4][0] = 'K';
        board[5][0] = 'B';
        board[6][0] = 'n';
        board[7][0] = 'R';
        board[0][7] = 'R';
        board[1][7] = 'n';
        board[2][7] = 'B';
        board[3][7] = 'Q';
        board[4][7] = 'K';
        board[5][7] = 'B';
        board[6][7] = 'n';
        board[7][7] = 'R';
        boardWhite[0][0] = 'R';
        boardWhite[1][0] = 'n';
        boardWhite[2][0] = 'B';
        boardWhite[3][0] = 'Q';
        boardWhite[4][0] = 'K';
        boardWhite[5][0] = 'B';
        boardWhite[6][0] = 'n';
        boardWhite[7][0] = 'R';
        boardBlack[0][7] = 'R';
        boardBlack[1][7] = 'n';
        boardBlack[2][7] = 'B';
        boardBlack[3][7] = 'Q';
        boardBlack[4][7] = 'K';
        boardBlack[5][7] = 'B';
        boardBlack[6][7] = 'n';
        boardBlack[7][7] = 'R';
        whitePiecesLib[0] = '\u2654';
        whitePiecesLib[1] = '\u2655';
        whitePiecesLib[2] = '\u2656';
        whitePiecesLib[3] = '\u2657';
        whitePiecesLib[4] = '\u2658';
        whitePiecesLib[5] = '\u2659';
        blackPiecesLib[0] = '\u265A';
        blackPiecesLib[1] = '\u265B';
        blackPiecesLib[2] = '\u265C';
        blackPiecesLib[3] = '\u265D';
        blackPiecesLib[4] = '\u265E';
        blackPiecesLib[5] = '\u265F';
    }
    public static void initUI() {
        for (int c = 7; c > -1; c--) {
            for (int r = 7; r > -1; r--) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(70, 70));
                button.setFont(new Font("Serif", Font.PLAIN, 35));
                final int row = r;
                final int col = c;
                if (aimadness != true) {
                    button.addActionListener(e -> handleClick(row, col));
                }
                buttons[r][c] = button;
                screen1.add(button);
            }
        }
        if (aimadness != true) {
            label.setPreferredSize(new Dimension(560, 25));
            label.setFont(new Font("Script", Font.PLAIN, 18));
            screen2.add(label);
            screen2.setBackground(DARK_SQUARE);
            label.setForeground(LIGHT_SQUARE);
        } else {
            JButton button = new JButton("Click me to see ai play");
            button.setPreferredSize(new Dimension(560, 25));
            button.setFont(new Font("Script", Font.PLAIN, 18));
            button.addActionListener(e -> userInput(5));
            button.addActionListener(e -> userInput(6));
            button.addActionListener(e -> refreshBoard());
            screen2.add(button);
            screen2.setBackground(DARK_SQUARE);
            button.setBackground(DARK_SQUARE);
            button.setForeground(LIGHT_SQUARE);
        }
        refreshBoard();
    }
    public static void window() {
        initUI();
        //int x = 2345, y = -150; //this is for if monitor is connected
        int x = 250, y=150; //this is for if monitor isnt connected, adjust values for your screen size
        JFrame frame = new JFrame("Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLocation(x, y);
        //frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setShape(new RoundRectangle2D.Double(0, 0, 560, 560, 50, 50));
        frame.setIconImage(icon);
        frame.add(screen1);
        frame.pack();
        frame.setVisible(true);
        JFrame frame2 = new JFrame("e");
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setLocationRelativeTo(null);
        frame2.setResizable(false);
        frame2.setLocation(x, y + 590);
        frame2.setAlwaysOnTop(true);
        frame2.setUndecorated(true);
        frame2.setShape(new RoundRectangle2D.Double(0, 0, 560, 25, 25, 25));
        frame2.setIconImage(icon);
        frame2.add(screen2);
        frame2.pack();
        frame2.setVisible(true);
        JOptionPane.showMessageDialog(screen1, "Welcome to Chess! You have one and only one goal in chess.\nWin against the computer at chess.\nThere is one catch though..the computer gets to move more times every win you get.\nGood luck... you might need it");
    }
    static int Time = 300;
    public static void Timers() {
        Timer timer = new Timer(1000, e -> {
            Time--;
            label.setText("  Computer makes " + difficulty + " moves per turn | Moves: " + moves + " | Time: " + Time);
        });
        timer.start();
    }
    public static void showBoard() {
        String coolBoard = "";
        coolBoard += ("here is the board:\n-------------black--------------------" + "\n");
        for (int c = 7; c > -1; c--) {
            //coolBoard+=(c+1);
            for (int r = 7; r > -1; r--) {
                if (r != 7) {
                    coolBoard += ("|" + '\t');
                }
                if (board[r][c] == '+') {
                    coolBoard += ("\u3000");
                    continue;
                }
                if (boardBlack[r][c] == 'K') {
                    coolBoard += ("\u265A");
                    continue;
                }
                if (boardBlack[r][c] == 'Q') {
                    coolBoard += ("\u265B");
                    continue;
                }
                if (boardBlack[r][c] == 'R') {
                    coolBoard += ("\u265C");
                    continue;
                }
                if (boardBlack[r][c] == 'B') {
                    coolBoard += ("\u265D");
                    continue;
                }
                if (boardBlack[r][c] == 'n') {
                    coolBoard += ("\u265E");
                    continue;
                }
                if (boardBlack[r][c] == 'P') {
                    coolBoard += ("\u265F");
                    continue;
                }
                if (boardWhite[r][c] == 'K') {
                    coolBoard += ("\u2654");
                    continue;
                }
                if (boardWhite[r][c] == 'Q') {
                    coolBoard += ("\u2655");
                    continue;
                }
                if (boardWhite[r][c] == 'R') {
                    coolBoard += ("\u2656");
                    continue;
                }
                if (boardWhite[r][c] == 'B') {
                    coolBoard += ("\u2657");
                    continue;
                }
                if (boardWhite[r][c] == 'n') {
                    coolBoard += ("\u2658");
                    continue;
                }
                if (boardWhite[r][c] == 'P') {
                    coolBoard += ("\u2659");
                    continue;
                }
            }
            coolBoard += (" ||" + (c + 1) + "\n");
        }
        coolBoard += ("____________________________________________________________________" + "\n");
        for (int r = 7; r > -1; r--) {
            if (r != 7) {
                coolBoard += ('\t');
            }
            coolBoard += ("|" + ((char)((int)
                'a' + r)) + "|");
        }
        coolBoard += ("\n---------------white------------------" + "\n");
        JOptionPane.showInputDialog(null, coolBoard);
    }
    public static void gameRuntime(int mode) throws java.lang.InterruptedException {
        if (mode == 1) {
            while (true) {
                out.println("white's turn:");
                userInput(5);
                if (isCheckMate(3)) {
                    out.println("White wins!");
                    break;
                }
                if (isCheckMate(4)) {
                    out.println("Black wins!");
                    break;
                }
                out.println("black's turn:");
                userInput(6);
                if (isCheckMate(4)) {
                    out.println("Black wins!");
                    break;
                }
                if (isCheckMate(3)) {
                    out.println("White wins!");
                    break;
                }
                renderBoard(4);
                Thread.sleep(500);
                out.print("\f");
            }
        } else if (mode == 3) {
            while (true) {
                renderBoard(3);
                out.println("white's turn:");
                userInput(1);
                if (isCheckMate(2)) {
                    out.println("White wins!");
                    break;
                }
                out.print("\f");
                renderBoard(4);
                out.println("black's turn:");
                userInput(2);
                if (isCheckMate(1)) {
                    out.println("White wins!");
                    break;
                }
                out.print("\f");
            }
        } else if (mode == 2) {
            while (true) {
                renderBoard(5);
                out.println("white's turn:");
                userInput(1);
                if (isCheckMate(3)) {
                    out.println("White wins!");
                    break;
                }
                out.print("\f");
                out.println("black's turn:");
                userInput(6);
                if (isCheckMate(1)) {
                    out.println("White wins!");
                    break;
                }
                out.print("\f");
            }
        } else {
            out.println("invaild gamemode");
        }
        renderBoard(4);
    }
    public static void renderBoard(int mode) {
        switch (mode) {
            case 1:
                out.println("here is the board:\n-------------black--------------------");
                for (int c = 7; c > -1; c--) {
                    for (int r = 7; r > -1; r--) {
                        out.print(board[r][c] + "\t");
                    }
                    out.println();
                }
                out.println("---------------white------------------");
                break;
            case 2:
                out.println("here is the board:\n-------------white--------------------");
                for (int c = 0; c < board.length; c++) {
                    for (int r = 0; r < board[c].length; r++) {
                        out.print(board[r][c] + "\t");
                    }
                    out.println();
                }
                out.println("---------------black------------------");
                break;

            case 3:
                if (isCoolConsole == true) {
                    out.println("here is the board:\n-------------black--------------------");
                    for (int c = 7; c > -1; c--) {
                        //out.print(c+1);
                        for (int r = 7; r > -1; r--) {
                            if (r != 7) {
                                out.print("|" + '\t');
                            }
                            if (board[r][c] == '+') {
                                out.print(' ' + "\u25A0");
                                continue;
                            }
                            if (boardBlack[r][c] == 'K') {
                                out.print("\u265A");
                                continue;
                            }
                            if (boardBlack[r][c] == 'Q') {
                                out.print("\u265B");
                                continue;
                            }
                            if (boardBlack[r][c] == 'R') {
                                out.print("\u265C");
                                continue;
                            }
                            if (boardBlack[r][c] == 'B') {
                                out.print("\u265D");
                                continue;
                            }
                            if (boardBlack[r][c] == 'n') {
                                out.print("\u265E");
                                continue;
                            }
                            if (boardBlack[r][c] == 'P') {
                                out.print("\u265F");
                                continue;
                            }
                            if (boardWhite[r][c] == 'K') {
                                out.print("\u2654");
                                continue;
                            }
                            if (boardWhite[r][c] == 'Q') {
                                out.print("\u2655");
                                continue;
                            }
                            if (boardWhite[r][c] == 'R') {
                                out.print("\u2656");
                                continue;
                            }
                            if (boardWhite[r][c] == 'B') {
                                out.print("\u2657");
                                continue;
                            }
                            if (boardWhite[r][c] == 'n') {
                                out.print("\u2658");
                                continue;
                            }
                            if (boardWhite[r][c] == 'P') {
                                out.print("\u2659");
                                continue;
                            }
                        }
                        out.println(" ||" + (c + 1));
                    }
                    out.println("____________________________________________________________________");
                    for (int r = 7; r > -1; r--) {
                        if (r != 7) {
                            out.print('\t');
                        }
                        out.print("|" + ((char)((int)
                            'a' + r)) + "|");
                    }
                    out.println("\n---------------white------------------");
                } else {
                    renderBoard(1);
                }
                break;
            case 4:
                if (isCoolConsole == true) {
                    out.println("here is the board:\n-------------white--------------------");
                    for (int c = 0; c < board.length; c++) {
                        //out.print(c+1);
                        for (int r = 0; r < board.length; r++) {
                            out.print("|" + '\t');
                            if (board[r][c] == '+') {
                                out.print(' ' + "\u25A0");
                                continue;
                            }
                            if (boardBlack[r][c] == 'K') {
                                out.print("\u265A");
                                continue;
                            }
                            if (boardBlack[r][c] == 'Q') {
                                out.print("\u265B");
                                continue;
                            }
                            if (boardBlack[r][c] == 'R') {
                                out.print("\u265C");
                                continue;
                            }
                            if (boardBlack[r][c] == 'B') {
                                out.print("\u265D");
                                continue;
                            }
                            if (boardBlack[r][c] == 'n') {
                                out.print("\u265E");
                                continue;
                            }
                            if (boardBlack[r][c] == 'P') {
                                out.print("\u265F");
                                continue;
                            }
                            if (boardWhite[r][c] == 'K') {
                                out.print("\u2654");
                                continue;
                            }
                            if (boardWhite[r][c] == 'Q') {
                                out.print("\u2655");
                                continue;
                            }
                            if (boardWhite[r][c] == 'R') {
                                out.print("\u2656");
                                continue;
                            }
                            if (boardWhite[r][c] == 'B') {
                                out.print("\u2657");
                                continue;
                            }
                            if (boardWhite[r][c] == 'n') {
                                out.print("\u2658");
                                continue;
                            }
                            if (boardWhite[r][c] == 'P') {
                                out.print("\u2659");
                                continue;
                            }
                        }
                        out.println();
                    }
                    out.println("---------------black------------------");
                } else {
                    renderBoard(1);
                }
                break;
            case 5:
                if (isCoolConsole == true) {
                    out.println("here is the board:\n-------------black--------------------");
                    for (int c = 7; c > -1; c--) {
                        for (int r = 7; r > -1; r--) {
                            char piece;
                            if (board[r][c] == '+') {
                                piece = ('\u3000');
                            } else if (boardBlack[r][c] == 'K') {
                                piece = ('\u265A');
                            } else if (boardBlack[r][c] == 'Q') {
                                piece = ('\u265B');
                            } else if (boardBlack[r][c] == 'R') {
                                piece = ('\u265C');
                            } else if (boardBlack[r][c] == 'B') {
                                piece = ('\u265D');
                            } else if (boardBlack[r][c] == 'n') {
                                piece = ('\u265E');
                            } else if (boardBlack[r][c] == 'P') {
                                piece = ('\u265F');
                            } else if (boardWhite[r][c] == 'K') {
                                piece = ('\u2654');
                            } else if (boardWhite[r][c] == 'Q') {
                                piece = ('\u2655');
                            } else if (boardWhite[r][c] == 'R') {
                                piece = ('\u2656');
                            } else if (boardWhite[r][c] == 'B') {
                                piece = ('\u2657');
                            } else if (boardWhite[r][c] == 'n') {
                                piece = ('\u2658');
                            } else if (boardWhite[r][c] == 'P') {
                                piece = ('\u2659');
                            } else {
                                piece = '\u2588';
                            }
                            out.print("\u001B[4m|" + piece + "|\u001B[0m");
                            //out.print(piece+"\u0332");
                            // String ee = "\u3000"+piece;
                            // String nfc = Normalizer.normalize("a\u265F", Normalizer.Form.NFC);
                            // out.print("|"+nfc+"|");
                        }
                        out.println("|" + (c + 1));
                    }
                    for (int r = 7; r > -1; r--) {
                        if (r != 7) {
                            out.print('\u2003');
                        }
                        out.print("|" + ((char)((int)
                            'a' + r)) + "|");
                    }
                    out.println("\n---------------white------------------");
                } else {
                    renderBoard(1);
                }
                break;
            default:
                break;
        }
    }
    public static boolean isCheckMate(int side) {
        if (side == 1 || side == 4) {
            int KingX = -1;
            int KingY = -1;
            for (int c = 0; c < boardWhite.length; c++) {
                for (int r = 0; r < boardWhite[c].length; r++) {
                    if (boardWhite[r][c] == 'K') {
                        KingX = r;
                        KingY = c;
                    }
                }
            }
            if (KingX == -1 || KingY == -1) {
                return true;
            } else {
                return false;
            }
        } else if (side == 2 || side == 3) {
            int KingX = -1;
            int KingY = -1;
            for (int c = 0; c < boardBlack.length; c++) {
                for (int r = 0; r < boardBlack[c].length; r++) {
                    if (boardBlack[r][c] == 'K') {
                        KingX = r;
                        KingY = c;
                    }
                }
            }
            if (KingX == -1 || KingY == -1) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    public static boolean userInput(int side) {
        if (side == 1) {
            out.println("what piece you want to move");
            String move = sc.next();
            if (move.length() == 2) {
                int r = move.charAt(0) - 'a';
                int c = move.charAt(1) - '1';
                if (r < 0 || r > 7 || c < 0 || c > 7 || boardWhite[r][c] == '+') {
                    out.println("no");
                    return userInput(side);
                } else {
                    out.println("where the piece should go");
                    String move2 = sc.next();
                    if (move2.length() == 2) {
                        int new_r = move2.charAt(0) - 'a';
                        int new_c = move2.charAt(1) - '1';
                        if (new_r < 0 || new_r > 7 || new_c < 0 || new_c > 7) {
                            out.println("no");
                            return userInput(side);
                        }
                        if (isLegalMove(side, r, c, new_r, new_c)) {
                            out.println("ok");
                            makeMove(side, r, c, new_r, new_c);
                            return true;
                        } else {
                            out.println("not legal");
                            return userInput(side);
                        }
                    } else {
                        out.println("no");
                        return userInput(side);
                    }
                }
            } else {
                out.println("no");
                return userInput(side);
            }
        } else if (side == 2) {
            out.println("what piece you want to move");
            String move = sc.next();
            if (move.length() == 2) {
                int r = move.charAt(0) - 'a';
                int c = move.charAt(1) - '1';
                if (r < 0 || r > 7 || c < 0 || c > 7 || boardBlack[r][c] == '+') {
                    out.println("no");
                    return userInput(side);
                } else {
                    out.println("where the piece should go");
                    String move2 = sc.next();
                    if (move2.length() == 2) {
                        int new_r = move2.charAt(0) - 'a';
                        int new_c = move2.charAt(1) - '1';
                        if (new_r < 0 || new_r > 7 || new_c < 0 || new_c > 7) {
                            out.println("no");
                            return userInput(side);
                        }
                        if (isLegalMove(side, r, c, new_r, new_c)) {
                            out.println("ok");
                            makeMove(side, r, c, new_r, new_c);
                            return true;
                        } else {
                            out.println("not legal");
                            return userInput(side);
                        }
                    } else {
                        out.println("no");
                        return userInput(side);
                    }
                }
            } else {
                out.println("no");
                return userInput(side);
            }
        } else if (side == 3) {
            out.print(".");
            int r = (int)(Math.random() * 8);
            int c = (int)(Math.random() * 8);
            if (r < 0 || r > 7 || c < 0 || c > 7 || boardBlack[r][c] == '+') {
                return userInput(side);
            } else {
                int new_r = (int)(Math.random() * 8);
                int new_c = (int)(Math.random() * 8);
                if (new_r < 0 || new_r > 7 || new_c < 0 || new_c > 7) {
                    return userInput(side);
                }
                if (isLegalMove(side, r, c, new_r, new_c)) {
                    if (makeMove(side, r, c, new_r, new_c) == true) {
                        return true;
                    } else {
                        return userInput(side);
                    }
                } else {
                    return userInput(side);
                }
            }
        } else if (side == 4) {
            out.print(".");
            int r = (int)(Math.random() * 8);
            int c = (int)(Math.random() * 8);
            if (r < 0 || r > 7 || c < 0 || c > 7 || boardWhite[r][c] == '+') {
                return userInput(side);
            } else {
                int new_r = (int)(Math.random() * 8);
                int new_c = (int)(Math.random() * 8);
                if (new_r < 0 || new_r > 7 || new_c < 0 || new_c > 7) {
                    return userInput(side);
                }
                if (isLegalMove(side, r, c, new_r, new_c)) {
                    makeMove(side, r, c, new_r, new_c);
                    return true;
                } else {
                    return userInput(side);
                }
            }
        } else if (side == 5) {
            int[] movesetX = new int[ai_Thinking];
            int[] movesetY = new int[ai_Thinking];
            int[] oldmovesetX = new int[ai_Thinking];
            int[] oldmovesetY = new int[ai_Thinking];
            int[] bestChoice = new int[ai_Thinking];
            for (int i = 0; i < movesetX.length; i++) {
                aiThinking(4);
                //System.out.print("!");
                movesetX[i] = move_r;
                movesetY[i] = move_c;
                oldmovesetX[i] = oldmove_r;
                oldmovesetY[i] = oldmove_c;
            }
            for (int i = 0; i < movesetX.length; i++) {
                if (isLegalMove(4, oldmovesetX[i], oldmovesetY[i], movesetX[i], movesetY[i]) == true) {
                    if (pieceValue(movesetX[i], movesetY[i]) == 99999) {
                        bestChoice[i] = 999999;
                        break;
                    }
                    bestChoice[i] += pieceValue(movesetX[i], movesetY[i]);
                } else {
                    bestChoice[i] = -1;
                }

            }
            int MostChosen = 0;
            int PointsWorth = 0;
            for (int i = 0; i < movesetX.length; i++) {
                if (PointsWorth < bestChoice[i]) {
                    MostChosen = i;
                    PointsWorth = bestChoice[i];
                }
            }
            if (isLegalMove(4, oldmovesetX[MostChosen], oldmovesetY[MostChosen], movesetX[MostChosen], movesetY[MostChosen])) {
                makeMove(4, oldmovesetX[MostChosen], oldmovesetY[MostChosen], movesetX[MostChosen], movesetY[MostChosen]);
                return true;
            } else {
                return userInput(side);
            }
        } else if (side == 6) {
            int[] movesetX = new int[ai_Thinking];
            int[] movesetY = new int[ai_Thinking];
            int[] oldmovesetX = new int[ai_Thinking];
            int[] oldmovesetY = new int[ai_Thinking];
            int[] bestChoice = new int[ai_Thinking];
            for (int i = 0; i < movesetX.length; i++) {
                aiThinking(3);
                //System.out.print("!");
                movesetX[i] = move_r;
                movesetY[i] = move_c;
                oldmovesetX[i] = oldmove_r;
                oldmovesetY[i] = oldmove_c;
            }
            for (int i = 0; i < movesetX.length; i++) {
                if (isLegalMove(3, oldmovesetX[i], oldmovesetY[i], movesetX[i], movesetY[i]) == true) {
                    if (pieceValue(movesetX[i], movesetY[i]) == 99999) {
                        bestChoice[i] = 999999;
                        break;
                    }
                    bestChoice[i] += pieceValue(movesetX[i], movesetY[i]);
                } else {
                    bestChoice[i] = -1;
                }

            }
            int MostChosen = 0;
            int PointsWorth = 0;
            for (int i = 0; i < movesetX.length; i++) {
                if (PointsWorth < bestChoice[i]) {
                    MostChosen = i;
                    PointsWorth = bestChoice[i];
                }
            }
            if (isLegalMove(3, oldmovesetX[MostChosen], oldmovesetY[MostChosen], movesetX[MostChosen], movesetY[MostChosen]) == true) {
                makeMove(3, oldmovesetX[MostChosen], oldmovesetY[MostChosen], movesetX[MostChosen], movesetY[MostChosen]);
                return true;
            } else {
                return userInput(side);
            }
        } else if (side == 7) {
            int rank = -1;
            int old_r = -1, old_C = -1, r = -1, c = -1;
            for (int c2 = 0; c2 < 8; c2++) {
                for (int r2 = 0; r2 < 8; r2++) {
                    if (boardBlack[r2][c2] == '+') {
                        continue;
                    }
                    for (int c3 = 0; c3 < 8; c3++) {
                        for (int r3 = 0; r3 < 8; r3++) {
                            if (!isLegalMove(3, r2, c2, r3, c3)) {
                                continue;
                            }
                            int score = pieceValue(r3, c3);
                            score += (3 - Math.abs(r3 - 3)) + (3 - Math.abs(c3 - 3));
                            if (boardBlack[r2][c2] == 'K') {
                                score -= 5;
                            }
                            if (score > rank) {
                                rank = score;
                                old_r = r2;
                                old_C = c2;
                                r = r3;
                                c = c3;
                            }
                        }
                    }
                }
            }
            if (old_r != -1) {
                makeMove(3, old_r, old_C, r, c);
                return true;
            } else {
                return userInput(side);
            }
        } else {
            out.println("invalid person given");
            return false;
        }
    }
    public static boolean aiThinking(int side) {
        if (side == 3) {
            //out.print(".");
            int r = (int)(Math.random() * 8);
            int c = (int)(Math.random() * 8);
            if (r < 0 || r > 7 || c < 0 || c > 7 || boardBlack[r][c] == '+') {
                return aiThinking(side);
            } else {
                int new_r = (int)(Math.random() * 8);
                int new_c = (int)(Math.random() * 8);
                if (new_r < 0 || new_r > 7 || new_c < 0 || new_c > 7) {
                    return aiThinking(side);
                }
                if (isLegalMove(side, r, c, new_r, new_c)) {
                    move_c = new_c;
                    move_r = new_r;
                    oldmove_c = c;
                    oldmove_r = r;
                    return true;
                } else {
                    return aiThinking(side);
                }
            }
        } else if (side == 4) {
            //out.print(".");
            int r = (int)(Math.random() * 8);
            int c = (int)(Math.random() * 8);
            if (r < 0 || r > 7 || c < 0 || c > 7 || boardWhite[r][c] == '+') {
                return aiThinking(side);
            } else {
                int new_r = (int)(Math.random() * 8);
                int new_c = (int)(Math.random() * 8);
                if (new_r < 0 || new_r > 7 || new_c < 0 || new_c > 7) {
                    return aiThinking(side);
                }
                if (isLegalMove(side, r, c, new_r, new_c)) {
                    move_c = new_c;
                    move_r = new_r;
                    oldmove_c = c;
                    oldmove_r = r;
                    return true;
                } else {
                    return aiThinking(side);
                }
            }
        } else {
            out.println("invalid person given");
            return false;
        }
    }
    public static int pieceValue(int new_r, int new_c) {
        if (board[new_r][new_c] == '+') {
            return 1;
        } else if (board[new_r][new_c] == 'P') {
            return 2;
        } else if (board[new_r][new_c] == 'B') {
            return 5;
        } else if (board[new_r][new_c] == 'n') {
            return 5;
        } else if (board[new_r][new_c] == 'R') {
            return 10;
        } else if (board[new_r][new_c] == 'Q') {
            return 25;
        } else if (board[new_r][new_c] == 'K') {
            return 99999;
        } else {
            return 0;
        }
    }
    public static boolean makeMove(int side, int r, int c, int new_r, int new_c) {
        if (side == 1 || side == 4) {
            if (isLegalMove(side, r, c, new_r, new_c) == true && isCheckMate(side) == false) {
                char oldpiece = board[r][c];
                if (board[new_r][new_c] != '+') {
                    playSound("capture.wav");
                } else {
                    playSound("move-self.wav");
                }
                board[r][c] = '+';
                boardWhite[r][c] = '+';
                boardBlack[r][c] = '+';
                if (oldpiece == 'P') {
                    legalPawnMovesWhite[r] = 1;
                }
                board[new_r][new_c] = oldpiece;
                boardWhite[new_r][new_c] = oldpiece;
                boardBlack[new_r][new_c] = '+';
                if (oldpiece == 'P' && new_c == 7) {
                    playSound("promote.wav");
                    board[new_r][new_c] = 'Q';
                    boardWhite[new_r][new_c] = 'Q';
                }
                out.println("made your move ;)");
                moves++;
                return true;
            } else {
                out.println("not legal");
                return userInput(side);
            }
        } else if (side == 2 || side == 3) {
            if (isLegalMove(side, r, c, new_r, new_c) == true) {
                char oldpiece = board[r][c];
                if (board[new_r][new_c] != '+') {
                    playSound("capture.wav");
                } else {
                    playSound("move-self.wav");
                }
                board[r][c] = '+';
                boardWhite[r][c] = '+';
                boardBlack[r][c] = '+';
                if (oldpiece == 'P') {
                    legalPawnMovesBlack[r] = 1;
                }
                board[new_r][new_c] = oldpiece;
                boardWhite[new_r][new_c] = '+';
                boardBlack[new_r][new_c] = oldpiece;
                if (oldpiece == 'P' && new_c == 0) {
                    playSound("promote.wav");
                    board[new_r][new_c] = 'Q';
                    boardBlack[new_r][new_c] = 'Q';
                }
                out.println("made your move ;)");
                moves++;
                return true;
            } else {
                out.println("not legal");
                return userInput(side);
            }
        } else {
            return false;
        }
    }
    public static boolean isLegalMove(int side, int r, int c, int new_r, int new_c) {
        if (side == 1 || side == 4) {
            for (int c2 = 0; c2 < board.length; c2++) {
                for (int r2 = 0; r2 < board[c2].length; r2++) {
                    legalMoveWhite[r2][c2] = '=';
                }
            }
            if (boardWhite[r][c] == 'P') {
                int[][] move = {
                    {
                        0,
                        1
                    },
                    {
                        1,
                        1
                    },
                    {
                        -1,
                        1
                    },
                    {
                        0,
                        2
                    }
                };
                int row[] = move[0];
                int mr = r + row[0];
                int mc = c + row[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] == '+') {
                        legalMoveWhite[mr][mc] = '*';
                    }
                }
                int row2[] = move[1];
                mr = r + row2[0];
                mc = c + row2[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] != '+' && boardBlack[mr][mc] != '+') {
                        legalMoveWhite[mr][mc] = '*';
                    }
                }
                int row3[] = move[2];
                mr = r + row3[0];
                mc = c + row3[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] != '+' && boardBlack[mr][mc] != '+') {
                        legalMoveWhite[mr][mc] = '*';
                    }
                }
                int row4[] = move[3];
                mr = r + row4[0];
                mc = c + row4[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] == '+' && legalPawnMovesWhite[r] == 0) {
                        legalMoveWhite[mr][mc] = '*';
                    }
                }
            }
            if (boardWhite[r][c] == 'n') {
                int[][] move = {
                    {
                        2,
                        1
                    },
                    {
                        1,
                        2
                    },
                    {
                        -2,
                        1
                    },
                    {
                        2,
                        -1
                    },
                    {
                        -1,
                        2
                    },
                    {
                        -2,
                        -1
                    },
                    {
                        1,
                        -2
                    },
                    {
                        -1,
                        -2
                    }
                };
                for (int i = 0; i < move.length; i++) {
                    int row[] = move[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                            }
                        }
                    }
                }
            }
            if (boardWhite[r][c] == 'R') {
                int[][] move1 = {
                    {
                        0,
                        1
                    },
                    {
                        0,
                        2
                    },
                    {
                        0,
                        3
                    },
                    {
                        0,
                        4
                    },
                    {
                        0,
                        5
                    },
                    {
                        0,
                        6
                    },
                    {
                        0,
                        7
                    },
                    {
                        0,
                        8
                    }
                };
                int[][] move2 = {
                    {
                        0,
                        -1
                    },
                    {
                        0,
                        -2
                    },
                    {
                        0,
                        -3
                    },
                    {
                        0,
                        -4
                    },
                    {
                        0,
                        -5
                    },
                    {
                        0,
                        -6
                    },
                    {
                        0,
                        -7
                    },
                    {
                        0,
                        -8
                    }
                };
                int[][] move3 = {
                    {
                        1,
                        0
                    },
                    {
                        2,
                        0
                    },
                    {
                        3,
                        0
                    },
                    {
                        4,
                        0
                    },
                    {
                        5,
                        0
                    },
                    {
                        6,
                        0
                    },
                    {
                        7,
                        0
                    },
                    {
                        8,
                        0
                    }
                };
                int[][] move4 = {
                    {
                        -1, 0
                    },
                    {
                        -2,
                        0
                    },
                    {
                        -3,
                        0
                    },
                    {
                        -4,
                        0
                    },
                    {
                        -5,
                        0
                    },
                    {
                        -6,
                        0
                    },
                    {
                        -7,
                        0
                    },
                    {
                        -8,
                        0
                    }
                };
                for (int i = 0; i < move1.length; i++) {
                    int row[] = move1[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move2.length; i++) {
                    int row[] = move2[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move3.length; i++) {
                    int row[] = move3[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move4.length; i++) {
                    int row[] = move4[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (boardWhite[r][c] == 'Q') {
                int[][] move1 = {
                    {
                        0,
                        1
                    },
                    {
                        0,
                        2
                    },
                    {
                        0,
                        3
                    },
                    {
                        0,
                        4
                    },
                    {
                        0,
                        5
                    },
                    {
                        0,
                        6
                    },
                    {
                        0,
                        7
                    },
                    {
                        0,
                        8
                    }
                };
                int[][] move2 = {
                    {
                        0,
                        -1
                    },
                    {
                        0,
                        -2
                    },
                    {
                        0,
                        -3
                    },
                    {
                        0,
                        -4
                    },
                    {
                        0,
                        -5
                    },
                    {
                        0,
                        -6
                    },
                    {
                        0,
                        -7
                    },
                    {
                        0,
                        -8
                    }
                };
                int[][] move3 = {
                    {
                        1,
                        0
                    },
                    {
                        2,
                        0
                    },
                    {
                        3,
                        0
                    },
                    {
                        4,
                        0
                    },
                    {
                        5,
                        0
                    },
                    {
                        6,
                        0
                    },
                    {
                        7,
                        0
                    },
                    {
                        8,
                        0
                    }
                };
                int[][] move4 = {
                    {
                        -1, 0
                    },
                    {
                        -2,
                        0
                    },
                    {
                        -3,
                        0
                    },
                    {
                        -4,
                        0
                    },
                    {
                        -5,
                        0
                    },
                    {
                        -6,
                        0
                    },
                    {
                        -7,
                        0
                    },
                    {
                        -8,
                        0
                    }
                };
                int[][] move5 = {
                    {
                        1,
                        1
                    },
                    {
                        2,
                        2
                    },
                    {
                        3,
                        3
                    },
                    {
                        4,
                        4
                    },
                    {
                        5,
                        5
                    },
                    {
                        6,
                        6
                    },
                    {
                        7,
                        7
                    },
                    {
                        8,
                        8
                    }
                };
                int[][] move6 = {
                    {
                        1,
                        -1
                    },
                    {
                        2,
                        -2
                    },
                    {
                        3,
                        -3
                    },
                    {
                        4,
                        -4
                    },
                    {
                        5,
                        -5
                    },
                    {
                        6,
                        -6
                    },
                    {
                        7,
                        -7
                    },
                    {
                        8,
                        -8
                    }
                };
                int[][] move7 = {
                    {
                        -1, 1
                    },
                    {
                        -2,
                        2
                    },
                    {
                        -3,
                        3
                    },
                    {
                        -4,
                        4
                    },
                    {
                        -5,
                        5
                    },
                    {
                        -6,
                        6
                    },
                    {
                        -7,
                        7
                    },
                    {
                        -8,
                        8
                    }
                };
                int[][] move8 = {
                    {
                        -1, -1
                    },
                    {
                        -2,
                        -2
                    },
                    {
                        -3,
                        -3
                    },
                    {
                        -4,
                        -4
                    },
                    {
                        -5,
                        -5
                    },
                    {
                        -6,
                        -6
                    },
                    {
                        -7,
                        -7
                    },
                    {
                        -8,
                        -8
                    }
                };
                for (int i = 0; i < move1.length; i++) {
                    int row[] = move1[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move2.length; i++) {
                    int row[] = move2[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move3.length; i++) {
                    int row[] = move3[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move4.length; i++) {
                    int row[] = move4[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move5.length; i++) {
                    int row[] = move5[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move6.length; i++) {
                    int row[] = move6[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move7.length; i++) {
                    int row[] = move7[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move8.length; i++) {
                    int row[] = move8[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (boardWhite[r][c] == 'B') {
                int[][] move5 = {
                    {
                        1,
                        1
                    },
                    {
                        2,
                        2
                    },
                    {
                        3,
                        3
                    },
                    {
                        4,
                        4
                    },
                    {
                        5,
                        5
                    },
                    {
                        6,
                        6
                    },
                    {
                        7,
                        7
                    },
                    {
                        8,
                        8
                    }
                };
                int[][] move6 = {
                    {
                        1,
                        -1
                    },
                    {
                        2,
                        -2
                    },
                    {
                        3,
                        -3
                    },
                    {
                        4,
                        -4
                    },
                    {
                        5,
                        -5
                    },
                    {
                        6,
                        -6
                    },
                    {
                        7,
                        -7
                    },
                    {
                        8,
                        -8
                    }
                };
                int[][] move7 = {
                    {
                        -1, 1
                    },
                    {
                        -2,
                        2
                    },
                    {
                        -3,
                        3
                    },
                    {
                        -4,
                        4
                    },
                    {
                        -5,
                        5
                    },
                    {
                        -6,
                        6
                    },
                    {
                        -7,
                        7
                    },
                    {
                        -8,
                        8
                    }
                };
                int[][] move8 = {
                    {
                        -1, -1
                    },
                    {
                        -2,
                        -2
                    },
                    {
                        -3,
                        -3
                    },
                    {
                        -4,
                        -4
                    },
                    {
                        -5,
                        -5
                    },
                    {
                        -6,
                        -6
                    },
                    {
                        -7,
                        -7
                    },
                    {
                        -8,
                        -8
                    }
                };
                for (int i = 0; i < move5.length; i++) {
                    int row[] = move5[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move6.length; i++) {
                    int row[] = move6[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move7.length; i++) {
                    int row[] = move7[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move8.length; i++) {
                    int row[] = move8[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (boardWhite[r][c] == 'K') {
                int[][] move = {
                    {
                        1,
                        1
                    },
                    {
                        1,
                        0
                    },
                    {
                        -1,
                        0
                    },
                    {
                        -1,
                        -1
                    },
                    {
                        1,
                        -1
                    },
                    {
                        0,
                        1
                    },
                    {
                        0,
                        -1
                    },
                    {
                        -1,
                        1
                    }
                };
                for (int i = 0; i < move.length; i++) {
                    int row[] = move[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveWhite[mr][mc] = '*';
                        } else {
                            if (boardBlack[mr][mc] != '+') {
                                legalMoveWhite[mr][mc] = '*';
                            }
                        }
                    }
                }
            }
            if (legalMoveWhite[new_r][new_c] == '*') {
                return true;
            } else {
                return false;
            }
        } else if (side == 2 || side == 3) {
            for (int c2 = 0; c2 < board.length; c2++) {
                for (int r2 = 0; r2 < board[c2].length; r2++) {
                    legalMoveBlack[r2][c2] = '=';
                }
            }
            if (boardBlack[r][c] == 'P') {
                int[][] move = {
                    {
                        0,
                        -1
                    },
                    {
                        -1,
                        -1
                    },
                    {
                        1,
                        -1
                    },
                    {
                        0,
                        -2
                    }
                };
                int row[] = move[0];
                int mr = r + row[0];
                int mc = c + row[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] == '+') {
                        legalMoveBlack[mr][mc] = '*';
                    }
                }
                int row2[] = move[1];
                mr = r + row2[0];
                mc = c + row2[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] != '+' && boardWhite[mr][mc] != '+') {
                        legalMoveBlack[mr][mc] = '*';
                    }
                }
                int row3[] = move[2];
                mr = r + row3[0];
                mc = c + row3[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] != '+' && boardWhite[mr][mc] != '+') {
                        legalMoveBlack[mr][mc] = '*';
                    }
                }
                int row4[] = move[3];
                mr = r + row4[0];
                mc = c + row4[1];
                if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                    if (board[mr][mc] == '+' && legalPawnMovesBlack[r] == 0) {
                        legalMoveBlack[mr][mc] = '*';
                    }
                }
            }
            if (boardBlack[r][c] == 'n') {
                int[][] move = {
                    {
                        2,
                        1
                    },
                    {
                        1,
                        2
                    },
                    {
                        -2,
                        1
                    },
                    {
                        2,
                        -1
                    },
                    {
                        -1,
                        2
                    },
                    {
                        -2,
                        -1
                    },
                    {
                        1,
                        -2
                    }
                };
                for (int i = 0; i < move.length; i++) {
                    int row[] = move[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                            }
                        }
                    }
                }
            }
            if (boardBlack[r][c] == 'R') {
                int[][] move1 = {
                    {
                        0,
                        1
                    },
                    {
                        0,
                        2
                    },
                    {
                        0,
                        3
                    },
                    {
                        0,
                        4
                    },
                    {
                        0,
                        5
                    },
                    {
                        0,
                        6
                    },
                    {
                        0,
                        7
                    },
                    {
                        0,
                        8
                    }
                };
                int[][] move2 = {
                    {
                        0,
                        -1
                    },
                    {
                        0,
                        -2
                    },
                    {
                        0,
                        -3
                    },
                    {
                        0,
                        -4
                    },
                    {
                        0,
                        -5
                    },
                    {
                        0,
                        -6
                    },
                    {
                        0,
                        -7
                    },
                    {
                        0,
                        -8
                    }
                };
                int[][] move3 = {
                    {
                        1,
                        0
                    },
                    {
                        2,
                        0
                    },
                    {
                        3,
                        0
                    },
                    {
                        4,
                        0
                    },
                    {
                        5,
                        0
                    },
                    {
                        6,
                        0
                    },
                    {
                        7,
                        0
                    },
                    {
                        8,
                        0
                    }
                };
                int[][] move4 = {
                    {
                        -1, 0
                    },
                    {
                        -2,
                        0
                    },
                    {
                        -3,
                        0
                    },
                    {
                        -4,
                        0
                    },
                    {
                        -5,
                        0
                    },
                    {
                        -6,
                        0
                    },
                    {
                        -7,
                        0
                    },
                    {
                        -8,
                        0
                    }
                };
                for (int i = 0; i < move1.length; i++) {
                    int row[] = move1[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move2.length; i++) {
                    int row[] = move2[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move3.length; i++) {
                    int row[] = move3[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move4.length; i++) {
                    int row[] = move4[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (boardBlack[r][c] == 'Q') {
                int[][] move1 = {
                    {
                        0,
                        1
                    },
                    {
                        0,
                        2
                    },
                    {
                        0,
                        3
                    },
                    {
                        0,
                        4
                    },
                    {
                        0,
                        5
                    },
                    {
                        0,
                        6
                    },
                    {
                        0,
                        7
                    },
                    {
                        0,
                        8
                    }
                };
                int[][] move2 = {
                    {
                        0,
                        -1
                    },
                    {
                        0,
                        -2
                    },
                    {
                        0,
                        -3
                    },
                    {
                        0,
                        -4
                    },
                    {
                        0,
                        -5
                    },
                    {
                        0,
                        -6
                    },
                    {
                        0,
                        -7
                    },
                    {
                        0,
                        -8
                    }
                };
                int[][] move3 = {
                    {
                        1,
                        0
                    },
                    {
                        2,
                        0
                    },
                    {
                        3,
                        0
                    },
                    {
                        4,
                        0
                    },
                    {
                        5,
                        0
                    },
                    {
                        6,
                        0
                    },
                    {
                        7,
                        0
                    },
                    {
                        8,
                        0
                    }
                };
                int[][] move4 = {
                    {
                        -1, 0
                    },
                    {
                        -2,
                        0
                    },
                    {
                        -3,
                        0
                    },
                    {
                        -4,
                        0
                    },
                    {
                        -5,
                        0
                    },
                    {
                        -6,
                        0
                    },
                    {
                        -7,
                        0
                    },
                    {
                        -8,
                        0
                    }
                };
                int[][] move5 = {
                    {
                        1,
                        1
                    },
                    {
                        2,
                        2
                    },
                    {
                        3,
                        3
                    },
                    {
                        4,
                        4
                    },
                    {
                        5,
                        5
                    },
                    {
                        6,
                        6
                    },
                    {
                        7,
                        7
                    },
                    {
                        8,
                        8
                    }
                };
                int[][] move6 = {
                    {
                        1,
                        -1
                    },
                    {
                        2,
                        -2
                    },
                    {
                        3,
                        -3
                    },
                    {
                        4,
                        -4
                    },
                    {
                        5,
                        -5
                    },
                    {
                        6,
                        -6
                    },
                    {
                        7,
                        -7
                    },
                    {
                        8,
                        -8
                    }
                };
                int[][] move7 = {
                    {
                        -1, 1
                    },
                    {
                        -2,
                        2
                    },
                    {
                        -3,
                        3
                    },
                    {
                        -4,
                        4
                    },
                    {
                        -5,
                        5
                    },
                    {
                        -6,
                        6
                    },
                    {
                        -7,
                        7
                    },
                    {
                        -8,
                        8
                    }
                };
                int[][] move8 = {
                    {
                        -1, -1
                    },
                    {
                        -2,
                        -2
                    },
                    {
                        -3,
                        -3
                    },
                    {
                        -4,
                        -4
                    },
                    {
                        -5,
                        -5
                    },
                    {
                        -6,
                        -6
                    },
                    {
                        -7,
                        -7
                    },
                    {
                        -8,
                        -8
                    }
                };
                for (int i = 0; i < move1.length; i++) {
                    int row[] = move1[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move2.length; i++) {
                    int row[] = move2[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move3.length; i++) {
                    int row[] = move3[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move4.length; i++) {
                    int row[] = move4[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move5.length; i++) {
                    int row[] = move5[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move6.length; i++) {
                    int row[] = move6[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move7.length; i++) {
                    int row[] = move7[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move8.length; i++) {
                    int row[] = move8[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (boardBlack[r][c] == 'B') {
                int[][] move5 = {
                    {
                        1,
                        1
                    },
                    {
                        2,
                        2
                    },
                    {
                        3,
                        3
                    },
                    {
                        4,
                        4
                    },
                    {
                        5,
                        5
                    },
                    {
                        6,
                        6
                    },
                    {
                        7,
                        7
                    },
                    {
                        8,
                        8
                    }
                };
                int[][] move6 = {
                    {
                        1,
                        -1
                    },
                    {
                        2,
                        -2
                    },
                    {
                        3,
                        -3
                    },
                    {
                        4,
                        -4
                    },
                    {
                        5,
                        -5
                    },
                    {
                        6,
                        -6
                    },
                    {
                        7,
                        -7
                    },
                    {
                        8,
                        -8
                    }
                };
                int[][] move7 = {
                    {
                        -1, 1
                    },
                    {
                        -2,
                        2
                    },
                    {
                        -3,
                        3
                    },
                    {
                        -4,
                        4
                    },
                    {
                        -5,
                        5
                    },
                    {
                        -6,
                        6
                    },
                    {
                        -7,
                        7
                    },
                    {
                        -8,
                        8
                    }
                };
                int[][] move8 = {
                    {
                        -1, -1
                    },
                    {
                        -2,
                        -2
                    },
                    {
                        -3,
                        -3
                    },
                    {
                        -4,
                        -4
                    },
                    {
                        -5,
                        -5
                    },
                    {
                        -6,
                        -6
                    },
                    {
                        -7,
                        -7
                    },
                    {
                        -8,
                        -8
                    }
                };
                for (int i = 0; i < move5.length; i++) {
                    int row[] = move5[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move6.length; i++) {
                    int row[] = move6[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move7.length; i++) {
                    int row[] = move7[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < move8.length; i++) {
                    int row[] = move8[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (boardBlack[r][c] == 'K') {
                int[][] move = {
                    {
                        1,
                        1
                    },
                    {
                        1,
                        0
                    },
                    {
                        -1,
                        0
                    },
                    {
                        -1,
                        -1
                    },
                    {
                        1,
                        -1
                    },
                    {
                        0,
                        1
                    },
                    {
                        0,
                        -1
                    },
                    {
                        -1,
                        1
                    }
                };
                for (int i = 0; i < move.length; i++) {
                    int row[] = move[i];
                    int mr = r + row[0];
                    int mc = c + row[1];
                    if (mr >= 0 && mr < 8 && mc >= 0 && mc < 8) {
                        if (board[mr][mc] == '+') {
                            legalMoveBlack[mr][mc] = '*';
                        } else {
                            if (boardWhite[mr][mc] != '+') {
                                legalMoveBlack[mr][mc] = '*';
                            }
                        }
                    }
                }
            }
            if (legalMoveBlack[new_r][new_c] == '*') {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static void instructions() {
        out.println("Chess Instructions:");
        out.println();
        out.println("Objective:");
        out.println("- The goal of chess is to checkmate your opponent's king. This means the king is under attack and cannot escape capture.");
        out.println();
        out.println("Basic Rules:");
        out.println("- Each player starts with 16 pieces: 1 king, 1 queen, 2 rooks, 2 bishops, 2 knights, and 8 pawns.");
        out.println("- White moves first, then players alternate turns.");
        out.println("- Pieces move as follows:");
        out.println("  * King: one square in any direction.");
        out.println("  * Queen: any number of squares in any direction.");
        out.println("  * Rook: any number of squares horizontally or vertically.");
        out.println("  * Bishop: any number of squares diagonally.");
        out.println("  * Knight: in an 'L' shape (two squares in one direction, then one square perpendicular).");
        out.println("  * Pawn: one square forward (two squares on its first move); captures one square diagonally forward.");
        out.println();
        out.println("Special Moves:");
        out.println("- Castling: King and rook move simultaneously under certain conditions.");
        out.println("- En passant: Special pawn capture when a pawn moves two squares forward from its starting position.");
        out.println("- Pawn promotion: When a pawn reaches the opposite end, it is promoted (usually to a queen).");
        out.println();
        out.println("How to Win:");
        out.println("- Checkmate your opponent's king or capture their king.");
        out.println("- The game can also end in a draw (stalemate, insufficient material, threefold repetition, or 50-move rule).");
        out.println();
        out.println("Good luck and have fun!");
    }
    public static void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}