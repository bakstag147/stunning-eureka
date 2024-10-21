import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Main extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Timer robotSpawner;
    private Timer cloudMover;
    private int heroX = 100;
    private int heroY = 200;
    private int velocityY = 0;
    private boolean jumping = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private List<Robot> robots = new ArrayList<>();
    private List<Cloud> clouds = new ArrayList<>();
    private List<BackgroundObject> backgroundObjects = new ArrayList<>();
    private int score = 0;
    private int spawnInterval = 2000;
    private int gameTime = 0;

    private Image heroImage;
    private Image robotImage;
    private Image cloudImage;
    private Image grass1Image;
    private Image grass2Image;
    private Image treeImage;
    private static final int HERO_WIDTH = 110;
    private static final int HERO_HEIGHT = 150;
    private static final int ROBOT_WIDTH = 90;
    private static final int ROBOT_HEIGHT = 90;
    private static final int CLOUD_WIDTH = 100;
    private static final int CLOUD_HEIGHT = 80;
    private static final int GROUND_Y = 350;
    private static final int GRASS_WIDTH = 30;
    private static final int GRASS_HEIGHT = 15;
    private static final int ROBOT_SPEED = 5;

    public Main() {
        heroImage = new ImageIcon("src/man23.png").getImage();
        robotImage = new ImageIcon("src/robot.gif").getImage();
        cloudImage = new ImageIcon("src/cloud.png").getImage();
        grass1Image = new ImageIcon("src/grass11.png").getImage();
        grass2Image = new ImageIcon("src/grass22.png").getImage();
        treeImage = new ImageIcon("src/tree11.png").getImage();

        initGame();

        timer = new Timer(20, this);
        timer.start();
        setFocusable(true);
        addKeyListener(this);
        initializeClouds();
        initializeBackgroundObjects();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Any Game");
        Main game = new Main();
        frame.add(game);
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void initGame() {
        robots.clear();
        clouds.clear();
        backgroundObjects.clear();
        heroX = 100;
        heroY = GROUND_Y - HERO_HEIGHT;
        velocityY = 0;
        jumping = false;
        moveLeft = false;
        moveRight = false;
        score = 0;
        spawnInterval = 2000;
        gameTime = 0;

        spawnRobots();
    }

    private void resetGame() {
        initGame();
        repaint();
    }

    private void spawnRobots() {
        if (robotSpawner != null) {
            robotSpawner.stop();
        }
        robotSpawner = new Timer(spawnInterval, e -> {
            robots.add(new Robot(
                    new Rectangle(800, GROUND_Y - ROBOT_HEIGHT + 4, ROBOT_WIDTH, ROBOT_HEIGHT), // Adjusted Y-coordinate
                    new Random().nextBoolean()
            ));
            gameTime += spawnInterval / 1000;
            if (spawnInterval > 200) {
                spawnInterval -= 100;
                if (spawnInterval < 200) {
                    spawnInterval = 200;
                }
                robotSpawner.setDelay(spawnInterval);
            }
        });
        robotSpawner.start();
    }

    private void initializeClouds() {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            int x = rand.nextInt(800);
            int y = rand.nextInt(150);
            clouds.add(new Cloud(x, y));
        }
        cloudMover = new Timer(20, e -> moveClouds());
        cloudMover.start();
    }

    private void initializeBackgroundObjects() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int type = rand.nextInt(3);
            int x = rand.nextInt(800);
            int y = GROUND_Y - getImageHeight(type) + ((type == 2) ? 4 : 0); // Adjusted Y-coordinate for trees
            int width = getImageWidth(type);
            int height = getImageHeight(type);
            if (type == 2) { // Adjust tree height and width randomly by +/- 40%
                width = (int) (width * (0.6 + rand.nextFloat() * 0.8));
                height = (int) (height * (0.6 + rand.nextFloat() * 0.8));
                y = GROUND_Y - height + 4;
            }
            backgroundObjects.add(new BackgroundObject(x, y, type, width, height));
        }
    }

    private void moveClouds() {
        Iterator<Cloud> it = clouds.iterator();
        while (it.hasNext()) {
            Cloud cloud = it.next();
            cloud.x -= cloud.speed;

            if (cloud.x + CLOUD_WIDTH < 0) {
                it.remove();
            }
        }

        while (clouds.size() < 5) {
            Random rand = new Random();
            int x = rand.nextInt(800) + 800;
            int y = rand.nextInt(150);
            clouds.add(new Cloud(x, y));
        }
    }

    private void moveBackgroundObjects() {
        Iterator<BackgroundObject> it = backgroundObjects.iterator();
        while (it.hasNext()) {
            BackgroundObject obj = it.next();
            int speed;
            if (obj.type == 2) {
                speed = (int) (ROBOT_SPEED * 0.9); // Trees move 10% slower
            } else if (obj.type == 0 || obj.type == 1) {
                speed = (int) (ROBOT_SPEED * 0.95); // Grass moves 5% slower
            } else {
                speed = ROBOT_SPEED;
            }
            obj.x -= speed;

            if (obj.x + obj.width < 0) {
                it.remove();
            }
        }

        while (backgroundObjects.size() < 10) {
            Random rand = new Random();
            int type = rand.nextInt(3);
            int x = rand.nextInt(800) + 800;
            int y = GROUND_Y - getImageHeight(type) + ((type == 2) ? 4 : 0); // Adjusted Y-coordinate for trees
            int width = getImageWidth(type);
            int height = getImageHeight(type);
            if (type == 2) { // Adjust tree height and width randomly by +/- 40%
                width = (int) (width * (0.6 + rand.nextFloat() * 0.8));
                height = (int) (height * (0.6 + rand.nextFloat() * 0.8));
                y = GROUND_Y - height + 4;
            }
            backgroundObjects.add(new BackgroundObject(x, y, type, width, height));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, getWidth(), getHeight());

        for (Cloud cloud : clouds) {
            g.drawImage(cloudImage, cloud.x, cloud.y, CLOUD_WIDTH, CLOUD_HEIGHT, this);
        }

        for (BackgroundObject obj : backgroundObjects) {
            Image img = getImage(obj.type);
            g.drawImage(img, obj.x, obj.y, obj.width, obj.height, this);
        }

        g.setColor(new Color(0, 100, 0));
        g.fillRect(0, GROUND_Y, getWidth(), 50);

        g.drawImage(heroImage, heroX, heroY, HERO_WIDTH, HERO_HEIGHT, this);

        for (Robot robot : robots) {
            g.drawImage(robotImage, robot.rect.x, robot.rect.y, ROBOT_WIDTH, ROBOT_HEIGHT, this);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        int scoreX = (getWidth() - fm.stringWidth("Score: " + score)) / 2;
        g.drawString("Score: " + score, scoreX, 30);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Time: " + gameTime + "s", 10, getHeight() - 20);
        g.drawString("Spawn Interval: " + spawnInterval + "ms", 10, getHeight() - 5);

        setLayout(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (moveLeft) {
            heroX -= 5;
        }
        if (moveRight) {
            heroX += 5;
        }
        if (jumping) {
            velocityY += 1;
            heroY += velocityY;
            if (heroY >= GROUND_Y - HERO_HEIGHT) {
                heroY = GROUND_Y - HERO_HEIGHT;
                jumping = false;
                velocityY = 0;
            }
        } else {
            if (heroY < GROUND_Y - HERO_HEIGHT) {
                velocityY += 1;
                heroY += velocityY;
                if (heroY > GROUND_Y - HERO_HEIGHT) {
                    heroY = GROUND_Y - HERO_HEIGHT;
                    velocityY = 0;
                }
            }
        }

        Iterator<Robot> it = robots.iterator();
        while (it.hasNext()) {
            Robot robot = it.next();
            robot.rect.x -= ROBOT_SPEED;

            if (robot.jumps && !robot.jumping) {
                robot.jumping = true;
                robot.velocityY = -15;
            }
            if (robot.jumping) {
                robot.velocityY += 1;
                robot.rect.y += robot.velocityY;
                if (robot.rect.y >= GROUND_Y - ROBOT_HEIGHT + 4) { // Adjusted Y-coordinate
                    robot.rect.y = GROUND_Y - ROBOT_HEIGHT + 4; // Adjusted Y-coordinate
                    robot.jumping = false;
                    robot.velocityY = 0;
                }
            }

            if (robot.rect.intersects(heroX, heroY, HERO_WIDTH, HERO_HEIGHT) && velocityY > 0) {
                it.remove();
                score += 1;
            } else if (robot.rect.x + robot.rect.width < 0) {
                it.remove();
                score -= 1;
            }
        }

        moveBackgroundObjects();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !jumping) {
            jumping = true;
            velocityY = -22;
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = false;
        }
    }

    private Image getImage(int type) {
        switch (type) {
            case 0:
                return grass1Image;
            case 1:
                return grass2Image;
            case 2:
                return treeImage;
            default:
                return null;
        }
    }

    private int getImageWidth(int type) {
        switch (type) {
            case 0:
            case 1:
                return GRASS_WIDTH;
            case 2:
                return treeImage.getWidth(this);
            default:
                return 0;
        }
    }

    private int getImageHeight(int type) {
        switch (type) {
            case 0:
            case 1:
                return GRASS_HEIGHT;
            case 2:
                return treeImage.getHeight(this);
            default:
                return 0;
        }
    }

    private static class Robot {
        Rectangle rect;
        boolean jumps;
        boolean jumping;
        int velocityY = 0;

        Robot(Rectangle rect, boolean jumps) {
            this.rect = rect;
            this.jumps = jumps;
            this.jumping = false;
        }
    }

    private static class Cloud {
        int x, y;
        double speed;

        Cloud(int x, int y) {
            this.x = x;
            this.y = y;
            Random rand = new Random();
            this.speed = 5 / 1.5 * (1 + (rand.nextInt(6) + 5) / 100.0);
        }
    }

    private static class BackgroundObject {
        int x, y;
        int type;
        int width, height;

        BackgroundObject(int x, int y, int type, int width, int height) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.width = width;
            this.height = height;
        }
    }
}