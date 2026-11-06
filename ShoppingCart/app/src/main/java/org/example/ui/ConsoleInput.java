package org.example.ui;

import java.io.Reader;
import java.util.Locale;
import java.util.Scanner;
import org.example.common.StoreException;

public final class ConsoleInput {
    private final Scanner scanner;


    public ConsoleInput(Reader reader) {
        this.scanner = new Scanner(reader);
    }


    public String text(String prompt) {
        System.out.println(prompt + ":");
        return scanner.nextLine().trim();
    }


    public int integer(String prompt) {
        try {
            return Integer.parseInt(text(prompt));
        } catch (NumberFormatException exception) {
            throw new StoreException("Client Error: Enter a whole number.");
        }
    }


    public int choice(String prompt, int count) {
        int selected = integer(prompt);
        
        if (selected < 1 || selected > count) {
            throw new StoreException("Client Error: Choose a number from 1 to " + count + ".");
        }
        return selected - 1;
    }


    public boolean confirm(String prompt) {
        String answer = text(prompt + " [y/n]").toLowerCase(Locale.ROOT);

        if (answer.equals("y") || answer.equals("yes")) {
            return true;
        }

        if (answer.equals("n") || answer.equals("no")) {
            return false;
        }
        
        throw new StoreException("Client Error: Enter y or n. Nothing was submitted.");
    }

}
