package com.example.team1.interface_team1;

import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by sakai on 2017/08/07.
 */

public class AccelerometerStepDetector {
    private Handler myHandler;
    {
        HandlerThread ht = new HandlerThread("StepDetector");
        ht.start();
        myHandler = new Handler(ht.getLooper());
    }

    private Runnable onStep;

    final private float dampA = 0.6f;
    final private float dampAI = 1-dampA;
    final private float dampV = 0.6f;
    final private float dampVI = 1-dampV;
    final private float threshold = (float)(SensorManager.GRAVITY_EARTH*0.99);
    final private long shortestStepNS = 100_000_000L;

    private float velX;
    private float velY;
    private float velZ;
    private float previousVelocity = 0;
    private long previousStepNS = 0;

    void updateAcceleration(final long timeNS, final float x, final float y, final float z) {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                velX = velX*dampA + x*dampAI;
                velY = velY*dampA + y*dampAI;
                velZ = velZ*dampA + z*dampAI;
                float velocity = previousVelocity*dampV + (float)Math.sqrt(velX*velX + velY*velY + velZ*velZ)*dampVI;
                if (velocity < threshold && threshold <= previousVelocity) {
                    if (shortestStepNS < timeNS - previousStepNS) {
                        previousStepNS = timeNS;
                        if (null != onStep) onStep.run();
                    }
                }
                previousVelocity = velocity;
            }
        });
    }

    void setOnStep(final Runnable process) {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                onStep = process;
            }
        });
    }
}
