package com.mycompany.mavenproject2.Frames;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import com.mycompany.mavenproject2.Client.INetworkListener;
import com.mycompany.mavenproject2.Client.NetworkManager;
import com.mycompany.mavenproject2.Common.Dice;
import com.mycompany.mavenproject2.Common.GameState;
import com.mycompany.mavenproject2.Common.NetworkMessage;
import com.mycompany.mavenproject2.Common.NetworkMessage.MessageType;
import com.mycompany.mavenproject2.Common.ScoringLogic;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author ggunes
 */
public class GameFrame extends javax.swing.JFrame implements INetworkListener {

    /**
     * Creates new form GameFrame
     */
    public GameFrame(NetworkManager networkManager, int myPlayerId, String roomId) {
        initComponents();
        currentSession = new GameState();
        scoreLabels = new HashMap<>();
        player1ScoreLabels = new HashMap<>();
        player2ScoreLabels = new HashMap<>();
        this.networkManager = networkManager;
        this.myPlayerId = myPlayerId;
        this.roomId = roomId;
        initializeScoreLabelMaps();
        if (myPlayerId == 1) {
            ownPlayerlbl.setText("Player 1");
            otherPlayerlbl.setText("Player 2");
            scoreLabels.putAll(player1ScoreLabels);

        } else if (myPlayerId == 2) {
            ownPlayerlbl.setText("Player 2");
            otherPlayerlbl.setText("Player 1");
            scoreLabels.putAll(player2ScoreLabels);
        }

        for (String category : scoreLabels.keySet()) {
            JLabel label = scoreLabels.get(category);
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    scoreLabelClicked(category);
                }
            });
        }
        roomIDlbl.setText(roomId);
        resetFinalScoreLabels();
    }
    private GameState currentSession; //That variable includes the current situation of the game
    private HashMap<String, JLabel> scoreLabels;
    private HashMap<String, JLabel> player1ScoreLabels;
    private HashMap<String, JLabel> player2ScoreLabels;
    private NetworkManager networkManager;
    private Boolean isWaitingForServer = false;
    private boolean gameOverDialogShown = false;
    private boolean reconnectInProgress = false;
    private int myPlayerId;
    private String roomId;

    public String getDiceIcon(int value) {
        switch (value) {
            case 1:
                return "⚀";
            case 2:
                return "⚁";
            case 3:
                return "⚂";
            case 4:
                return "⚃";
            case 5:
                return "⚄";
            case 6:
                return "⚅";
            default:
                return "";
        }
    }

    private void scoreLabelClicked(String category) {
        //  CHECK: Is it my turn?
        if (isWaitingForServer || currentSession.getCurrentPlayer() != myPlayerId) {
            return;
        }

        // CHECK: Were the dice rolled?
        if (currentSession.getCurrentDices()[0].getNumber() == 0) {
            return;
        }

        // Select Local Table
        Map<String, Integer> myScoreMap = (myPlayerId == 1)
                ? currentSession.getPlayer1Score()
                : currentSession.getPlayer2Score();

        if (myScoreMap.get(category) == -1) {
            try {
                //  LOCAL UPDATE START 

                //  First, calculate the score and enter it in the local state.
                int calculatedScore = ScoringLogic.calculateScore(category, currentSession.getCurrentDices());
                myScoreMap.put(category, calculatedScore);

                // Pass the turn to the other player (setCurrentPlayer)
                int nextPlayer = (myPlayerId == 1) ? 2 : 1;
                currentSession.setCurrentPlayer(nextPlayer);

                // Lock the interface
                this.isWaitingForServer = true;
                disableAllScoreLabels();
                RollButton.setEnabled(false);
                setHoldButtonsEnabled(false);

                //  REPORT TO SERVER 
                Object[] data = {category, currentSession.getCurrentDices()};
                NetworkMessage msg = new NetworkMessage(MessageType.SAVE_SCORE, data, "Player" + myPlayerId);
                networkManager.sendMessage(msg);

                // Update the image (See the score immediately on your own screen)
                updateScorePreviews(currentSession);

                System.out.println("Local update completed, next up: Player " + nextPlayer);

            } catch (IOException e) {
                // If there is a mistake, undo
                this.isWaitingForServer = false;
                enableScoreLabelsIfMyTurn();
                System.err.println("Connection Error: " + e.getMessage());
            }
        }
    }

    private void updateScorePreviews(GameState state) {
        Map<String, Integer> myScores = (myPlayerId == 1) ? state.getPlayer1Score() : state.getPlayer2Score();

        renderSavedScores(player1ScoreLabels, state.getPlayer1Score());
        renderSavedScores(player2ScoreLabels, state.getPlayer2Score());
        updateFinalScoreLabels(state);

        for (String category : scoreLabels.keySet()) {
            JLabel label = scoreLabels.get(category);

            if (myScores.get(category) != -1) {
                continue;
            }

            if (state.getCurrentDices()[0].getNumber() == 0 || state.getCurrentPlayer() != myPlayerId) {
                label.setText("");
                label.setForeground(new Color(180, 180, 180));
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            } else {
                int potential = ScoringLogic.calculateScore(category, state.getCurrentDices());
                label.setText(String.valueOf(potential));
                label.setForeground(new Color(180, 180, 180));
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
        }
        updateTurnIndicator(state);
    }

    private void initializeScoreLabelMaps() {
        player1ScoreLabels.put("Ones", lblOnes);
        player1ScoreLabels.put("Twos", lblTwos);
        player1ScoreLabels.put("Threes", lblThrees);
        player1ScoreLabels.put("Fours", lblFours);
        player1ScoreLabels.put("Fives", lblFives);
        player1ScoreLabels.put("Sixes", lblSixes);
        player1ScoreLabels.put("Three of a Kind", lblThreeKind);
        player1ScoreLabels.put("Four of a Kind", lblFourKind);
        player1ScoreLabels.put("Full House", lblFullHouse);
        player1ScoreLabels.put("Small Straight", lblSmallStr);
        player1ScoreLabels.put("Large Straight", lblLargeStr);
        player1ScoreLabels.put("Yahtzee", lblYahtzee);
        player1ScoreLabels.put("Chance", lblChance);

        player2ScoreLabels.put("Ones", lblOnes2);
        player2ScoreLabels.put("Twos", lblTwos2);
        player2ScoreLabels.put("Threes", lblThrees2);
        player2ScoreLabels.put("Fours", lblFours2);
        player2ScoreLabels.put("Fives", lblFives2);
        player2ScoreLabels.put("Sixes", lblSixes2);
        player2ScoreLabels.put("Three of a Kind", lblThreeKind2);
        player2ScoreLabels.put("Four of a Kind", lblFourKind2);
        player2ScoreLabels.put("Full House", lblFullHouse2);
        player2ScoreLabels.put("Small Straight", lblSmallStr2);
        player2ScoreLabels.put("Large Straight", lblLargeStr2);
        player2ScoreLabels.put("Yahtzee", lblYahtzee2);
        player2ScoreLabels.put("Chance", lblChance2);
    }

    private void renderSavedScores(Map<String, JLabel> labelMap, Map<String, Integer> scoreMap) {
        // Iterate through all scoring categories
        for (String category : labelMap.keySet()) {
            JLabel label = labelMap.get(category);
            int fixedScore = scoreMap.get(category);

            // If the score is -1, the category has not been used yet
            if (fixedScore == -1) {
                label.setText("");
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
                continue;
            }
            // Display the saved score
            label.setText(String.valueOf(fixedScore));
            // Highlight the saved score
            label.setForeground(Color.BLACK);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
    }

    private void updateFinalScoreLabels(GameState state) {
        if (state == null || !state.isGameOver()) {
            resetFinalScoreLabels();
            return;
        }

        int p1UpperTotal = ScoringLogic.calculateUpperSectionTotal(state.getPlayer1Score());
        int p2UpperTotal = ScoringLogic.calculateUpperSectionTotal(state.getPlayer2Score());
        int p1Bonus = ScoringLogic.calculateUpperSectionBonus(state.getPlayer1Score());
        int p2Bonus = ScoringLogic.calculateUpperSectionBonus(state.getPlayer2Score());
        int p1FinalScore = ScoringLogic.calculateFinalScore(state.getPlayer1Score());
        int p2FinalScore = ScoringLogic.calculateFinalScore(state.getPlayer2Score());

        player1Total.setText(String.valueOf(p1UpperTotal));
        player2Total.setText(String.valueOf(p2UpperTotal));
        player1Bonus.setText(String.valueOf(p1Bonus));
        player2Bonus.setText(String.valueOf(p2Bonus));
        player1Score.setText(String.valueOf(p1FinalScore));
        player2Score.setText(String.valueOf(p2FinalScore));
    }

    private void resetFinalScoreLabels() {
        player1Total.setText("");
        player2Total.setText("");
        player1Bonus.setText("");
        player2Bonus.setText("");
        player1Score.setText("");
        player2Score.setText("");
    }

    private void showGameOverDialog() {
        if (gameOverDialogShown) {
            return;
        }
        gameOverDialogShown = true;

        int p1FinalScore = ScoringLogic.calculateFinalScore(currentSession.getPlayer1Score());
        int p2FinalScore = ScoringLogic.calculateFinalScore(currentSession.getPlayer2Score());
        String winnerText;

        if (p1FinalScore > p2FinalScore) {
            winnerText = "Winner: Player 1";
        } else if (p2FinalScore > p1FinalScore) {
            winnerText = "Winner: Player 2";
        } else {
            winnerText = "DRAW";
        }

        Object[] options = {"Play Again", "Exit the Game"};
        int choice = JOptionPane.showOptionDialog(
                this,
                winnerText + "\nPlayer 1 Score: " + p1FinalScore + "\nPlayer 2 Score: " + p2FinalScore,
                "Oyun Bitti",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            exitGame();
        }
    }

    private void restartGame() {
        // Create a fresh game state while keeping both players in the same room.
        GameState newGameState = new GameState();

        try {
            // Send the new state to the server so both clients restart together.
            networkManager.sendMessage(new NetworkMessage(MessageType.GAME_UPDATE, newGameState, "Player" + myPlayerId));
        } catch (IOException e) {
            System.err.println("Game could not be restarted: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Game could not be restarted: " + e.getMessage());
            return;
        }

        // Update the local client immediately instead of waiting for the server echo.
        this.currentSession = newGameState;
        this.gameOverDialogShown = false;
        this.isWaitingForServer = false;

         // Clear final score labels and redraw the score table for a new game.
        resetFinalScoreLabels();
        updateScorePreviews(currentSession);
        // Clear dice visuals and reset hold states.
        Dice1.setText("");
        Dice2.setText("");
        Dice3.setText("");
        Dice4.setText("");
        Dice5.setText("");
        resetDiceHoldButtons();

        // Player 1 always starts the new game.
        RollButton.setEnabled(myPlayerId == 1);
        if (myPlayerId == 1) {
            enableScoreLabelsIfMyTurn();
        } else {
            disableAllScoreLabels();
            setHoldButtonsEnabled(false);
        }

        revalidate();
        repaint();
    }

    private void exitGame() {
        try {
            networkManager.disconnect();
        } catch (IOException e) {
            System.err.println("Connection could not be closed: " + e.getMessage());
        }
        System.exit(0);
    }

    private void handleServerDisconnected(String errorMessage) {
        // Prevent multiple reconnect threads from starting for the same connection loss.
        if (reconnectInProgress) {
            return;
        }
        reconnectInProgress = true;

        // Lock the game UI because the game cannot continue without the server.
        isWaitingForServer = true;
        RollButton.setEnabled(false);
        disableAllScoreLabels();
        setHoldButtonsEnabled(false);

        // Start a background thread that periodically tries to reconnect to the server.
        Thread reconnectThread = new Thread(() -> {
            while (reconnectInProgress) {
                try {
                     // Wait before each retry to avoid constantly hammering the server.
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // If the server is back online, return the user to the main menu.
                if (networkManager.reconnect()) {
                    java.awt.EventQueue.invokeLater(() -> {
                        try {
                             // Close the temporary reconnect socket before opening a new game flow.
                            networkManager.disconnect();
                        } catch (IOException e) {
                            System.err.println("Connection could not be closed: " + e.getMessage());
                        }
                        reconnectInProgress = false;
                        // The simple reconnect flow does not restore the old room,
                        // so the user is sent back to the main menu.
                        JOptionPane.showMessageDialog(
                                this,
                                "Server connection re-established.\nReturning to the main menu as the old room information was not preserved..",
                                "Connection Restored",
                                JOptionPane.INFORMATION_MESSAGE);
                        new StartFrame().setVisible(true);
                        this.dispose();
                    });
                    return;
                }
            }
        });
        // Allow the application to close even if the reconnect thread is still running.
        reconnectThread.setDaemon(true);
        reconnectThread.start();

         // Inform the user that the connection was lost and reconnect attempts are running
        JOptionPane.showMessageDialog(
                this,
                errorMessage + "\nThe main menu will be restored once the server is back online..",
                "Connection Lost",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator12 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        jSeparator14 = new javax.swing.JSeparator();
        jSeparator15 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSeparator16 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        lblOnes = new javax.swing.JLabel();
        lblOnes2 = new javax.swing.JLabel();
        lblTwos = new javax.swing.JLabel();
        lblThrees = new javax.swing.JLabel();
        lblTwos2 = new javax.swing.JLabel();
        lblThrees2 = new javax.swing.JLabel();
        lblFours = new javax.swing.JLabel();
        lblFours2 = new javax.swing.JLabel();
        lblFives = new javax.swing.JLabel();
        lblFives2 = new javax.swing.JLabel();
        lblSixes = new javax.swing.JLabel();
        lblSixes2 = new javax.swing.JLabel();
        lblThreeKind = new javax.swing.JLabel();
        lblThreeKind2 = new javax.swing.JLabel();
        lblFourKind = new javax.swing.JLabel();
        lblFourKind2 = new javax.swing.JLabel();
        lblFullHouse = new javax.swing.JLabel();
        lblFullHouse2 = new javax.swing.JLabel();
        lblSmallStr = new javax.swing.JLabel();
        lblSmallStr2 = new javax.swing.JLabel();
        lblLargeStr = new javax.swing.JLabel();
        lblLargeStr2 = new javax.swing.JLabel();
        lblChance = new javax.swing.JLabel();
        lblChance2 = new javax.swing.JLabel();
        lblYahtzee = new javax.swing.JLabel();
        lblYahtzee2 = new javax.swing.JLabel();
        player1Total = new javax.swing.JLabel();
        player2Total = new javax.swing.JLabel();
        player1Bonus = new javax.swing.JLabel();
        player2Bonus = new javax.swing.JLabel();
        player1Score = new javax.swing.JLabel();
        player2Score = new javax.swing.JLabel();
        otherPlayerlbl = new javax.swing.JLabel();
        ownPlayerlbl = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        Dice1 = new javax.swing.JLabel();
        Dice2 = new javax.swing.JLabel();
        Dice3 = new javax.swing.JLabel();
        Dice4 = new javax.swing.JLabel();
        Dice5 = new javax.swing.JLabel();
        Dice1HoldButton = new javax.swing.JButton();
        Dice2HoldButton = new javax.swing.JButton();
        Dice3HoldButton = new javax.swing.JButton();
        Dice4HoldButton = new javax.swing.JButton();
        Dice5HoldButton = new javax.swing.JButton();
        RollButton = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        roomIDlbl = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocation(new java.awt.Point(320, 170));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(20, 100, 40));
        jPanel1.setPreferredSize(new java.awt.Dimension(893, 571));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator12.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator12.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator11.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator11.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator2.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator10.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator10.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator9.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator9.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator4.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator8.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator8.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator7.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator7.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator6.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator6.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator5.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator5.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator13.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator13.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator14.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator14.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator15.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator15.setForeground(new java.awt.Color(0, 0, 0));

        jLabel1.setText("Ones");

        jLabel2.setText("Twos");

        jLabel4.setText("Threes");

        jLabel5.setText("Fours");

        jLabel6.setText("Fives");

        jLabel7.setText("Sixes");

        jLabel8.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel8.setText("TOTAL");

        jLabel9.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel9.setText("BONUS");

        jLabel15.setText("Three of a Kind");

        jLabel11.setText("Four of a Kind");

        jLabel13.setText("Full House");

        jLabel14.setText("Small Straight");

        jLabel10.setText("Large Straight");

        jLabel3.setText("Chance");

        jLabel12.setText("YAHTZEE");

        jSeparator16.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator16.setForeground(new java.awt.Color(0, 0, 0));

        jLabel16.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel16.setText("SCORE");

        jLabel17.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 0, 51));
        jLabel17.setText("PLAYER 1");

        jLabel18.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 0, 51));
        jLabel18.setText("PLAYER 2");

        lblOnes.setForeground(new java.awt.Color(153, 153, 153));
        lblOnes.setText("0");

        lblOnes2.setForeground(new java.awt.Color(255, 51, 51));
        lblOnes2.setText("0");

        lblTwos.setForeground(new java.awt.Color(153, 153, 153));
        lblTwos.setText("0");

        lblThrees.setForeground(new java.awt.Color(153, 153, 153));
        lblThrees.setText("0");

        lblTwos2.setForeground(new java.awt.Color(255, 51, 51));
        lblTwos2.setText("0");

        lblThrees2.setForeground(new java.awt.Color(255, 51, 51));
        lblThrees2.setText("0");

        lblFours.setForeground(new java.awt.Color(153, 153, 153));
        lblFours.setText("0");

        lblFours2.setForeground(new java.awt.Color(255, 51, 51));
        lblFours2.setText("0");

        lblFives.setForeground(new java.awt.Color(153, 153, 153));
        lblFives.setText("0");

        lblFives2.setForeground(new java.awt.Color(255, 51, 51));
        lblFives2.setText("0");

        lblSixes.setForeground(new java.awt.Color(153, 153, 153));
        lblSixes.setText("0");

        lblSixes2.setForeground(new java.awt.Color(255, 51, 51));
        lblSixes2.setText("0");

        lblThreeKind.setForeground(new java.awt.Color(153, 153, 153));
        lblThreeKind.setText("0");

        lblThreeKind2.setForeground(new java.awt.Color(255, 51, 51));
        lblThreeKind2.setText("0");

        lblFourKind.setForeground(new java.awt.Color(153, 153, 153));
        lblFourKind.setText("0");

        lblFourKind2.setForeground(new java.awt.Color(255, 51, 51));
        lblFourKind2.setText("0");

        lblFullHouse.setForeground(new java.awt.Color(153, 153, 153));
        lblFullHouse.setText("0");

        lblFullHouse2.setForeground(new java.awt.Color(255, 51, 51));
        lblFullHouse2.setText("0");

        lblSmallStr.setForeground(new java.awt.Color(153, 153, 153));
        lblSmallStr.setText("0");

        lblSmallStr2.setForeground(new java.awt.Color(255, 51, 51));
        lblSmallStr2.setText("0");

        lblLargeStr.setForeground(new java.awt.Color(153, 153, 153));
        lblLargeStr.setText("0");

        lblLargeStr2.setForeground(new java.awt.Color(255, 51, 51));
        lblLargeStr2.setText("0");

        lblChance.setForeground(new java.awt.Color(153, 153, 153));
        lblChance.setText("0");

        lblChance2.setForeground(new java.awt.Color(255, 51, 51));
        lblChance2.setText("0");

        lblYahtzee.setForeground(new java.awt.Color(153, 153, 153));
        lblYahtzee.setText("0");

        lblYahtzee2.setForeground(new java.awt.Color(255, 51, 51));
        lblYahtzee2.setText("0");

        player1Total.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N

        player2Total.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N

        player1Bonus.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N

        player2Bonus.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N

        player1Score.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N

        player2Score.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addComponent(jSeparator11)
            .addComponent(jSeparator12)
            .addComponent(jSeparator10)
            .addComponent(jSeparator2)
            .addComponent(jSeparator1)
            .addComponent(jSeparator9)
            .addComponent(jSeparator4)
            .addComponent(jSeparator8)
            .addComponent(jSeparator7)
            .addComponent(jSeparator6)
            .addComponent(jSeparator15)
            .addComponent(jSeparator14)
            .addComponent(jSeparator5)
            .addComponent(jSeparator13, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator16)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addGap(34, 34, 34))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(98, 98, 98)
                .addComponent(lblOnes, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblOnes2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addGap(74, 74, 74)
                .addComponent(player1Score, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player2Score, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(98, 98, 98)
                .addComponent(lblTwos, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTwos2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addGap(91, 91, 91)
                .addComponent(lblThrees, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblThrees2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(98, 98, 98)
                .addComponent(lblFours, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFours2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(99, 99, 99)
                .addComponent(lblFives, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFives2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(99, 99, 99)
                .addComponent(lblSixes, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSixes2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addGap(90, 90, 90)
                .addComponent(player1Total, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player2Total, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addGap(86, 86, 86)
                .addComponent(player1Bonus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player2Bonus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel15)
                .addGap(42, 42, 42)
                .addComponent(lblThreeKind, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblThreeKind2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addGap(50, 50, 50)
                .addComponent(lblFourKind, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFourKind2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel13)
                .addGap(71, 71, 71)
                .addComponent(lblFullHouse, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFullHouse2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel14)
                .addGap(53, 53, 53)
                .addComponent(lblSmallStr, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSmallStr2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addGap(54, 54, 54)
                .addComponent(lblLargeStr, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblLargeStr2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(91, 91, 91)
                .addComponent(lblChance, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblChance2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(80, 80, 80)
                .addComponent(lblYahtzee, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblYahtzee2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addGap(1, 1, 1)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOnes, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOnes2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTwos2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTwos, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator11, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblThrees, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblThrees2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFours, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFours2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFives, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFives2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSixes, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSixes2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(player1Total, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(player2Total, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(player1Bonus, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(player2Bonus, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblThreeKind, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblThreeKind2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFourKind, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFourKind2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFullHouse, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFullHouse2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSmallStr2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSmallStr, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator13, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(lblLargeStr, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLargeStr2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator14, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblChance, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblChance2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator15, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblYahtzee, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblYahtzee2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator16, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(player1Score)
                    .addComponent(player2Score))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 10, 320, 540));

        otherPlayerlbl.setFont(new java.awt.Font("Helvetica Neue", 1, 28)); // NOI18N
        otherPlayerlbl.setForeground(new java.awt.Color(255, 0, 51));
        otherPlayerlbl.setText("PLAYER 2");
        jPanel1.add(otherPlayerlbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, -1, -1));

        ownPlayerlbl.setFont(new java.awt.Font("Helvetica Neue", 1, 28)); // NOI18N
        ownPlayerlbl.setForeground(new java.awt.Color(255, 0, 51));
        ownPlayerlbl.setText("PLAYER 1");
        jPanel1.add(ownPlayerlbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 460, -1, -1));

        jPanel3.setBackground(new java.awt.Color(20, 100, 40));

        Dice1.setFont(new java.awt.Font("Helvetica Neue", 0, 100)); // NOI18N
        Dice1.setText("⚀");

        Dice2.setFont(new java.awt.Font("Helvetica Neue", 0, 100)); // NOI18N
        Dice2.setText("⚀");

        Dice3.setFont(new java.awt.Font("Helvetica Neue", 0, 100)); // NOI18N
        Dice3.setText("⚀");

        Dice4.setFont(new java.awt.Font("Helvetica Neue", 0, 100)); // NOI18N
        Dice4.setText("⚀");

        Dice5.setFont(new java.awt.Font("Helvetica Neue", 0, 100)); // NOI18N
        Dice5.setText("⚀");

        Dice1HoldButton.setFont(new java.awt.Font("Helvetica Neue", 1, 10)); // NOI18N
        Dice1HoldButton.setText("HOLD");
        Dice1HoldButton.setEnabled(false);
        Dice1HoldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice1HoldButtonActionPerformed(evt);
            }
        });

        Dice2HoldButton.setFont(new java.awt.Font("Helvetica Neue", 1, 10)); // NOI18N
        Dice2HoldButton.setText("HOLD");
        Dice2HoldButton.setEnabled(false);
        Dice2HoldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice2HoldButtonActionPerformed(evt);
            }
        });

        Dice3HoldButton.setFont(new java.awt.Font("Helvetica Neue", 1, 10)); // NOI18N
        Dice3HoldButton.setText("HOLD");
        Dice3HoldButton.setEnabled(false);
        Dice3HoldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice3HoldButtonActionPerformed(evt);
            }
        });

        Dice4HoldButton.setFont(new java.awt.Font("Helvetica Neue", 1, 10)); // NOI18N
        Dice4HoldButton.setText("HOLD");
        Dice4HoldButton.setEnabled(false);
        Dice4HoldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice4HoldButtonActionPerformed(evt);
            }
        });

        Dice5HoldButton.setFont(new java.awt.Font("Helvetica Neue", 1, 10)); // NOI18N
        Dice5HoldButton.setText("HOLD");
        Dice5HoldButton.setEnabled(false);
        Dice5HoldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice5HoldButtonActionPerformed(evt);
            }
        });

        RollButton.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        RollButton.setText("ROLL DICES");
        RollButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RollButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(Dice1HoldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(Dice2HoldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(Dice3HoldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(Dice4HoldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(Dice5HoldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(Dice1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Dice2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Dice3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Dice4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Dice5, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(RollButton, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(10, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Dice1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Dice2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Dice3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Dice4, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Dice5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Dice1HoldButton)
                    .addComponent(Dice2HoldButton)
                    .addComponent(Dice3HoldButton)
                    .addComponent(Dice4HoldButton)
                    .addComponent(Dice5HoldButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                .addComponent(RollButton)
                .addContainerGap())
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 190, 390, 250));

        jLabel20.setText("ROOM ID:");
        jPanel1.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 550, -1, -1));

        roomIDlbl.setText("XXXXXX");
        jPanel1.add(roomIDlbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 550, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void RollButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RollButtonActionPerformed
        // Check if it is this player's turn
        if (currentSession.getCurrentPlayer() != myPlayerId) {
            JOptionPane.showMessageDialog(this, "Not Your Turn! .");
            return;
        }

        GameState state = currentSession;
        // Check if the player still has rolls left
        if (state.getRollsLeft() > 0) {
            Dice[] dices = state.getCurrentDices();
            for (int i = 0; i < dices.length; i++) {
                if (!dices[i].isHeld()) {
                    dices[i].setNumber(dices[i].rollDice());
                }
            }
            // Decrease remaining roll count
            state.setRollsLeft(state.getRollsLeft() - 1);

            try {
                // The dice have been changed, notify the server so your opponent can see it too.
                networkManager.sendMessage(new NetworkMessage(MessageType.GAME_UPDATE, state, "Player" + myPlayerId));
            } catch (IOException e) {
                System.err.println("Dice information could not be transmitted to the server.!");
            }

            updateScorePreviews(state);
            refreshHoldButtons();
        } else {
            setHoldButtonsEnabled(false);
            JOptionPane.showMessageDialog(null, "You've used up all your dice, please choose a score.");
        }

    }//GEN-LAST:event_RollButtonActionPerformed

    @Override
    public void onChatMessageReceived(NetworkMessage msg) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onGameStatusUpdate(NetworkMessage msg) {
        java.awt.EventQueue.invokeLater(() -> {
            // Memorize the current game status.
            this.currentSession = (GameState) msg.getData();
            if (!currentSession.isGameOver()) {
                this.gameOverDialogShown = false;
            }

            // Unlock it first: The wait ends the moment the package arrives.
            this.isWaitingForServer = false;

            // Queue control
            boolean isMyTurn = (currentSession.getCurrentPlayer() == myPlayerId);

            // Update the Scoreboard (including opponent scores)
            updateScorePreviews(this.currentSession);

            // Update the Dice: Clear them in the new round (when it's 0), otherwise press the icons.
            Dice[] d = currentSession.getCurrentDices();
            if (d[0].getNumber() == 0) {
                Dice1.setText("");
                Dice2.setText("");
                Dice3.setText("");
                Dice4.setText("");
                Dice5.setText("");
                resetDiceHoldButtons();
            } else {
                updateDiceIcons(d);
            }

            if (currentSession.isGameOver()) {
                RollButton.setEnabled(false);
                setHoldButtonsEnabled(false);
                disableAllScoreLabels();
                revalidate();
                repaint();
                showGameOverDialog();
                return;
            }

            //  (CRITICAL) Enable/Disable Button and Panel Locks in Sequence
            RollButton.setEnabled(isMyTurn);

            if (isMyTurn) {
                // My turn: Make empty categories clickable.
                enableScoreLabelsIfMyTurn();
                refreshHoldButtons();
                System.out.println(" Player " + myPlayerId + "'s turn.");
            } else {
                // Now it's the opponent's turn: Lock everything down.
                disableAllScoreLabels();
                setHoldButtonsEnabled(false);
            }

            // refresh the interface
            revalidate();
            repaint();
        });

    }

    @Override
    public void onErrorMessageReceived(NetworkMessage msg) {
        java.awt.EventQueue.invokeLater(() -> {
            if ("NETWORK".equals(msg.getSender())) {
                handleServerDisconnected(String.valueOf(msg.getData()));
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + msg.getData(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void onJoinMessageReceived(NetworkMessage msg) {
        java.awt.EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Player 2 Joined.\nRoom ID: " + msg.getData(),
                    "Player Joined",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Override
    public void onCreateMessageReceived(NetworkMessage msg) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void Dice1HoldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice1HoldButtonActionPerformed
        currentSession.getCurrentDices()[0].changeStatu();
        refreshHoldButtons();
    }//GEN-LAST:event_Dice1HoldButtonActionPerformed

    private void Dice2HoldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice2HoldButtonActionPerformed
        currentSession.getCurrentDices()[1].changeStatu();
        refreshHoldButtons();
    }//GEN-LAST:event_Dice2HoldButtonActionPerformed

    private void Dice3HoldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice3HoldButtonActionPerformed
        currentSession.getCurrentDices()[2].changeStatu();
        refreshHoldButtons();
    }//GEN-LAST:event_Dice3HoldButtonActionPerformed

    private void Dice4HoldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice4HoldButtonActionPerformed
        currentSession.getCurrentDices()[3].changeStatu();
        refreshHoldButtons();
    }//GEN-LAST:event_Dice4HoldButtonActionPerformed

    private void Dice5HoldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice5HoldButtonActionPerformed
        currentSession.getCurrentDices()[4].changeStatu();
        refreshHoldButtons();
    }//GEN-LAST:event_Dice5HoldButtonActionPerformed

    private void disableAllScoreLabels() {
        // Activate the logical lock.
        isWaitingForServer = true; // Logical lock

        for (JLabel label : scoreLabels.values()) {
            label.setEnabled(false); // Click lock

            // If the cell is not full (if the text is not black), clear its contents.
            if (label.getForeground() != Color.BLACK) {
                label.setText("");
            }
        }
    }

    private void enableScoreLabelsIfMyTurn() {
        // Only unlock the lock if it's our turn.
        if (currentSession.getCurrentPlayer() == myPlayerId && !isWaitingForServer) {
            isWaitingForServer = false; // Don't lock

            //Get each Player's own table
            Map<String, Integer> myScores = (myPlayerId == 1)
                    ? currentSession.getPlayer1Score() : currentSession.getPlayer2Score();

            for (String category : scoreLabels.keySet()) {
                JLabel lbl = scoreLabels.get(category);
                // If the category is empty, make it clickable (-1).
                if (myScores.get(category) == -1) {
                    lbl.setEnabled(true);
                } else {
                    // Leave the already filled score visible but not clickable (the ones in black).
                    lbl.setEnabled(false);
                }
            }
        }
    }

    private void updateTurnIndicator(GameState state) {

        if (state == null) {
            return;
        }

        // Display player identifiers on the UI
        ownPlayerlbl.setText("Player " + myPlayerId);
        otherPlayerlbl.setText("Player " + (myPlayerId == 1 ? 2 : 1));

        // Check whose turn it is and update label colors accordingly
        if (state.getCurrentPlayer() == myPlayerId) {
            // This player's turn (green)
            ownPlayerlbl.setForeground(new Color(34, 139, 34));
            otherPlayerlbl.setForeground(new Color(178, 34, 34));
        } else {
            // Opponent's turn (green)
            ownPlayerlbl.setForeground(new Color(178, 34, 34));
            otherPlayerlbl.setForeground(new Color(34, 139, 34));
        }
    }

    private void resetDiceHoldButtons() {
        // Reset the held state of all dice
        for (Dice d : currentSession.getCurrentDices()) {
            d.setHeld(false);
        }
        // Disable hold buttons since no dice are currently held
        setHoldButtonsEnabled(false);

        // Update button text to reflect hold states
        updateHoldButtonTexts();
    }

    private void refreshHoldButtons() {
        // Determine whether the player is allowed to hold dice
        boolean canHold = currentSession.getCurrentPlayer() == myPlayerId
                && !isWaitingForServer
                && currentSession.getCurrentDices()[0].getNumber() != 0
                && currentSession.getRollsLeft() > 0;

        // Enable or disable hold buttons based on the conditions
        setHoldButtonsEnabled(canHold);
        // Update hold button text to reflect dice hold states
        updateHoldButtonTexts();
    }

    private void setHoldButtonsEnabled(boolean enabled) {
        Dice1HoldButton.setEnabled(enabled);
        Dice2HoldButton.setEnabled(enabled);
        Dice3HoldButton.setEnabled(enabled);
        Dice4HoldButton.setEnabled(enabled);
        Dice5HoldButton.setEnabled(enabled);
    }

    private void updateHoldButtonTexts() {
        Dice[] dices = currentSession.getCurrentDices();
        Dice1HoldButton.setText(dices[0].isHeld() ? "HELD" : "HOLD");
        Dice2HoldButton.setText(dices[1].isHeld() ? "HELD" : "HOLD");
        Dice3HoldButton.setText(dices[2].isHeld() ? "HELD" : "HOLD");
        Dice4HoldButton.setText(dices[3].isHeld() ? "HELD" : "HOLD");
        Dice5HoldButton.setText(dices[4].isHeld() ? "HELD" : "HOLD");
    }

    private void updateDiceIcons(Dice[] dices) {
        // It turns each die number in the sequence into an icon and prints it to the labels.
        Dice1.setText(getDiceIcon(dices[0].getNumber()));
        Dice2.setText(getDiceIcon(dices[1].getNumber()));
        Dice3.setText(getDiceIcon(dices[2].getNumber()));
        Dice4.setText(getDiceIcon(dices[3].getNumber()));
        Dice5.setText(getDiceIcon(dices[4].getNumber()));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new GameFrame(networkManager).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Dice1;
    private javax.swing.JButton Dice1HoldButton;
    private javax.swing.JLabel Dice2;
    private javax.swing.JButton Dice2HoldButton;
    private javax.swing.JLabel Dice3;
    private javax.swing.JButton Dice3HoldButton;
    private javax.swing.JLabel Dice4;
    private javax.swing.JButton Dice4HoldButton;
    private javax.swing.JLabel Dice5;
    private javax.swing.JButton Dice5HoldButton;
    private javax.swing.JButton RollButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JLabel lblChance;
    private javax.swing.JLabel lblChance2;
    private javax.swing.JLabel lblFives;
    private javax.swing.JLabel lblFives2;
    private javax.swing.JLabel lblFourKind;
    private javax.swing.JLabel lblFourKind2;
    private javax.swing.JLabel lblFours;
    private javax.swing.JLabel lblFours2;
    private javax.swing.JLabel lblFullHouse;
    private javax.swing.JLabel lblFullHouse2;
    private javax.swing.JLabel lblLargeStr;
    private javax.swing.JLabel lblLargeStr2;
    private javax.swing.JLabel lblOnes;
    private javax.swing.JLabel lblOnes2;
    private javax.swing.JLabel lblSixes;
    private javax.swing.JLabel lblSixes2;
    private javax.swing.JLabel lblSmallStr;
    private javax.swing.JLabel lblSmallStr2;
    private javax.swing.JLabel lblThreeKind;
    private javax.swing.JLabel lblThreeKind2;
    private javax.swing.JLabel lblThrees;
    private javax.swing.JLabel lblThrees2;
    private javax.swing.JLabel lblTwos;
    private javax.swing.JLabel lblTwos2;
    private javax.swing.JLabel lblYahtzee;
    private javax.swing.JLabel lblYahtzee2;
    private javax.swing.JLabel otherPlayerlbl;
    private javax.swing.JLabel ownPlayerlbl;
    private javax.swing.JLabel player1Bonus;
    private javax.swing.JLabel player1Score;
    private javax.swing.JLabel player1Total;
    private javax.swing.JLabel player2Bonus;
    private javax.swing.JLabel player2Score;
    private javax.swing.JLabel player2Total;
    private javax.swing.JLabel roomIDlbl;
    // End of variables declaration//GEN-END:variables

    

   
}
