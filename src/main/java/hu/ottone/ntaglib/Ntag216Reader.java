package hu.ottone.ntaglib;

import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by richardbodai on 5/20/16.
 */
public class Ntag216Reader implements INTag21xReader {

    public final static byte CMD_READ = (byte)0x30;
    public final static byte CMD_FAST_READ = (byte)0x3A;
    public final static byte CMD_WRITE = (byte)0xA2;
    public final static byte CMD_PWD_AUTH = (byte)0x1B;
    public final static byte PWD_ADDRESS_216 = (byte)0xE5;
    public final static byte AUTH0_ADDRESS_216 = (byte)0xE3;
    public final static byte STATIC_LOCK_BITS_ADDRESS = (byte)0x02;

    public final static String DEFAULT_PASSWORD = "FFFFFFFF";

    private NfcA tag;

    class NFCTask extends AsyncTask {
        INTagFunction function;
        INTagCallback callback;
        boolean success = true;

        public NFCTask(INTagFunction function, INTagCallback callback) {
            this.function = function;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null)
            callback.commandStart();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (callback != null) {
                if (success) {
                    callback.commandDone();
                }
                else {
                    callback.commandError();
                }
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                function.callFunction();
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // szinkron hívások
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private byte[] getAuthConfig() throws IOException {
        return read(AUTH0_ADDRESS_216);
    }

    private byte[] getLockConfig() throws IOException {
        return read((byte)1);
    }

    public String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    @Override
    public void setTag(NfcA tag) {
        this.tag = tag;
    }

    @Override
    public void initTag(Ntag21xConfig config) throws IOException {
        byte configBits = config.getConfigBits();
        if ((configBits & Ntag21xConfig.CONF_AUTH_BIT) != 0) {
            authenticate(config.getAuthPass());
        }
        if ((configBits & Ntag21xConfig.CONF_FORMAT_BIT) != 0) {
            format(config.getFormatStartAddress());
        }
        if ((configBits & Ntag21xConfig.CONF_INIT_DATA_BIT) != 0) {
            HashMap<Byte, byte[]> initData = config.getInitDataArray();
            Iterator it = initData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                byte address = (Byte)pair.getKey();
                byte[] data = (byte[])pair.getValue();
                byte[] result = writeByteArray(address, data);
                Log.i("reader", "init_data result (hex): " + ByteOperator.bytesToHex(result));
            }
        }
        if ((configBits & Ntag21xConfig.CONF_INIT_MESSAGE_BIT) != 0) {
            HashMap<Byte, String> initData = config.getInitMessageArray();
            Iterator it = initData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                byte address = (Byte)pair.getKey();
                String data = (String)pair.getValue();
                writeMessage(address, data);
            }
        }
        if ((configBits & Ntag21xConfig.CONF_LOCK_STATIC_PAGE_BIT) != 0) {
            setLockBits(config.getLockBytes());
        }
        if ((configBits & Ntag21xConfig.CONF_SET_PASSWORD_BIT) != 0) {
            setPassword(config.getmPassword());
        }
        if ((configBits & Ntag21xConfig.CONF_SET_AUTH_PAGE_BIT) != 0) {
            setAuth0(config.getAuth0());
        }
    }

    @Override
    public byte[] authenticate(byte[] pwd) throws IOException {
        byte[] cmd = {CMD_PWD_AUTH, pwd[0], pwd[1], pwd[2], pwd[3]};
        return tag.transceive(cmd);
    }

    @Override
    public byte[] authenticate(String password) throws IOException {
        byte[] pwd = ByteOperator.hexToBytes(password);
        return authenticate(pwd);
    }

    @Override
    public byte[] read(byte address) throws IOException {
        byte[] cmd = {CMD_READ, address};
        return tag.transceive(cmd);
    }

    @Override
    public byte[] fastRead(byte startAddress, byte endAddress) throws IOException {
        byte[] cmd = {CMD_FAST_READ, startAddress, endAddress};
        return tag.transceive(cmd);
    }

