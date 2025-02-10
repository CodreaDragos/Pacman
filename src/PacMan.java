import java.awt.*;
import java.awt.event.*;
import java.sql.Time;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import javax.swing.*;


public class PacMan extends JPanel implements ActionListener, KeyListener {

    class Block{
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction= 'U'; // U = up, D = down, L = left, R = right
        int velocityX = 0;
        int velocityY = 0;
        char pendingDirection = ' ';
        boolean isScared = false;

        Block(Image image, int x, int y, int width, int height){
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }
        void updateDirection(char direction){
            pendingDirection = direction;
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for(Block wall : walls){
                if (Collision(this, wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                    break;
                }
            }
        }

        void updateVelocity(){
            if (this.direction == 'U'){
                this.velocityX = 0;
                this.velocityY = -titleSize/4;
            }
            else if (this.direction == 'D'){
                this.velocityX = 0;
                this.velocityY = titleSize/4;
            }
            else if (this.direction == 'L'){
                this.velocityX = -titleSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R'){
                this.velocityX = titleSize/4;
                this.velocityY = 0;
            }

    }
    void applyPendingDirection() {
        if (pendingDirection != ' ') {
            char prevDirection = this.direction;
            this.direction = pendingDirection;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;

            boolean collided = false;
            for (Block wall : walls) {
                if (Collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection; // Revert direction
                    updateVelocity();
                    collided = true;
                    break;
                }
            }

            if (!collided) {
                pendingDirection = ' '; // Clear the pending direction if successful
                updateImage();          // Update the image only if direction changes
            }
            else {
                this.direction = prevDirection; // Ensure direction stays the same
            }
        }
    }
    void updateImage() {
        if (this == pacman) { // Only update the image for Pac-Man
            if (direction == 'U') {
                this.image = pacmanUpImage;
            } else if (direction == 'D') {
                this.image = pacmanDownImage;
            } else if (direction == 'L') {
                this.image = pacmanLeftImage;
            } else if (direction == 'R') {
                this.image = pacmanRightImage;
            }
        }
    }
    

    void reset(){
        this.x = this.startX;
        this.y = this.startY;
    }
}

    private int rowCount =21;
    private int columnCount = 19;
    private int titleSize = 32;
    private int boardWidth = columnCount * titleSize;
    private int boardHeight = rowCount * titleSize;
    private boolean ghostsScared = false;
    private long scaredStartTime;


     private Image wallImage;
     private Image blueGhostImage;
     private Image orangeGhostImage;
     private Image pinkGhostImage;
     private Image redGhostImage;
     private Image cherryImage;
     private Image scaredGhostImage;


     private Image pacmanUpImage;
     private Image pacmanDownImage;
     private Image pacmanLeftImage;
     private Image pacmanRightImage;
  

      //X = wall, O = skip, P = pac man, C=cherries , ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X      C X",
        "X XX XXX X XXX XX X",
        "X C               X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X  C X   X   X   CX",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block>  walls;
    HashSet<Block>  foods;
    HashSet<Block>  ghosts;
    HashSet<Block> cherries;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score=0;
    int lives =3;
    boolean gameOver = false;

