package hu.ottone.ntaglib;

/**
 * Created by richardbodai on 5/30/16.
 */
public interface INTagCallback {
    void commandStart();
    void commandDone();
    void commandError();
}
