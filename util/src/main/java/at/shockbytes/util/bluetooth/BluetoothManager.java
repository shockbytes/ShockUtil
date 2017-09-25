package at.shockbytes.util.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.util.UUID;

import at.shockbytes.util.security.ShockCipher;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

public class BluetoothManager {

    public enum AuthLevel {NONE, STANDARD}

    public interface WyfilesCallback {

        enum Severity {WARNING, ERROR}

        void onBluetoothError(Throwable t);

        void onBluetoothConnected(String remoteDeviceName);
    }

    private RxBluetooth rxBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    private UUID uuid;

    private ShockCipher cipher;
    private boolean useCipher;

    private BluetoothConnection bluetoothConnection;

    private String bluetoothClientName;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public BluetoothManager(BluetoothAdapter bluetoothAdapter, RxBluetooth rxBluetooth,
                            UUID uuid, ShockCipher cipher) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.rxBluetooth = rxBluetooth;
        this.uuid = uuid;
        this.cipher = cipher;

        useCipher = false;
    }

    public void initializeCipherMode(String encodedIv, String encodedKey) throws Exception {
        cipher.initializeCiphers(encodedIv, encodedKey);
        useCipher = true;
    }

    private void setBluetoothClientName(String name) {
        bluetoothClientName = name;
    }

    public void connectWithBluetoothDevice(String bluetoothClientName,
                                           @NonNull final WyfilesCallback callback) {

        setBluetoothClientName(bluetoothClientName);
        BluetoothDevice device = getBluetoothDeviceByName();

        if (device != null) {
            rxBluetooth.observeConnectDevice(device, uuid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<BluetoothSocket>() {
                        @Override
                        public void call(BluetoothSocket bluetoothSocket) {
                            setupBluetoothConnection(bluetoothSocket, callback);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            callback.onBluetoothError(throwable);
                        }
                    });
        } else {
            callback.onBluetoothError(new Throwable("Cannot find bluetooth device!"));
        }

    }

    public Observable<String> requestBluetoothReadConnection() {

        if (bluetoothConnection == null) {
            return Observable.empty();
        }

        return bluetoothConnection.observeStringStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public String decryptIfNecessary(String encrypted) {

        // Only decrypt message, if cipher is enabled, otherwise pass back plaintext
        if (useCipher) {
            try {
                encrypted = cipher.decryptMessage(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encrypted;
    }

    public void sendBluetoothMessage(String text) {

        if (bluetoothConnection != null) {

            if (useCipher) { // If encryption is available, apply it before sending
                try {
                    text = cipher.encryptMessage(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            text += "\n"; // Always append line break at the end
            bluetoothConnection.send(text);
        }
    }

    public void shutdown() {

        if (rxBluetooth != null) {
            rxBluetooth.cancelDiscovery();
        }
        if (bluetoothConnection != null) {
            bluetoothConnection.closeConnection();
        }
    }

    @Nullable
    private BluetoothDevice getBluetoothDeviceByName() {

        for (BluetoothDevice d : bluetoothAdapter.getBondedDevices()) {
            if (d.getName().equals(bluetoothClientName)) {
                return d;
            }
        }
        return null;
    }

    private void setupBluetoothConnection(BluetoothSocket socket,
                                          @NonNull WyfilesCallback callback) {

        if (socket == null) {
            callback.onBluetoothError(new Throwable("BluetoothSocket is null!"));
            return;
        }

        try {

            bluetoothConnection = new BluetoothConnection(socket);
            callback.onBluetoothConnected(socket.getRemoteDevice().getName());

        } catch (Exception e) {
            e.printStackTrace();
            callback.onBluetoothError(e.getCause());
        }
    }

    public void startBluetoothServer(@NonNull final WyfilesCallback callback) {

        rxBluetooth.observeBluetoothSocket("wyfiles_bt_socket", uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<BluetoothSocket>() {
                    @Override
                    public void call(BluetoothSocket bluetoothSocket) {
                        setupBluetoothConnection(bluetoothSocket, callback);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onBluetoothError(throwable);
                    }
                });
    }

}
