package Application.Controllers;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML private Canvas gameCanvas;
    @FXML private Button startButton;
    @FXML private StackPane gameContainer;
    @FXML private VBox gameOverMenu;
    @FXML private Button playAgainButton;
    @FXML private Button exitButton;

    private Image birdImage, bgImage, topPipeImage, bottomPipeImage;

    private double birdX = 100, birdY = 250, birdVelocity = 0;
    private final double gravity = 0.5, jumpForce = -7, pipeSpeed = 2;
    private boolean gameRunning = false;
    private AnimationTimer gameLoop;
    private int score = 0;
    private long lastPipeTime = 0;
    private final int pipeGap = 150, pipeFrequency = 2000; // ms
    private ArrayList<Pipe> pipes = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Responsive canvas
        gameCanvas.widthProperty().bind(gameContainer.widthProperty());
        gameCanvas.heightProperty().bind(gameContainer.heightProperty());

        birdImage = loadImage("/Application/Fxmls/flappybird.png");
        bgImage = loadImage("/Application/Fxmls/flappybirdbg.png");
        topPipeImage = loadImage("/Application/Fxmls/toppipe.png");
        bottomPipeImage = loadImage("/Application/Fxmls/bottompipe.png");

        renderInitialScreen();

        startButton.setOnAction(e -> startGame());
        playAgainButton.setOnAction(e -> startGame());
        exitButton.setOnAction(e -> System.exit(0));

        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
    }

    private Image loadImage(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Image not found: " + path);
                return null;
            }
            return new Image(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void renderInitialScreen() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        if (bgImage != null)
            gc.drawImage(bgImage, 0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        if (birdImage != null)
            gc.drawImage(birdImage, birdX, birdY, 50, 50);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font(24));
        gc.fillText("Click Start or Press SPACE", 50, gameCanvas.getHeight() / 2);
    }

    private void startGame() {
        gameRunning = true;
        startButton.setVisible(false);
        gameOverMenu.setVisible(false);
        birdY = 250;
        birdVelocity = 0;
        score = 0;
        pipes.clear();
        gameCanvas.requestFocus();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameRunning) return;
                updateGame(now);
                renderGame(gameCanvas.getGraphicsContext2D());
            }
        };
        gameLoop.start();
    }

    private void updateGame(long now) {
        birdVelocity += gravity;
        birdY += birdVelocity;

        if (now - lastPipeTime > pipeFrequency * 1_000_000L) {
            createPipes();
            lastPipeTime = now;
        }

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x -= pipeSpeed;

            if (!pipe.passed && pipe.x + 50 < birdX) {
                pipe.passed = true;
                score++;
            }

            if (pipe.x < -100) {
                pipes.remove(i--);
            }
        }

        if (birdY < 0 || birdY > gameCanvas.getHeight() - 50) gameOver();

        for (Pipe pipe : pipes) {
            if (checkCollision(pipe)) {
                gameOver();
                break;
            }
        }
    }

    private void createPipes() {
        double h = gameCanvas.getHeight();
        int openingY = random.nextInt((int) (h - pipeGap - 200)) + 100;

        pipes.add(new Pipe(gameCanvas.getWidth(), 0, openingY, true));
        pipes.add(new Pipe(gameCanvas.getWidth(), openingY + pipeGap, h - (openingY + pipeGap), false));
    }

    private boolean checkCollision(Pipe pipe) {
        double birdRight = birdX + 50, birdBottom = birdY + 50;
        double pipeRight = pipe.x + 50, pipeBottom = pipe.y + pipe.height;

        return birdX < pipeRight && birdRight > pipe.x &&
                birdY < pipeBottom && birdBottom > pipe.y;
    }

    private void renderGame(GraphicsContext gc) {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        if (bgImage != null)
            gc.drawImage(bgImage, 0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        for (Pipe pipe : pipes) {
            if (pipe.isTop)
                gc.drawImage(topPipeImage, pipe.x, pipe.y, 50, pipe.height);
            else
                gc.drawImage(bottomPipeImage, pipe.x, pipe.y, 50, pipe.height);
        }

        if (birdImage != null)
            gc.drawImage(birdImage, birdX, birdY, 50, 50);

        gc.setFill(Color.YELLOW);
        gc.setFont(new Font("Arial", 32));
        gc.fillText("Score: " + score, 20, 40);
    }

    private void gameOver() {
        gameRunning = false;
        if (gameLoop != null) gameLoop.stop();

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(new Font(32));
        gc.fillText("Game Over!", gameCanvas.getWidth() / 2 - 90, gameCanvas.getHeight() / 2 - 30);
        gc.fillText("Score: " + score, gameCanvas.getWidth() / 2 - 60, gameCanvas.getHeight() / 2 + 10);

        gameOverMenu.setVisible(true);
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            if (!gameRunning) startGame();
            else birdVelocity = jumpForce;
        }
    }

    private static class Pipe {
        double x, y, height;
        boolean isTop, passed = false;

        Pipe(double x, double y, double height, boolean isTop) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.isTop = isTop;
        }
    }
}
