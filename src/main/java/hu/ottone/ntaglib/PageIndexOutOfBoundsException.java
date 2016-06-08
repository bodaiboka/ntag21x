package hu.ottone.ntaglib;

/**
 * Created by richardbodai on 5/25/16.
 */
public class PageIndexOutOfBoundsException extends Exception {

    String message;

    PageIndexOutOfBoundsException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n" + message;
    }
}
