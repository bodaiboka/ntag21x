package hu.ottone.ntaglib;

import java.util.HashMap;

/**
 * Created by richardbodai on 5/25/16.
 */
public class Ntag21xConfig {

    public static final byte DEFAULT_FORMAT_START_ADDRESS = 16;
    public static final byte CONF_LOCK_STATIC_PAGE_BIT = 1;
    public static final byte CONF_SET_AUTH_PAGE_BIT = 2;
    public static final byte CONF_SET_PASSWORD_BIT = 4;
    public static final byte CONF_INIT_DATA_BIT = 8;
    public static final byte CONF_INIT_MESSAGE = 16;
    public static final byte CONF_FORMAT = 32;
    public static final byte CONF_AUTH_BIT = 64;

    private byte configBits = 0;
    private byte[] mPassword = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    private byte auth0;
    private byte formatStartAddress = DEFAULT_FORMAT_START_ADDRESS;
    private byte[] lockBytes = new byte[] {0, 0};
    private short lockBytesShort;
    private String authPass;
    private HashMap<Byte, byte[]> initDataArray = new HashMap<>();
    private HashMap<Byte, String> initMessageArray = new HashMap<>();

    public void authenticate(String pass) {
        authPass = pass;
        setConfigBits(CONF_AUTH_BIT);
    }

    public void lockStaticPage(int page) throws PageIndexOutOfBoundsException {
        int pageAddress = 1;
        if ((page < 3) || (page > 15)) {
            throw new PageIndexOutOfBoundsException("Page number must be in the range [3, 15]");
        }
        pageAddress = pageAddress << page;
        lockBytesShort = (short)(lockBytesShort | pageAddress);
        short lockbyte0Mask = (short)0x00FF;
        short lockbyte1Mask = (short)0xFF00;
        lockBytes[0] = (byte)(lockBytesShort & lockbyte0Mask);
        lockBytes[1] = (byte)((lockBytesShort & lockbyte1Mask) >> 8);
        setConfigBits(CONF_LOCK_STATIC_PAGE_BIT);
    }

    public void lockStaticPageBlock(byte startPage, byte endPage) throws PageIndexOutOfBoundsException {
        if ((startPage > 15) || (startPage < 3) || (endPage > 15) || (endPage < 3)) {
            throw new PageIndexOutOfBoundsException("Page number must be in the range [3, 15]");
        }
        if (startPage <= endPage) {
            for (int i = startPage; i <= endPage; i++) {
                lockStaticPage(i);
            }
        }
        setConfigBits(CONF_LOCK_STATIC_PAGE_BIT);
    }

    public void setAuthPage(byte page) throws PageIndexOutOfBoundsException {
        this.auth0 = page;
        setConfigBits(CONF_SET_AUTH_PAGE_BIT);
    }

    public void setPassword(String password) throws PasswordLengthException {
        byte[] passwordBytes = ByteOperator.hexToBytes(password);
        if (passwordBytes.length != 4) {
            throw new PasswordLengthException();
        }
        for (int i = 0; i < 4; i++) {
            mPassword[i] = passwordBytes[i];
        }
        setConfigBits(CONF_SET_PASSWORD_BIT);
    }

    public void setPassword(byte[] password) throws PasswordLengthException {
        if (password.length != 4) {
            throw new PasswordLengthException();
        }
        for (int i = 0; i < 4; i++) {
            mPassword[i] = password[i];
        }
        setConfigBits(CONF_SET_PASSWORD_BIT);
    }

    public void initData(byte page, byte[] data) {
        initDataArray.put(page, data);
        setConfigBits(CONF_INIT_DATA_BIT);
    }

    public void initMessage(byte page, String message) {
        initMessageArray.put(page, message);
        setConfigBits(CONF_INIT_MESSAGE);
    }

    public void enableFormat() {
        setConfigBits(CONF_FORMAT);
    }

    public void disableFormat() {
        cancelConfigBits(CONF_FORMAT);
    }

    public byte getConfigBits() {
        return configBits;
    }

    public void setConfigBits(byte configBits) {
        this.configBits = (byte)(this.configBits | configBits);
    }

    public void cancelConfigBits(byte configBits) {
        this.configBits = (byte)(this.configBits & (~configBits));
    }

    public byte[] getmPassword() {
        return mPassword;
    }

    public void setmPassword(byte[] mPassword) {
        this.mPassword = mPassword;
    }

    public byte getAuth0() {
        return this.auth0;
    }

    public byte[] getLockBytes() {
        return lockBytes;
    }

    public void setLockBytes(byte[] lockBytes) {
        this.lockBytes = lockBytes;
    }

    public HashMap<Byte, byte[]> getInitDataArray() {
        return initDataArray;
    }

    public void setInitDataArray(HashMap<Byte, byte[]> initDataArray) {
        this.initDataArray = initDataArray;
    }

    public HashMap<Byte, String> getInitMessageArray() {
        return initMessageArray;
    }

    public void setInitMessageArray(HashMap<Byte, String> initMessageArray) {
        this.initMessageArray = initMessageArray;
    }

    public String getAuthPass() {
        return authPass;
    }

    public void setFormatStartAddress(byte address) {
        formatStartAddress = address;
    }

    public byte getFormatStartAddress() {
        return formatStartAddress;
    }
}
