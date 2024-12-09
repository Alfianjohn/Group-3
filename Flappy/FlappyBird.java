import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image[] backgroundImgs;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Indeks latar belakang saat ini
    int currentBackgroundIndex = 0;

    // Bird
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 55;
    int birdHeight = 55;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game Logic
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    boolean showMainMenu = true; 
    double score = 0;

    Image[] topPipeImgs;
    Image[] bottomPipeImgs;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImgs = new Image[]{
            new ImageIcon(getClass().getResource("./flappybirdbg1.png")).getImage(),
            new ImageIcon(getClass().getResource("./flappybirdbg2.png")).getImage(),
            new ImageIcon(getClass().getResource("./flappybirdbg3.png")).getImage()
        };
        birdImg = new ImageIcon(getClass().getResource("./bird.png")).getImage();

        topPipeImgs = new Image[]{
            new ImageIcon(getClass().getResource("./toppipe1.png")).getImage(),
            new ImageIcon(getClass().getResource("./toppipe2.png")).getImage(),
            new ImageIcon(getClass().getResource("./toppipe3.png")).getImage()
        };
        bottomPipeImgs = new Image[]{
            new ImageIcon(getClass().getResource("./bottompipe1.png")).getImage(),
            new ImageIcon(getClass().getResource("./bottompipe2.png")).getImage(),
            new ImageIcon(getClass().getResource("./bottompipe3.png")).getImage()
        };

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        int pipeIndex = ((int) score / 5) % topPipeImgs.length;

        Pipe topPipe = new Pipe(topPipeImgs[pipeIndex]);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImgs[pipeIndex]);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showMainMenu) {
            drawMainMenu(g);
        } else {
            draw(g);
        }
    }

    public void drawMainMenu(Graphics g) {
        // background gambar pertama
        g.drawImage(backgroundImgs[0], 0, 0, boardWidth, boardHeight, null);
    
        // font dan ukurannya
        Font titleFont = new Font("Serif", Font.BOLD, 36);
        Font instructionFont = new Font("Serif", Font.PLAIN, 24);
    
        // Judul
        g.setFont(titleFont);
        FontMetrics titleMetrics = g.getFontMetrics(titleFont);
        String title = "FLAPPY BIRD";
        int titleX = (boardWidth - titleMetrics.stringWidth(title)) / 2; // Hitung posisi X agar teks di tengah
        int titleY = boardHeight / 3;
    
        // Efek bayangan untuk judul
        g.setColor(Color.DARK_GRAY);
        g.drawString(title, titleX + 2, titleY + 2); // Bayangan
        g.setColor(Color.WHITE);
        g.drawString(title, titleX, titleY); // Teks utama
    
        // Instruksi
        g.setFont(instructionFont);
        FontMetrics instructionMetrics = g.getFontMetrics(instructionFont);
        String instruction = "Press SPACE to Start";
        int instructionX = (boardWidth - instructionMetrics.stringWidth(instruction)) / 2; // Hitung posisi X agar teks di tengah
        int instructionY = boardHeight / 2;
    
        // Efek bayangan untuk instruksi
        g.setColor(Color.DARK_GRAY);
        g.drawString(instruction, instructionX + 2, instructionY + 2); // Bayangan
        g.setColor(Color.YELLOW);
        g.drawString(instruction, instructionX, instructionY); // Teks utama
    }
    
    

    public void draw(Graphics g) {
        currentBackgroundIndex = (int) (score / 5) % backgroundImgs.length;

        g.drawImage(backgroundImgs[currentBackgroundIndex], 0, 0, this.boardWidth, this.boardHeight, null);

        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        if (showMainMenu) return;

        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Jika SPACE atau J ditekan
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_J) {
            if (showMainMenu) {
                showMainMenu = false; // Menghilangkan menu utama
                return;
            }

            velocityY = -9;

            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
