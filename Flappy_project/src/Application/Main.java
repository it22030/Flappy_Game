package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // Debugging line to check if FXML is found
            System.out.println(getClass().getResource("/Application/Fxmls/game_view.fxml"));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/Fxmls/game_view.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setTitle("Flappy Bird FXML");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
