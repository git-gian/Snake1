
import java.awt.*;
import java.awt.event.*;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javax.swing.*;

import java.util.Random;

public class GamePanel extends JPanel implements ActionListener{
    
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 75;
    static final int FEVER_DELAY = 45;
    final int x[] = new int[GAME_UNITS]; // x coordinates of the snake body
    final int y[] = new int[GAME_UNITS]; // y coordinates of the snake body
    int bodyParts = 6;
    int applesEaten;
    int appleX; // x coordinate of apple
    int appleY; // y coordinate of apple
    int mineX; // x coordinate of mine
    int mineY; // y coordinate of mine
    int highScore = 0;
    char direction = 'R';
    boolean running = false;
    boolean isAppleGolden = false;
    boolean isFeverMode = false;
    boolean isNewHighScore = false;
    Timer timer;
    Random random;

    GamePanel(){

        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame(){

        spawnApple(); //spawn a new apple
        running = true;
        if (isFeverMode){
            timer = new Timer(FEVER_DELAY, this);
        }
        else{
            timer = new Timer(DELAY, this);
        }
        timer.start();
        playSound("sounds/newGameSound.wav");
    }

    public void paintComponent(Graphics g){ //this method is called behind the scenes when the Frame is created, happens after startGame()
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){

        if(running){
            /*
            //grid for testing purposes
            for(int i = 0; i < SCREEN_HEIGHT/UNIT_SIZE; i++){
                g.drawLine(i*UNIT_SIZE, 0, i*UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i*UNIT_SIZE, SCREEN_WIDTH, i*UNIT_SIZE);
            }
            */

            //spawn mine
            if (isFeverMode){
                
                g.setColor(Color.gray);
                g.fillRect(mineX, mineY, UNIT_SIZE, UNIT_SIZE);
            }

            //drawing the apple
            if(isAppleGolden){

                g.setColor(Color.yellow); //golden apples are worth 5 points
                g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE); //apple is one size unit (square) of the entire grid
            }
            else{

                g.setColor(Color.red);
                g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE); //apple is one size unit (square) of the entire grid
            }
 
            //making the snake
            for (int i = 0; i < bodyParts; i++){
                
                //head of the snake
                if (i == 0){

                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
                //rest of the snake body
                else{

                    if (isFeverMode){
                        g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))); //spawn crazy colors!
                    }
                    else{
                        g.setColor(new Color(45, 180, 0));
                    }
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            //setting the running scoreboard text
            g.setColor(getBackground() == Color.black ? Color.white : Color.black); //if background is black, set text to white, else set to black
            g.setFont(new Font("Arial", Font.BOLD, 25));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten + "                High Score: " + highScore, (SCREEN_WIDTH - metrics.stringWidth("Score " + applesEaten + "               High Score: " + highScore))/2, g.getFont().getSize());
        }
        else{
            gameOver(g);
        }
    }

    public void spawnApple(){

        //set the coordinates for the apple
        if (random.nextInt(10) == 3 && applesEaten > 0){

            isAppleGolden = true;
        }
        else{

            isAppleGolden = false;
        }
        appleX = random.nextInt((SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
        appleY = random.nextInt((SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
    }

    public void spawnMine(){

        //set the coordinates for the mine
        mineX = random.nextInt((SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
        mineY = random.nextInt((SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
    }

    public void move(){

        //advancing the snake BODY
        for (int i = bodyParts; i > 0; i--){

            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        //advancing the snake HEAD
        switch(direction) {

            case 'U': 
                y[0] = y[0] - UNIT_SIZE;
                break;

            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;

            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;

            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;

            default:
            break;
        }
    }

    public void checkApple(){

        //if head is touching apple
        if ((x[0] == appleX) && (y[0] == appleY)){
            
            int soundFileNum = random.nextInt(3);

            if (soundFileNum == 0){

                playSound("sounds/applebite.wav");

            }else if (soundFileNum == 1){

                playSound("sounds/applebite2.wav");

            }else if (soundFileNum == 2){

                playSound("sounds/applebite3.wav");
            }

            if (isAppleGolden){
                bodyParts = bodyParts + 5;
                applesEaten = applesEaten + 5;
                isAppleGolden = false;
            }else{
                bodyParts++;
                applesEaten++;
            }
            
            if ((applesEaten > highScore && highScore != 0) && !isNewHighScore){
                playSound("sounds/newHighScore.wav");
                isNewHighScore = true;
            }

            spawnApple();
            if (isFeverMode){
                spawnMine();
            }
        }
    }

    public void checkCollisions(){

        //check if head collides w/ left, right, top, and bottom borders (respectively)
        if ((x[0] < 0) || (x[0] > SCREEN_WIDTH) || (y[0] < 0) || (y[0] > SCREEN_HEIGHT)) {

            running = false;
            playSound("sounds/gameover.wav");
            timer.stop();
        }

        if (isFeverMode){
            if ((x[0] == mineX) && (y[0] == mineY)){
                running = false;
                playSound("sounds/gameover.wav");
                timer.stop(); 
            }
        }

        for (int i = bodyParts; i > 0; i--){

            //checks if head collided w body
            if ((x[0] == x[i]) && (y[0] == y[i])){
                running = false;
                playSound("sounds/gameover.wav");
                timer.stop();
                break;
            }
        }
    }

    public void gameOver(Graphics g){

        //Game Over text
        g.setColor(getBackground() == Color.black ? Color.white : Color.black); //if background is black, set text to white, else set to black
        g.setFont(new Font("Arial", Font.BOLD, 70));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over"))/2, SCREEN_HEIGHT/2 - 100 );
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score " + applesEaten))/2, SCREEN_HEIGHT/2);

        if (isNewHighScore || highScore == 0){

            highScore = applesEaten;
            g.drawString("New High Score!", (SCREEN_WIDTH - metrics.stringWidth("New High Score!"))/2, SCREEN_HEIGHT/2 + 85);
            isNewHighScore = false;
        }
        else{

            g.drawString("High Score: " + highScore, (SCREEN_WIDTH - metrics.stringWidth("High Score " + highScore))/2, SCREEN_HEIGHT/2 + 85);
        }

        //play again text
        g.setFont(new Font("Arial", Font.BOLD, 25));
        metrics = getFontMetrics(g.getFont());
        g.drawString("Play Again? Y / N", (SCREEN_WIDTH - metrics.stringWidth("Play Again? Y / N"))/2, SCREEN_HEIGHT/2 + 150);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(running){

            move();
            checkApple();
            checkCollisions();
        }
        repaint();

    }

    public void playSound(String filepath){

        try{

            File soundPath = new File(filepath);

            if (soundPath.exists()){

                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();

            }
            else{

                System.out.println(soundPath.getAbsolutePath());
                System.out.println("Cannot find file");
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }

    public void resetGame(){

        //clear snake body
        for (int i = bodyParts + 1; i >= 0; i--){
            
            x[i] = 0;
            y[i] = 0;
        }

        //reset game states
        bodyParts = 6;
        applesEaten = 0;
        running = true;
        direction = 'R';
        startGame();
    }

    public class MyKeyAdapter extends KeyAdapter{

        @Override
        public void keyPressed(KeyEvent e){

            switch(e.getKeyCode()){

                case KeyEvent.VK_LEFT:
                    if (direction != 'R'){
                        direction = 'L';
                    }
                    break;

                case KeyEvent.VK_RIGHT:
                    if (direction != 'L'){
                        direction = 'R';
                    }
                    break;

                case KeyEvent.VK_UP:
                    if (direction != 'D'){
                        direction = 'U';
                    }
                    break;

                case KeyEvent.VK_DOWN:
                    if (direction!= 'U'){
                        direction = 'D';
                    }
                    break;

                case KeyEvent.VK_F:

                    /* TESTING
                    JComponent comp = (JComponent) e.getSource(); //Get the source of the action performed (keypressed -> GamePanel)
                    Window win = SwingUtilities.getWindowAncestor(comp); //Get the window ancestor of comp/GamePanel which is GameFrame
                    win.setSize(2000, 2000);
                    win.setLocationRelativeTo(null);
                    */
                    if (isFeverMode && timer.isRunning()){
                        isFeverMode = false;
                        playSound("sounds/feverMode.wav");
                        timer.setDelay(DELAY);
                    }
                    else if (!isFeverMode && timer.isRunning()){
                        
                        spawnMine();
                        isFeverMode = true;
                        playSound("sounds/feverMode.wav");
                        timer.setDelay(FEVER_DELAY);
                    }
                    break;

                case KeyEvent.VK_L:
                    setBackground(getBackground() == Color.black ? new Color(237,240,225) : Color.black);
                    playSound("sounds/lightswitch.wav");
                    break;

                case KeyEvent.VK_Y:
                    if (!running){
                        resetGame();
                    }
                    break;

                case KeyEvent.VK_A:
                    if (!isAppleGolden){
                        isAppleGolden = true;
                    }
                    break;

                case KeyEvent.VK_P:
                    if (timer.isRunning() && running){
                        timer.stop();
                        playSound("sounds/pause.wav");
                    }else if (running){
                        timer.start();
                        playSound("sounds/pause.wav");
                    }
                    break;

                case KeyEvent.VK_N:
                    if (!running){ //only execute if game is over
                        JComponent comp = (JComponent) e.getSource(); //Get the source of the action performed (keypressed -> GamePanel)
                        Window win = SwingUtilities.getWindowAncestor(comp); //Get the window ancestor of comp/GamePanel which is GameFrame
                        win.dispose(); //can now use Window method dispose() to close window
                    }
                    break;
                default:
                break;
            }
        }
    }
}
