package Application.Controllers;

import Application.Database.DatabaseManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GameController {

    @FXML private Canvas gameCanvas;
    @FXML private Button startButton;
    @FXML private Label scoreLabel;
    @FXML private Label gameOverLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label highScoreLabel;
    @FXML private Button playAgainButton;
    @FXML private Button exitButton;
    @FXML private Button saveScoreButton;

    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private double birdX = 200;
    private double birdY = 400;
    private double birdRadius = 20;
    private double velocityY = 0;
    private double gravity = 0.5;
    private double jumpForce = -7;
    private List<Pipe> pipes = new ArrayList<>();
    private double pipeSpeed = 2;
    private int score = 0;
    private int highScore = 0;
    private boolean gameRunning = false;
    private Random rand = new Random();
    private DatabaseManager dbManager = new DatabaseManager();

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        highScore = dbManager.getHighestScore();
        updateScoreLabel();

        startButton.setOnAction(e -> startGame());
        playAgainButton.setOnAction(e -> resetGame());
        exitButton.setOnAction(e -> System.exit(0));
        saveScoreButton.setOnAction(e -> promptForNameAndSaveScore());

        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (gameRunning) {
                    velocityY = jumpForce;
                } else if (!gameOverLabel.isVisible()) {
                    startGame();
                }
            }
        });

        drawStartScreen();
    }

    private void drawStartScreen() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(20));
        gc.fillText("Press SPACE or click START to play", 130, 250);
    }

    private void startGame() {
        startButton.setVisible(false);
        gameRunning = true;

        birdX = 150;
        birdY = 400;
        velocityY = 0;
        pipes.clear();
        score = 0;
        updateScoreLabel();
        spawnInitialPipes();

        gameLoop = new AnimationTimer() {
            private long lastPipeTime = 0;

            @Override
            public void handle(long now) {
                update();
                render();
                if (now - lastPipeTime > 2_000_000_000L) {
                    addPipe();
                    lastPipeTime = now;
                }
            }
        };

        gameLoop.start();
        gameCanvas.requestFocus();
    }

    private void resetGame() {
        gameOverLabel.setVisible(false);
        finalScoreLabel.setVisible(false);
        highScoreLabel.setVisible(false);
        playAgainButton.setVisible(false);
        exitButton.setVisible(false);
        saveScoreButton.setVisible(false);

        startButton.setVisible(true);
        drawStartScreen();
    }

    private void promptForNameAndSaveScore() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Score");
        dialog.setHeaderText("Enter your name to save your score of " + score);
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                dbManager.saveScore(name.trim(), score);
                highScore = dbManager.getHighestScore();
                highScoreLabel.setText("Highest Score: " + highScore);
                saveScoreButton.setVisible(false);
            }
        });
    }

    private void update() {
        velocityY += gravity;
        birdY += velocityY;

        if (birdY > gameCanvas.getHeight() - birdRadius) {
            birdY = gameCanvas.getHeight() - birdRadius;
            endGame();
        }
        if (birdY < birdRadius) {
            birdY = birdRadius;
            velocityY = 0;
        }

        Iterator<Pipe> iter = pipes.iterator();
        while (iter.hasNext()) {
            Pipe pipe = iter.next();
            pipe.x -= pipeSpeed;

            if (pipe.collidesWith(birdX, birdY, birdRadius)) {
                endGame();
                return;
            }

            if (!pipe.passed && pipe.x + pipe.width < birdX) {
                if (pipe.isTop) {
                    score++;
                    updateScoreLabel();
                }
                pipe.passed = true;
            }

            if (pipe.x + pipe.width < 0) {
                iter.remove();
            }
        }
    }

    private void render() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.SKYBLUE);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.YELLOW);
        gc.fillOval(birdX - birdRadius, birdY - birdRadius, birdRadius * 2, birdRadius * 2);

        gc.setFill(Color.GREEN);
        for (Pipe pipe : pipes) {
            if (pipe.isTop) {
                gc.fillRect(pipe.x, 0, pipe.width, pipe.height);
            } else {
                gc.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
            }
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    private void spawnInitialPipes() {
        pipes.clear();
        addPipe();
        addPipe();
    }

    private void addPipe() {
        double gap = 250;
        double width = 60;
        double heightTop = 100 + rand.nextInt(200);
        double heightBottom = gameCanvas.getHeight() - heightTop - gap;

        pipes.add(new Pipe(gameCanvas.getWidth(), 0, width, heightTop, true));
        pipes.add(new Pipe(gameCanvas.getWidth(), gameCanvas.getHeight() - heightBottom, width, heightBottom, false));
    }

    private void endGame() {
        gameRunning = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        highScore = dbManager.getHighestScore();

        gameOverLabel.setVisible(true);
        finalScoreLabel.setText("Your Score: " + score);
        finalScoreLabel.setVisible(true);
        highScoreLabel.setText("Highest Score: " + highScore);
        highScoreLabel.setVisible(true);

        playAgainButton.setVisible(true);
        exitButton.setVisible(true);
        saveScoreButton.setVisible(true);
    }

    private static class Pipe {
        double x, y;
        double width, height;
        boolean isTop;
        boolean passed = false;

        Pipe(double x, double y, double width, double height, boolean isTop) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isTop = isTop;
        }

        boolean collidesWith(double bx, double by, double radius) {
            double closestX = clamp(bx, x, x + width);
            double closestY = clamp(by, y, y + height);
            double dx = bx - closestX;
            double dy = by - closestY;
            return (dx * dx + dy * dy) < (radius * radius);
        }

        private double clamp(double val, double min, double max) {
            return Math.max(min, Math.min(max, val));
        }
    }
}