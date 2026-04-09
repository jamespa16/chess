package controllers;

import java.util.Scanner;

public class ControllerHelper {
	public static boolean tryAgain(Scanner scanner) {
		System.out.printf("try again? [y/n] >> ");
		String tryAgain = scanner.nextLine().trim();
		switch (tryAgain) {
			case "y":
			case "yes":
				return true;
		}
		return false;
	}
}