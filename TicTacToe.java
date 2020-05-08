import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import javax.sound.sampled.*;

/**
 *  A class modelling a tic-tac-toe (noughts and crosses, Xs and Os) game.
 * 
 * @author Lynn Marshall
 * @version November 8, 2012
 * 
 * @author Patrick Gibson
 * @version 2019-03-30
 */

public class TicTacToe implements ActionListener
{
    private JTextField gameStatus;               //Current status of the game: game in progress, whose turn, etc.

    private ArrayList<JButton> boardButtons;     //Buttons which make up the Tic-Tac-Toe board
    private String board[][];                    //Recorded values of the board

    private static final int ROW = 3;
    private static final int COL = ROW;

    //Menu items
    private JMenuItem newGameItem;
    private JMenuItem quitItem;
    private JMenuItem statsItem;

    public static final String PLAYER_X = "X"; // player using "X"
    public static final String PLAYER_O = "O"; // player using "O"
    public static final String EMPTY = "";  // empty cell
    public static final String TIE = "T"; // game ended in a tie

    private String player;   // current player (PLAYER_X or PLAYER_O)

    private String winner;   // winner: PLAYER_X, PLAYER_O, TIE, EMPTY = in progress

    private int xWins;   //Number of wins X had
    private int oWins;   //Number of wins O has
    private int draws;   //Number of ties

    private int spacesRemaining; // number of squares still free

    private static final ImageIcon XPIC = new ImageIcon("x.jpg");
    private static final ImageIcon OPIC = new ImageIcon("o.jpg");
    private static final ImageIcon EMPTYIMAGE = new ImageIcon("emptyImage.jpg");

    //Stream and clip required for sounds
    private static Clip clip;
    private static AudioInputStream inputStream;

    public TicTacToe()
    {
        //Initializes variables
        xWins = 0;
        oWins = 0;
        draws = 0;
        winner = EMPTY;

        //Sets up frame
        JFrame frame = new JFrame("Tic-Tac-Toe");
        Container contentPane = frame.getContentPane(); 
        contentPane.setLayout(new BorderLayout());

        //Sets up gameboard in frame
        JPanel gameBoard = new JPanel();
        gameBoard.setLayout(new GridLayout(ROW, COL));

        //Sets up game status at bottom of frame
        gameStatus = new JTextField();
        gameStatus.setEditable(false);
        gameStatus.setFont(new Font(null, Font.BOLD, 18));
        gameStatus.setHorizontalAlignment(JTextField.LEFT);
        gameStatus.setText("");

        //Adds to frame
        contentPane.add(gameBoard);
        contentPane.add(gameStatus, BorderLayout.SOUTH);

        //Menu Bar
        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);

        //Options Menu
        JMenu fileMenu = new JMenu("Options");
        menubar.add(fileMenu);

        //Items for options menu
        newGameItem = new JMenuItem("New Game");
        statsItem = new JMenuItem("Stats");
        quitItem = new JMenuItem("Quit");

        fileMenu.add(newGameItem);
        fileMenu.add(statsItem);
        fileMenu.add(quitItem);

