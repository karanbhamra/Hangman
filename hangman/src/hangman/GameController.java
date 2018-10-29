package hangman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class GameController {

    private final ExecutorService executorService;
    private final Game game;

    public GameController(Game game) {
        this.game = game;
        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @FXML
    private Pane board;
    @FXML
    private Label statusLabel;
    @FXML
    private Label enterALetterLabel;
    @FXML
    private TextField textField;

    ArrayList<Integer> letterXPositions;
    int letterYPosition;
    int lettersDisplayed;

    ArrayList<Shape> bodyParts;
    ArrayList<String> keysPressed;

    int guessStartX = 10;
    int guessStartY = 80;

    public void initialize() throws IOException {
        System.out.println("in initialize");
        setupHangman();
        addTextBoxListener();
        setUpStatusLabelBindings();
        drawPole();
        drawWordLines(game.getChosenWord());
        drawGuessesLabel();
        lettersDisplayed = 0;
        keysPressed = new ArrayList<String>();
    }

    private void addTextBoxListener() {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (newValue.length() > 0) {

                    addGuessToScreen(newValue);
                    boolean found = false;

                    // run make move for as many times a character appears in the word
                    for (int i = 0; i < game.getChosenWord().length(); i++) {
                        if (game.getChosenWord().charAt(i) == newValue.charAt(0)) {
                            game.makeMove(newValue);
                            found = true;
                        }
                    }

                    if (!found) {
                        game.makeMove(newValue);
                    }
                    textField.clear();

                    checkLetterInWord(newValue);
                }


                if (game.getGameStatus() == Game.GameStatus.BAD_GUESS) {

                    switch (game.getBadMoves()) {
                        case 0:
                            addToBody(drawHead(40));
                            break;
                        case 1:
                            addToBody(drawTorso(100));
                            break;
                        case 2:
                            addToBody(drawLeftArm());
                            break;
                        case 3:
                            addToBody(drawRightArm());
                            break;
                        case 4:
                            addToBody(drawLeftLeg());
                            break;
                    }
                }

                if (game.getGameStatus() == Game.GameStatus.WON || game.getGameStatus() == Game.GameStatus.GAME_OVER) {

                    textField.setDisable(true);
                    if (game.getGameStatus() == Game.GameStatus.GAME_OVER) {
                        addToBody(drawRightLeg());
                    }

                }

                System.out.println("Wrong moves: " + game.getBadMoves());
            }
        });
    }

    private void addGuessToScreen(String guess) {
        keysPressed.add(guess);

        for (String k : keysPressed) {
            System.out.println(k);
        }

        int amountOfApple = Collections.frequency(keysPressed, guess);
        if (amountOfApple < 2) {
            Text t = new Text(guess.toUpperCase());
            t.setFont(new Font(30));
            t.setX(guessStartX);
            t.setY(guessStartY);

            if (game.getChosenWord().contains(guess)) {
                t.setFill(Color.WHITE);
            } else {
                t.setFill(Color.RED);
            }

            guessStartX += 35;
            addToBody(t);
        }

    }

    public void checkLetterInWord(String letter) {

        char charLetter = letter.charAt(0);
        ArrayList<Integer> letterPositions = new ArrayList<Integer>();

        // add the index positions of all the found char
        for (int i = 0; i < game.getChosenWord().length(); i++) {
            if (game.getChosenWord().charAt(i) == charLetter) {
                letterPositions.add(i);
            }
        }

        // loop through the index positions, find the x position at that location and display char
        for (int i = 0; i < letterPositions.size(); i++) {

            Text t = new Text(letter.toUpperCase());
            t.setFont(new Font(48));
            t.setX(letterXPositions.get(letterPositions.get(i)));
            t.setY(letterYPosition);
            t.setTextAlignment(TextAlignment.CENTER);
            addToBody(t);
            lettersDisplayed++;
        }
    }

    private void setUpStatusLabelBindings() {

        System.out.println("in setUpStatusLabelBindings");
        statusLabel.textProperty().bind(Bindings.format("%s", game.gameStatusProperty()));
        enterALetterLabel.textProperty().bind(Bindings.format("%s", "Enter a letter:"));
		/*	Bindings.when(
					game.currentPlayerProperty().isNotNull()
			).then(
				Bindings.format("To play: %s", game.currentPlayerProperty())
			).otherwise(
				""
			)
		);
		*/
    }

    private Shape drawHead(int radius) {
        Circle c = new Circle();
        c.setCenterX(250);
        c.setCenterY(300);

        c.setRadius(radius);
        return c;
    }

    private Shape drawTorso(int height) {
        Line l = new Line();
        l.setStartY(340);
        l.setEndY(l.getStartY() + height);
        l.setStartX(250);
        l.setEndX(250);
        l.setStrokeWidth(10);

        return l;
    }

    private Shape drawRightArm() {
        Line l = new Line();
        l.setStrokeWidth(10);
        l.setStartY(400);
        l.setEndY(340);
        l.setStartX(255);
        l.setEndX(300);

        return l;
    }


    private Shape drawLeftArm() {
        Line l = new Line();
        l.setStrokeWidth(10);
        l.setStartY(400);
        l.setEndY(340);
        l.setStartX(245);
        l.setEndX(245 - 45);

        return l;
    }

    private Shape drawRightLeg() {
        Line l = new Line();
        l.setStrokeWidth(10);
        l.setStartX(255);
        l.setEndX(255 + 45);
        l.setStartY(445);
        l.setEndY(500);

        return l;
    }

    private Shape drawLeftLeg() {
        Line l = new Line();
        l.setStrokeWidth(10);
        l.setStartX(245);
        l.setEndX(245 - 45);
        l.setStartY(445);
        l.setEndY(500);

        return l;
    }

    private void drawGuessesLabel() {
        Text t = new Text("Guesses");
        t.setFont(new Font(24));
        t.setX(190);
        t.setY(25);
        addToBody(t);
    }

    private void addToBody(Shape part) {
        board.getChildren().add(part);
    }


    // This method will calculate the position of each letter underline and position it centered and draw the lines
    private void drawWordLines(String word) {

        letterXPositions = new ArrayList<Integer>();
        int numChars = word.length();

        int width = 30;
        int gap = 10;

        int screensize = 500;
        int totalStringWidth = numChars * (width + gap);

        int startPos = (screensize - totalStringWidth) / 2;

        int startX = startPos;
        int ypos = 648;
        int offset = 30;

        letterYPosition = 640;

        ArrayList<Line> lines = new ArrayList<Line>();

        for (int i = 0; i < numChars; i++) {

            letterXPositions.add(new Integer(((startX + (startX + width)) / 2) - 15));
            Line temp = new Line(startX, ypos, startX + width, ypos);
            temp.setStrokeWidth(5);
            lines.add(i, temp);
            startX += offset + gap;

        }

        for (Line l : lines) {
            addToBody(l);
        }

    }


    private void setupHangman() {

        bodyParts = new ArrayList<Shape>();

        bodyParts.add(drawHead(40));
        bodyParts.add(drawTorso(100));
        bodyParts.add(drawRightArm());
        bodyParts.add(drawLeftArm());
        bodyParts.add(drawRightLeg());
        bodyParts.add(drawLeftLeg());

    }

    private void drawPole() {

        Line vert = new Line();
        vert.setStrokeWidth(10);
        vert.setStartX(100);
        vert.setEndX(100);
        vert.setStartY(550);
        vert.setEndY(250);

        Line bot = new Line();
        bot.setStrokeWidth(10);
        bot.setStartX(100 - 25);
        bot.setEndX(100 + 25);
        bot.setStartY(550);
        bot.setEndY(550);

        Line top = new Line();
        top.setStrokeWidth(10);
        top.setStartX(100);
        top.setEndX(250);
        top.setStartY(250);
        top.setEndY(250);

        Line noose = new Line();
        noose.setStrokeWidth(10);
        noose.setStartY(250);
        noose.setEndY(275);
        noose.setStartX(250);
        noose.setEndX(250);

        addToBody(vert);
        addToBody(bot);
        addToBody(top);
        addToBody(noose);
    }

    @FXML
    private void newHangman() {
        board.getChildren().clear();
        game.reset();
        textField.setDisable(false);
        lettersDisplayed = 0;
        drawWordLines(game.getChosenWord());
        drawPole();
        drawGuessesLabel();
        guessStartX = 10;
        guessStartY = 50;
        setupHangman();
        keysPressed = new ArrayList<String>();
    }

    @FXML
    private void quit() {
        board.getScene().getWindow().hide();
    }

}