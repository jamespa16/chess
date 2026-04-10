package controllers;

import java.util.Scanner;

public class ControllerHelper {
    public static boolean tryAgain(Scanner scanner) {
        System.out.print("try again? [y/n] >> ");
        String tryAgain = scanner.nextLine().trim();
        switch (tryAgain) {
            case "y", "yes" -> {
                return true;
            }
        }
        return false;
    }
}