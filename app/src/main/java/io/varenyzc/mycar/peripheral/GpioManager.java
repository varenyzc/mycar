package io.varenyzc.mycar.peripheral;

import android.content.res.Resources;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

import io.varenyzc.mycar.R;

public class GpioManager {

    private Gpio GpioLeft;
    private String gpioLeft = "BCM21";

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
            GpioLeft = mPeripheralManager.openGpio(gpioLeft);
            resetGpio(GpioLeft);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            GpioLeft.setValue(true);
                            Thread.sleep(5);
                            GpioLeft.setValue(false);
                            Thread.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetGpio(final Gpio gpio){
        try {
            if(gpio!=null) {
                gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);//设置为输出，默认低电平
                gpio.setActiveType(Gpio.ACTIVE_HIGH);//设置高电平为活跃的
                //gpio.setValue(false);//设置成低电平
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