        //Sets up the board information and action listeners
        boardButtons = new ArrayList<JButton>();
        board = new String [ROW][COL];

        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                boardButtons.add(new JButton(""));
            }
        }

        for (JButton button : boardButtons) {
            gameBoard.add(button);
            button.addActionListener(this);
        }

        // Listen for menu selections
        newGameItem.addActionListener(this);
        statsItem.addActionListener(this);
        quitItem.addActionListener(new ActionListener() // create an anonymous inner class
            { // start of anonymous subclass of ActionListener
                // this allows us to put the code for this action here  
                public void actionPerformed(ActionEvent event)
                {
                    System.exit(0); // quit
                }
            } // end of anonymous subclass
        ); // end of addActionListener parameter list and statement

        //Shortcuts for quit and new game
        final int SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_MASK));
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_MASK));

        //Finish setting up the frame
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // exit when we hit the "X"
        frame.pack(); // pack everthing into our frame
        frame.setResizable(true); // we can resize it
        frame.setSize(600, 600);
        frame.setVisible(true); // it's visible

        //Plays first game
        playGame();
    }

    /** This action listener is called when the user clicks on 
     * any of the GUI's buttons. 
     */
    public void actionPerformed(ActionEvent e)
    {
        Object o = e.getSource(); // get the action 

        // see if it's a JButton
        if (o instanceof JButton) {

            JButton button = (JButton)o;

            //Goes through the board listening for each button
            for (int row = 0; row < ROW; row++) {
                for (int col = 0; col < COL; col++) {

                    JButton spaceButton = boardButtons.get(row*ROW + col); // Repeated call, so a variable is made

                    if (button  == spaceButton && board[row][col] == EMPTY) {

                        if (player == PLAYER_X) {       //Sets icon to respective player
                            spaceButton.setIcon(XPIC);
                        } else {
                            spaceButton.setIcon(OPIC);
                        }
                        makeMove(row, col);             //Updates the board and switches players
                    }
                }
            }
        } else { // it's a JMenuItem
            JMenuItem item = (JMenuItem)o;

            if (item == newGameItem) {      //Starts new game
                playGame();
            } else if(item == statsItem) {  //Opens stats window
                statsWindow();
            }
        }
    }    
    /**
     * Resets all the information needed for a new game.
     */
    public void playGame()
    {
        clearBoard();
        stopSounds();
        
        //Picks who goes first. Either loser of previous game or X if its a new game.
        if (winner == EMPTY) {
            player = PLAYER_X;
            gameStatus.setText("Game: In progress     Turn: " + player);
        } else {
            changeTurn();
        }
        
        //Resets winner and number of empty spaces.
        winner = EMPTY;
        spacesRemaining = 9;
    }
    /**
     * Clears the board and the buttons which make up the board on the frame.
     */
    private void clearBoard()
    {
        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                JButton spaceButton = boardButtons.get(row*ROW + col);
                spaceButton.setEnabled(true);
                spaceButton.setBackground(Color.WHITE);
                spaceButton.setIcon(EMPTYIMAGE);
                board[row][col] = EMPTY;
            }
        }
    }
    /**
     * Sees if there is a winner or not. If there is a winner, it disables all buttons except the ones that made three in a row.
     * 
     * @param int row of square just set
     * @param int col of square just set
     * 
     * @return true if we have a winner, false otherwise
     */
    private boolean haveWinner(int row, int col)
    {
        // Note: We don't need to check all rows, columns, and diagonals, only those
        // that contain the latest filled square.  We know that we have a winner 
        // if all 3 squares are the same, as they can't all be blank (as the latest
        // filled square is one of them).

        // check row "row"
        if ( board[row][0].equals(board[row][1]) &&
             board[row][0].equals(board[row][2]) ) {

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COL; j++) {
                    if (i != row) {     //Disables all buttons not which were not three in a row
                        JButton spaceButton = boardButtons.get(i*ROW + j);
                        spaceButton.setEnabled(false);
                    }
                }
            }

            return true;
        }

        // check column "col"
        if ( board[0][col].equals(board[1][col]) &&
        board[0][col].equals(board[2][col]) )  {

            for (int i = 0; i < ROW; i++) {
                for (int j = 0; j < COL; j++) {
                    if (j != col) {     //Disables all buttons which were not three in a column
                        JButton spaceButton = boardButtons.get(i*ROW + j);
                        spaceButton.setEnabled(false);
                    }
                }
            }

            return true;
        }

        // if row=col check one diagonal
        if (row==col) {
            if ( board[0][0].equals(board[1][1]) &&
            board[0][0].equals(board[2][2]) ) {

                for (int i = 0; i < ROW; i++) {
                    for (int j = 0; j < COL; j++) {
                        if (j != i) {   //Disables all buttons which were not three in a diagonal from top left to bottom right
                            JButton spaceButton = boardButtons.get(i*ROW + j);
                            spaceButton.setEnabled(false);
                        }
                    }
                }
                return true;
            }
        }

        // if row=2-col check other diagonal
        if (row==2-col) {
            if ( board[0][2].equals(board[1][1]) &&
            board[0][2].equals(board[2][0]) ) {

                for (int i = 0; i < ROW; i++) {
                    for (int j = 0; j < COL; j++) {
                        if (i + j != 2) {   //Disables all buttons which were not three in a diagonal from bottom left to top right
                            JButton spaceButton = boardButtons.get(i*ROW + j);
                            spaceButton.setEnabled(false);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }
    
    /**
     * Updates the board with the new move.
     * 
     * @param int row of square just set
     * @param int col of square just set
     */
    private void makeMove(int row, int col)
    {
        //Updates board and reduces number of spaces remaining
        board[row][col] = player;
        spacesRemaining--;
        
        //Check if there's a winner and adjust stats accordingly
        if (spacesRemaining < 5) {
            if (haveWinner(row, col)) {
                winner = player;
                if (winner == PLAYER_X){
                    xWins++;
                } else {
                    oWins++;
                }
                endGame();
                return;  //prevents changeTurn from overriding the output text when a game is finished
            }
        }
        
        playSoundEffect();

        //If no spaces remaining, it's a tie since a winner would be determined above.
        if (spacesRemaining == 0) {
            draws++;
            endGame();
            return;
        }
        
        //If it gets here, there is no winner and still spaces available. So the game switches turns.
        changeTurn();
    }

    /**
     * Changes whose turns it is.
     */
    private void changeTurn()
    {
        if (player == PLAYER_X) {
            player = PLAYER_O;
        } else {
            player = PLAYER_X;
        }
        //Updates text at bottom of frame
        gameStatus.setText("Game: In progress     Turn: " + player);
    }

    /**
     * Takes care of the denouement of the game
     */
    private void endGame()
    {
        if (winner != EMPTY) {
            gameStatus.setText("Game: Over- " + player + " wins!");
            playSong();
        } else {
            gameStatus.setText("Game: Over- It's a draw.");
        }
    }

    /**
     * Opens a stat window showing the wins and losses
     */
    private void statsWindow()
    {
        JOptionPane stats = new JOptionPane();
        int total = xWins + oWins;
        stats.showMessageDialog(null, "Total games played: " + total + "\n" +
            "X wins: " + xWins + "\n" + 
            "O wins: " + oWins + "\n" +
            "Draws:  " + draws, "Stats", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Plays the song when there is a winner.
     */
    private void playSong()
    {
        try {
            clip = AudioSystem.getClip();
            inputStream = AudioSystem.getAudioInputStream(TicTacToe.class.getResourceAsStream("music.wav"));
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
        }
    }

    /**
     * Plays a "yuh" whenever a player makes a move.
     */
    private void playSoundEffect()
    {
        try {
            clip = AudioSystem.getClip();
            inputStream = AudioSystem.getAudioInputStream(TicTacToe.class.getResourceAsStream("yuh.wav"));
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
        }
    }

    /**
     * Ensures all sounds have stopped when needed.
     */
    private void stopSounds()
    {
        if (clip != null && clip.isRunning()){
            clip.stop();
        }
    }
}