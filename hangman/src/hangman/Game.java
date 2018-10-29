package hangman;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private String answer;
    private String tmpAnswer;
    private String[] letterAndPosArray;
    private String[] words;
    private int badMoves;
    private int index;
    private final ReadOnlyObjectWrapper<GameStatus> gameStatus;
    private ObjectProperty<Boolean> gameState = new ReadOnlyObjectWrapper<Boolean>();
    private String recentChar;

    // file to load words from
    private final String fileName = "resources/words.txt";

    // contains a stack with randomized words
    WordsLoader wordsList;

    public enum GameStatus {
        GAME_OVER {
            @Override
            public String toString() {
                return "Game over!";
            }
        },
        BAD_GUESS {
            @Override
            public String toString() {
                return "Bad guess...";
            }
        },
        GOOD_GUESS {
            @Override
            public String toString() {
                return "Good guess!";
            }
        },
        WON {
            @Override
            public String toString() {
                return "You won!";
            }
        },
        OPEN {
            @Override
            public String toString() {
                return "Game on, let's go!";
            }
        },
        DUPLICATE {
            @Override
            public String toString() {
                return "Duplicate letter!";
            }
        }
    }

    public Game() {

        // load our words
        wordsList = new WordsLoader(fileName);

        gameStatus = new ReadOnlyObjectWrapper<GameStatus>(this, "gameStatus", GameStatus.OPEN);
        gameStatus.addListener(new ChangeListener<GameStatus>() {
            @Override
            public void changed(ObservableValue<? extends GameStatus> observable,
                                GameStatus oldValue, GameStatus newValue) {
                if (gameStatus.get() != GameStatus.OPEN) {
                    log("in Game: in changed");
                    //currentPlayer.set(null);
                }
            }

        });
        reset();
    }


    private void createGameStatusBinding() {
        List<Observable> allObservableThings = new ArrayList<>();
        ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
            {
                super.bind(gameState);
            }

            @Override
            public GameStatus computeValue() {
                log("in computeValue");
                GameStatus check = checkForWinner(index);
                if (check != null) {
                    return check;
                }

                if (tmpAnswer.trim().length() == 0) {
                    log("new game");
                    return GameStatus.OPEN;
                } else if (index != -1) {
                    log("good guess");
                    return GameStatus.GOOD_GUESS;
                } else {
                    //badMoves++;
                    log("bad guess");
                    return GameStatus.BAD_GUESS;
                    //printHangman();
                }
            }
        };
        gameStatus.bind(gameStatusBinding);
    }

    public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
        return gameStatus.getReadOnlyProperty();
    }

    public GameStatus getGameStatus() {
        return gameStatus.get();
    }

    public int getBadMoves() {
        return badMoves;
    }

    private void setRandomWord() {
        //int idx = (int) (Math.random() * words.length);
        //answer = "apple";//words[idx].trim(); // remove new line character
        //if (!wordsList.isEmpty()) {
        answer = wordsList.getNewWord();
        //}

        log("Random word chosen is: " + answer);

    }

    public String getChosenWord() {

        return answer;
    }

    private void prepTmpAnswer() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < answer.length(); i++) {
            sb.append(" ");
        }
        tmpAnswer = sb.toString();
    }

    private void prepLetterAndPosArray() {
        letterAndPosArray = new String[answer.length()];
        for (int i = 0; i < answer.length(); i++) {
            letterAndPosArray[i] = answer.substring(i, i + 1);
        }
    }

    private int getValidIndex(String input) {
        int index = -1;
        for (int i = 0; i < letterAndPosArray.length; i++) {
            if (letterAndPosArray[i].equals(input)) {
                index = i;
                letterAndPosArray[i] = "";
                break;
            }
        }
        return index;
    }

    private int update(String input) {
        int index = getValidIndex(input);
        if (index != -1) {
            StringBuilder sb = new StringBuilder(tmpAnswer);
            sb.setCharAt(index, input.charAt(0));
            tmpAnswer = sb.toString();
        }

        System.out.println("Temp answer: " + tmpAnswer);

        return index;

    }


    public void makeMove(String letter) {
        recentChar = letter;
        log("\nin makeMove: " + letter);
        index = update(letter);

        // this will toggle the state of the game
        gameState.setValue(!gameState.getValue());
    }

    public void reset() {

        setRandomWord();
        prepTmpAnswer();
        prepLetterAndPosArray();

        gameState.setValue(false); // initial state
        createGameStatusBinding();
        badMoves = -1;

    }

    public int numOfTries() {
        return 4;
    }

    public static void log(String s) {
        System.out.println(s);
    }

    private GameStatus checkForWinner(int status) {
        log("in checkForWinner");

        if (badMoves >= numOfTries()) {
            log("game over");
            return GameStatus.GAME_OVER;
        } else if (status == -1) {
            log("bad guess");
            if (!tmpAnswer.contains(recentChar)) {
                badMoves++;
                return GameStatus.BAD_GUESS;
            }
            return GameStatus.DUPLICATE;
        } else if (tmpAnswer.equals(answer)) {
            log("won");
            return GameStatus.WON;
        } else {
            return null;
        }
    }
}
