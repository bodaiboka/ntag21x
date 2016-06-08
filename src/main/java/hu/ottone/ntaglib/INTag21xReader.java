package hu.ottone.ntaglib;

import android.nfc.tech.NfcA;

import java.io.IOException;


/**
 * Created by richardbodai on 5/20/16.
 */
public interface INTag21xReader {

    void setTag(NfcA tag);

    void initTag(Ntag21xConfig config) throws IOException;
    void initTag(Ntag21xConfig config, INTagCallback callback) throws IOException;

    byte[] authenticate(byte[] password) throws IOException;
    byte[] authenticate(String password) throws IOException;
    byte[] authenticate(byte[] password, INTagCallback callback) throws IOException;
    byte[] authenticate(String password, INTagCallback callback) throws IOException;

    byte[] read(byte address) throws IOException;
    byte[] read(byte address, INTagCallback callback) throws IOException;

    byte[] fastRead(byte startAddress, byte EndAddress) throws IOException;
    byte[] fastRead(byte startAddress, byte EndAddress, INTagCallback callback) throws IOException;

    byte[] write(byte address, byte[] data) throws IOException;
    byte[] write(byte address, byte[] data, INTagCallback callback) throws IOException;

    byte[] writeByteArray(byte address, byte[] data) throws IOException;
    byte[] writeByteArray(byte address, byte[] data, INTagCallback callback) throws IOException;

    byte[] writeMessage(byte address, String data) throws IOException;
    byte[] writeMessage(byte address, String data, INTagCallback callback) throws IOException;

    byte[] initPassword(String password) throws IOException;
    byte[] initPassword(byte[] password) throws IOException;
    byte[] initPassword(String password, INTagCallback callback) throws IOException;
    byte[] initPassword(byte[] password, INTagCallback callback) throws IOException;

    byte[] setPassword(String password) throws IOException;
    byte[] setPassword(byte[] password) throws IOException;
    byte[] setPassword(String password, INTagCallback callback) throws IOException;
    byte[] setPassword(byte[] password, INTagCallback callback) throws IOException;

    byte[] setAuth0(byte value) throws IOException;
    byte[] setAuth0(byte value, INTagCallback callback) throws IOException;

    byte[] format() throws IOException;
    byte[] format(INTagCallback callback) throws IOException;

    byte[] setLockBits(byte[] lockBits) throws IOException;
    byte[] setLockBits(byte[] lockBits, INTagCallback callback) throws IOException;







}
