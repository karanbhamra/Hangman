package hangman;
/*
    Purpose: To read a text file containing words and be able to give a random word until word list is empty.
 */

import java.io.File;
import java.util.Collections;
import java.util.Scanner;
import java.util.Stack;

/*
    Usage:
        WordLoader words = new WordLoader("resources/words.txt");

        while (!words.isEmpty()) {
            System.out.println(words.getNewWord());
        }
 */

public class WordsLoader {

    File wordsFile;
    String fileName;
    Stack<String> words;

    public WordsLoader(String fileName) {
        this.fileName = fileName;

        loadWords(fileName);

        shuffleWordList();

    }

    private void shuffleWordList() {
        Collections.shuffle(words);
    }

    public void loadWords(String fileName) {
        try {
            wordsFile = new File(fileName);

            Scanner sc = new Scanner(wordsFile);

            words = new Stack<String>();

            while (sc.hasNextLine()) {
                words.add(sc.nextLine());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public String getNewWord() {

        if (words.empty()) {

            loadWords(fileName);
            shuffleWordList();
        }

        return words.pop();
    }

    public boolean isEmpty() {
        return words.empty();
    }
}
