package hu.ottone.ntaglib;

/**
 * Created by richardbodai on 5/26/16.
 */
public class PasswordLengthException extends Exception {

    PasswordLengthException() {

    }

    @Override
    public String getMessage() {
        return super.getMessage() + "Password length must be 4 bytes";
    }
}