    @Override
    public byte[] write(byte address, byte[] data) throws IOException {
        byte[] cmd = {CMD_WRITE, address, data[0], data[1], data[2], data[3]};
        return tag.transceive(cmd);
    }



    public byte[] writeByteArray(byte address, byte[] data) throws IOException {
        byte startAddress = address;
        byte offset = 0;
        byte[] result = null;
        int n = 0;
        try {
            for (int i = 0; i < data.length; i += 4) {
                n = i;
                byte[] writeData = new byte[4];
                System.arraycopy(data, i, writeData, 0, 4);
                result = write((byte)(startAddress+offset), writeData);
                offset++;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            int mod = data.length % 4;
            byte[] writeData = new byte[4];
            for (int i = 0; i < 4; i++) {
                if (i < mod) {
                    writeData[i] = data[n + i];
                }
                else {
                    writeData[i] = 0;
                }
            }
            result = write((byte)(startAddress+offset), writeData);
        }
        return result;
    }

    @Override
    public byte[] writeMessage(byte address, String message) throws IOException {
        byte[] data = message.getBytes("ISO8859-1");
        return writeByteArray(address, data);
    }

    @Override
    public byte[] initPassword(String password) throws IOException {
        authenticate(DEFAULT_PASSWORD);
        return setPassword(password);
    }

    @Override
    public byte[] initPassword(byte[] password) throws IOException {
        authenticate(DEFAULT_PASSWORD);
        return setPassword(password);
    }

    @Override
    public byte[] setPassword(byte[] pwd) throws IOException {
        return write(PWD_ADDRESS_216, pwd);
    }

    @Override
    public byte[] setPassword(String password) throws IOException {
        byte[] pwd = ByteOperator.hexToBytes(password);
        return write(PWD_ADDRESS_216, pwd);
    }

    @Override
    public byte[] setAuth0(byte value) throws IOException {
        byte[] config = getAuthConfig();
        byte[] authConfig = new byte[4];
        System.arraycopy(config, 0, authConfig, 0, 3);
        authConfig[3] = value;
        return write(AUTH0_ADDRESS_216, authConfig);
    }

    @Override
    public byte[] format(byte startAddress) throws IOException{
        byte[] result = null;
        for (int i = startAddress; i < 226; i++) {
            result = write((byte)i, new byte[]{(byte)0, (byte)0, (byte)0, (byte)0});
        }
        return result;
    }

    @Override
    public byte[] setLockBits(byte[] lockBits) throws IOException {
        byte[] config = getLockConfig();
        byte[] lockConfig = new byte[4];
        System.arraycopy(config, 0, lockConfig, 0, 2);
        lockConfig[2] = lockBits[0];
        lockConfig[3] = lockBits[1];
        return write(STATIC_LOCK_BITS_ADDRESS, lockConfig);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // aszinkron hívások
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initTag(final Ntag21xConfig config, final INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                initTag(config);
            }
        }, callback);
        task.execute();
    }

    @Override
    public byte[] authenticate(final byte[] password, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                authenticate(password);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] authenticate(final String password, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                authenticate(password);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] read(final byte address, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                read(address);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] fastRead(final byte startAddress, final byte EndAddress, final INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                fastRead(startAddress, EndAddress);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] write(final byte address, final byte[] data, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                write(address, data);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] writeByteArray(final byte address, final byte[] data, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                writeByteArray(address, data);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] writeMessage(final byte address, final String data, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                writeMessage(address, data);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] initPassword(final String password, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                initPassword(password);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] initPassword(final byte[] password, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                initPassword(password);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] setPassword(final String password, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                setPassword(password);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] setPassword(final byte[] password, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                setPassword(password);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] setAuth0(final byte value, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                setAuth0(value);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] format(final byte startAddress, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                format(startAddress);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }

    @Override
    public byte[] setLockBits(final byte[] lockBits, INTagCallback callback) throws IOException {
        NFCTask task = new NFCTask(new INTagFunction() {
            @Override
            public void callFunction() throws IOException {
                setLockBits(lockBits);
            }
        }, callback);
        task.execute();
        return new byte[0];
    }
}
