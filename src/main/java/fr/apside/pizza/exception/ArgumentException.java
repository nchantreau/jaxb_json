package fr.apside.pizza.exception;

public class ArgumentException extends Exception {

    public ArgumentException(String message, Throwable cause) {
	super(message, cause);
    }

    public ArgumentException(String message) {
	super(message);
    }

}
