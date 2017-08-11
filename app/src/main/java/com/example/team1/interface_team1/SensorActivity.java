package com.example.team1.interface_team1;

        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Handler;
        import android.os.Looper;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.TextView;
        import java.util.List;
        import android.view.SurfaceView;


public class SensorActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private ScreenDrawer sd;
    private int stepcount = 0;
    private int stepcount2 = 0;
    private AccelerometerStepDetector detector;
    Runnable onStep;
    private int score = 0;

    // センサーの値
    float[] magneticValues  = null;

    private enum GameState {
        TOP,
        TOP_ACCEPTABLE,
        IN_GAME,
        GAME_OVER
    }
    GameState state = GameState.TOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        //ゲーム画面
        sd = new ScreenDrawer((SurfaceView) findViewById(R.id.surfaceView), this);

        //センサー・マネージャーを取得する
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        detector = new AccelerometerStepDetector();
        detector.setOnStep(onStep);
    }


    //アプリが起動する時
    @Override
    protected void onStart() {
        super.onStart();
        super.onResume();

        //全センサーの取得
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        //センサマネージャーへリスナーを登録
        for (Sensor sensor : sensors) {

            //Gyroの登録
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            }

            //加速器の登録
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    //アプリ終了時
    protected void onStop(){

        super.onStop();
        super.onPause();

        mSensorManager.unregisterListener( this );

    }


    // 回転行列
    float[]    I= new float[9];
    float[] accelerometerValues = new float[3];
    float step[];

    View.OnTouchListener startTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            state = GameState.IN_GAME;
            sd.setStatus(ScreenDrawer.ScreenStatus.IN_GAME);
            sd.setOnTouchListener(null);
            return true;
        }
    };

    View.OnTouchListener resetTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            state = GameState.TOP;
            sd.setStatus(ScreenDrawer.ScreenStatus.TOP);
            sd.setScore(score = 0);
            return true;
        }
    };

    {
        onStep = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (state == GameState.IN_GAME) {
                            sd.setScore(score++);
                        }
                    }
                });
            }
        };
    }

    //センサーの値が変更された時に呼び出される
    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                break;
        }

        if (magneticValues != null && accelerometerValues != null) {

            float[]  inR = new float[9];
            float[]    I= new float[9];
            SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues);//Gyroセンサーと加速どセンサーの値を行列に出力

            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);//座標系に変換

            float[] orientationValues   = new float[3];
            SensorManager.getOrientation(outR, orientationValues);//値を得る

            detector.updateAcceleration(event.timestamp, accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]);

            /* debug用
             * x:方位角
             * y:前後の傾斜
             * z:左右の傾斜
             */
            //計算クラスを呼び出す
            Calc calc = new Calc(radianToDegree(orientationValues[0]),radianToDegree(orientationValues[1]),radianToDegree(orientationValues[2]));
            //ここで,溢れる方向を得る
            int direction = calc.getDirection();
            //ここで,溢れる量を得る
            int amount = calc.getAmount();

            sd.setBowl(direction, amount);
            if (state == GameState.TOP) {
                if (amount == 0) {
                    state = GameState.TOP_ACCEPTABLE;
                    sd.setStatus(ScreenDrawer.ScreenStatus.TOP_START_ACCEPT);
                    sd.setOnTouchListener(startTouch);
                }
            } else if (state == GameState.TOP_ACCEPTABLE) {
                if (amount != 0) {
                    state = GameState.TOP;
                    sd.setStatus(ScreenDrawer.ScreenStatus.TOP);
                    sd.setOnTouchListener(null);
                }
            } else if (state == GameState.IN_GAME) {
                if (amount == 3) {
                    state = GameState.GAME_OVER;
                    sd.setStatus(ScreenDrawer.ScreenStatus.GAME_OVER);
                    sd.setOnTouchListener(resetTouch);
                }
            }
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //ラジアンから角度に変換
    int radianToDegree(float rad){
        return (int) Math.floor( Math.toDegrees(rad));
    }

}
