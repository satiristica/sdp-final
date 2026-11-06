package org.example;

import java.io.InputStreamReader;
import org.example.ui.ConsoleStore;

public final class App {
    public static void main(String[] args) {

        new ConsoleStore(
            new StoreApplication(),
            new InputStreamReader(System.in))
            .run();
    }
}
