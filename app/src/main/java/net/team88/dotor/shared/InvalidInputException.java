package net.team88.dotor.shared;

public class InvalidInputException extends Exception {

    public enum Type {
        REQUIRED,
        INVALID_FORMAT
    }

    private Type mType;


    public InvalidInputException(Type type) {
        super("Invalid User Input Exception");
        mType = type;
    }

    public InvalidInputException(Type type, String message) {
        super(message);
        mType = type;
    }

    public Type getType() {
        return mType;
    }
}
