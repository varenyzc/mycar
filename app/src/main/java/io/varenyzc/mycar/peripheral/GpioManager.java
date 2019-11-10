package io.varenyzc.mycar.peripheral;

import android.content.res.Resources;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

import io.varenyzc.mycar.R;

public class GpioManager {

    private Gpio SysLed;
    private String gpioSys = "BCM12";  //系统开机灯
    private Gpio NetLed;
    private String gpioLed = "BCM5";   //网络灯

    private PeripheralManager mPeripheralManager;

    private static GpioManager instance;
    private GpioManager(){}

    public static GpioManager getInstance() {
        if (instance == null) {
            instance = new GpioManager();
        }
        return instance;
    }

    public void init(){
        mPeripheralManager = PeripheralManager.getInstance();
        try {
            SysLed = mPeripheralManager.openGpio(gpioSys);
            NetLed = mPeripheralManager.openGpio(gpioLed);
            resetGpio(SysLed);
            resetGpio(NetLed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetGpio(final Gpio gpio){
        try {
            if(gpio!=null) {
                gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);//设置为输出，默认低电平
                gpio.setActiveType(Gpio.ACTIVE_HIGH);//设置高电平为活跃的
                gpio.setValue(false);//设置成低电平
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchSysLed(boolean b) {
        try {
            SysLed.setValue(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchNetLed(boolean b) {
        try {
            NetLed.setValue(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            SysLed.close();
            NetLed.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
