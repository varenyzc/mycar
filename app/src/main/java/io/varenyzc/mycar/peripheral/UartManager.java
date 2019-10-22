package io.varenyzc.mycar.peripheral;

import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

public class UartManager {
    private static UartManager instance;

    private static final String UART_DEVICE_NAME = "UART0";
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 512;

    private UartDevice mUartDevice;

    private UartManager(){

    }

    public static UartManager getInstance() {
        if (instance == null) {
            instance = new UartManager();
        }
        return instance;
    }

    public void init(){
        PeripheralManager peripheralManager = PeripheralManager.getInstance();
        try {
            mUartDevice = peripheralManager.openUartDevice(UART_DEVICE_NAME);
            mUartDevice.setBaudrate(BAUD_RATE);
            mUartDevice.setDataSize(DATA_BITS);
            mUartDevice.setParity(UartDevice.PARITY_NONE);
            mUartDevice.setStopBits(STOP_BITS);
            mUartDevice.registerUartDeviceCallback(mUartDeviceCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UartDeviceCallback mUartDeviceCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uartDevice) {
            return false;
        }
    };

    public void write(String data){
        byte[] bytes = data.getBytes();
        try {
            int i = mUartDevice.write(bytes, bytes.length);
            Log.d("varenyzc1", "write: "+bytes.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("varenyzc1", "write: "+e.toString());
        }
    }

    public void close() {
        if (mUartDevice != null) {
            try {
                mUartDevice.close();
                mUartDevice = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