    PacMan() {  

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        //load images

        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();


        loadMap();
        for(Block ghost : ghosts){
           char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        gameLoop = new Timer(50, this);  //20fps (1000/50)
        gameLoop.start();
   


    }

    public void loadMap(){

        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        cherries = new HashSet<Block>();

        for(int r =0; r < rowCount; r++)
          for(int c=0; c < columnCount; c++){
            String row = tileMap[r];
            char tileMapChar = row.charAt(c);

            int x = c * titleSize;
            int y = r * titleSize;

            if (tileMapChar =='X'){// block wall
            
                Block wall = new Block(wallImage, x, y, titleSize, titleSize);
                walls.add(wall);
            }
            if (tileMapChar =='C'){// cherries
            
                Block cherryBlock = new Block(cherryImage, x, y, titleSize, titleSize);
                cherries.add(cherryBlock);
            }
            else if (tileMapChar =='b'){ //blue ghost

                Block ghost = new Block(blueGhostImage, x, y, titleSize, titleSize);
                ghosts.add(ghost);

            }
            else if (tileMapChar =='o'){ //orange ghost

                Block ghost = new Block(orangeGhostImage, x, y, titleSize, titleSize);
                ghosts.add(ghost);

            }
            else if (tileMapChar =='p'){ //pink ghost

                Block ghost = new Block(pinkGhostImage, x, y, titleSize, titleSize);
                ghosts.add(ghost);

            }
            else if (tileMapChar =='r'){ //red ghost

                Block ghost = new Block(redGhostImage, x, y, titleSize, titleSize);
                ghosts.add(ghost);

            }
            else if (tileMapChar == 'P') //PacMan
            {
                pacman = new Block(pacmanRightImage, x, y, titleSize, titleSize);
            }
            else if (tileMapChar == ' ') //food
            {
                Block food = new Block(null, x + 14, y + 14, 4, 4);
                foods.add(food);
            }
          }

        
    }


    public void paintComponent(Graphics g){

        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){

        g.drawImage(pacman.image,pacman.x, pacman.y, pacman.width, pacman.height, null);

        for(Block ghost : ghosts){
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
        for(Block wall : walls){
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }
        g.setColor(Color.WHITE);
        for(Block food : foods){
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        //score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if(gameOver){
            g.drawString("Game Over:" + String.valueOf(score), titleSize/2, titleSize/2);
        }
        else{
            g.drawString("x" + String.valueOf(lives) +" Score: " + String.valueOf(score), titleSize/2, titleSize/2);
        }
        if (ghostsScared) {
            long timeLeft = 10 - (System.currentTimeMillis() - scaredStartTime) / 1000;
            g.setColor(Color.YELLOW);
            g.drawString("Scared Time: " + timeLeft, titleSize / 2, titleSize);
        }

    }
    public void move(){

        pacman.applyPendingDirection();

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Check for teleportation
    if (pacman.x < 0) { // Left side teleport
        pacman.x = boardWidth - pacman.width;
    } else if (pacman.x + pacman.width > boardWidth) { // Right side teleport
        pacman.x = 0;
    }

        //check for collision with walls
        for(Block wall : walls){
            if (Collision(pacman, wall)){
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check ghost collision
        for(Block ghost : ghosts){
            if (Collision(ghost, pacman)){
                if (ghostsScared) {
                    // Pac-Man eats the ghost
                    score += 200;
                    ghost.reset(); // Respawn ghost
                    ghost.updateDirection(directions[random.nextInt(4)]);
                } else {
                lives=lives-1;
                if(lives == 0){
                    gameOver = true;
                }
             resetPositions();
            }
            }
        
            if(ghost.y ==titleSize*9 && ghost.direction != 'U' && ghost.direction != 'D'){
                ghost.updateDirection('U');
            }
                ghost.x += ghost.velocityX;
                ghost.y += ghost.velocityY;

                // Check for teleportation
        if (ghost.x < 0) { // Left side teleport
            ghost.x = boardWidth - ghost.width;
        } else if (ghost.x + ghost.width > boardWidth) { // Right side teleport
            ghost.x = 0;
        }

                for(Block wall : walls){
                    if (Collision(ghost, wall)){
                        ghost.x -= ghost.velocityX;
                        ghost.y -= ghost.velocityY;
                        char newDirection = directions[random.nextInt(4)];
                        ghost.updateDirection(newDirection);
                        break;
                    }
                }
        
        }
        //check food collision
        Block foodEaten = null;
        for(Block food : foods){
            if (Collision(pacman, food)){
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

         // Check cherry collision
    Block cherryEaten = null;
    for (Block cherry : cherries) {
        if (Collision(pacman, cherry)) {
            cherryEaten = cherry;
            score += 100;
             // Make ghosts scared
             ghostsScared = true;
             scaredStartTime = System.currentTimeMillis();
             for (Block ghost : ghosts) {
                 ghost.image = scaredGhostImage;
             }
        }
    }
    cherries.remove(cherryEaten);

    if (ghostsScared && System.currentTimeMillis() - scaredStartTime >= 10000) {
        ghostsScared = false;
        for (Block ghost : ghosts) {
            if (ghost.image == scaredGhostImage) {
                System.out.println(ghost.startY);
                // Reset ghost image to normal based on its initial position
                if (ghost.startX == 8 * titleSize && ghost.startY == 9 * titleSize) {
                    ghost.image = blueGhostImage; // Blue ghost starting position
                    System.out.println(ghost.startX);
                } else if (ghost.startX == 10 * titleSize && ghost.startY == 9 * titleSize) {
                    ghost.image = orangeGhostImage; // Orange ghost starting position
                } else if (ghost.startX == 9 * titleSize && ghost.startY == 9 * titleSize) {
                    ghost.image = pinkGhostImage; // Pink ghost starting position
                } else if (ghost.startX == 9 * titleSize && ghost.startY == 8 * titleSize) {
                    ghost.image = redGhostImage; // Red ghost starting position
                }
            }
        }
    }
    
    
        if (foods.isEmpty() && cherries.isEmpty()){
         loadMap();
         resetPositions();
        }

    }
    public boolean Collision(Block a, Block b){
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void resetPositions(){
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for(Block ghost : ghosts){
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
      
    }
    private Image getGhostImage(Block ghost) {
        // Check the ghost's starting position and determine its original image
        if (ghost.startX == 10 * titleSize && ghost.startY == 3 * titleSize) {
            return blueGhostImage; // Blue ghost starting position
        } else if (ghost.startX == 10 * titleSize && ghost.startY == 5 * titleSize) {
            return orangeGhostImage; // Orange ghost starting position
        } else if (ghost.startX == 9 * titleSize && ghost.startY == 5 * titleSize) {
            return pinkGhostImage; // Pink ghost starting position
        } else if (ghost.startX == 11 * titleSize && ghost.startY == 5 * titleSize) {
            return redGhostImage; // Red ghost starting position
        }
        return null; // Fallback if no matching position is found
    }
    



    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver){
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {  
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(gameOver){
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        //System.out.println("KeyEvent: " + e.getKeyCode());
        if(e.getKeyCode() == KeyEvent.VK_UP){
            pacman.updateDirection('U');
         
            
        }
        else if(e.getKeyCode() == KeyEvent.VK_DOWN){
            pacman.updateDirection('D');
         
           
        }
        else if(e.getKeyCode() == KeyEvent.VK_LEFT){
            pacman.updateDirection('L');
         
           
        }
        else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            pacman.updateDirection('R');
          
            
        }
        
    }
}



   
