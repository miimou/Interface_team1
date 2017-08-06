package com.example.team1.interface_team1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by sakai on 2017/08/06.
 * Manage / Update Screen(SurfaceView)
 */

class ScreenDrawer implements SurfaceHolder.Callback {
    private SurfaceView sv;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Paint textPaint, scorePaint;
    {
        textPaint = new Paint();
        textPaint.setColor(0xff332211);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);
        scorePaint = new Paint();
        scorePaint.setColor(0xff112233);
        scorePaint.setTextAlign(Paint.Align.RIGHT);
        scorePaint.setTextSize(70);
        scorePaint.setAntiAlias(true);
    }

    ScreenDrawer(SurfaceView surfaceView, Context ctx) {
        sv = surfaceView;
        sv.getHolder().addCallback(this);
        Resources r = ctx.getResources();
        bowlSprites = new Drawable[] {
                r.getDrawable(R.drawable.center),
                r.getDrawable(R.drawable.d11), r.getDrawable(R.drawable.d12), r.getDrawable(R.drawable.d13),
                r.getDrawable(R.drawable.d21), r.getDrawable(R.drawable.d22), r.getDrawable(R.drawable.d23),
                r.getDrawable(R.drawable.d31), r.getDrawable(R.drawable.d32), r.getDrawable(R.drawable.d33),
                r.getDrawable(R.drawable.d41), r.getDrawable(R.drawable.d42), r.getDrawable(R.drawable.d43)
        };
        gameOverSprite = r.getDrawable(R.drawable.gameover);
    }

    enum ScreenStatus {
        TOP(true, false, false, false),
        TOP_START_ACCEPT(true, true, false, false),
        IN_GAME(false, false, true, false),
        GAME_OVER(false, false, true, true);

        private boolean showHorizontalMessage;
        private boolean showStartButton;
        private boolean showScore;
        private boolean showGameOver;

        ScreenStatus(boolean horizontal, boolean start, boolean score, boolean over) {
            showHorizontalMessage = horizontal;
            showStartButton = start;
            showScore = score;
            showGameOver = over;
        }
    }
    private ScreenStatus status = ScreenStatus.TOP;

    private Drawable[] bowlSprites;
    private Drawable gameOverSprite;

    private int bowlSpriteId = 0;
    private int score = 0;

    private void drawScreen() {
        SurfaceHolder holder = sv.getHolder();
        Canvas c;
        if (null == (c = holder.lockCanvas())) return;
        c.drawARGB(255, 230, 230, 230);
        Drawable bs = bowlSprites[bowlSpriteId];
        int cWidth = c.getWidth();
        int cHeight = c.getHeight();
        if (cWidth < cHeight) {
            bs.setBounds(0, cHeight / 2 - cWidth / 2, cWidth, cHeight / 2 + cWidth / 2);
        } else {
            bs.setBounds(cWidth / 2 - cHeight / 2, 0, cWidth / 2 + cHeight / 2, cHeight);
        }
        bs.draw(c);
        if (status.showHorizontalMessage) {
            c.drawText("画面を水平にしてください", cWidth/2, textPaint.getTextSize(), textPaint);
        }
        if (status.showStartButton) {
            c.drawText("タップしてゲームスタート", cWidth/2, cHeight-textPaint.descent(), textPaint);
        }
        if (status.showScore) {
            c.drawText("Score: "+score, cWidth, scorePaint.getTextSize(), scorePaint);
        }
        if (status.showGameOver) {
            int gHeight = gameOverSprite.getIntrinsicHeight();
            int gWidth = gameOverSprite.getIntrinsicWidth();
            float cAspect = (float)cHeight/cWidth;
            float gAspect = (float)gHeight/gWidth;
            if (cAspect < gAspect) {
                gameOverSprite.setBounds((int)(cWidth-cHeight/gAspect)/2, 0, (int)(cWidth+cHeight/gAspect)/2, cHeight);
            } else {
                gameOverSprite.setBounds(0, (int)(cHeight-cWidth*gAspect)/2, cWidth, (int)(cHeight+cWidth*gAspect)/2);
            }
            Log.d("ScreenDrawer", "drawScreen: "+ cAspect +"/"+ gAspect + "(" + gHeight + "," + gWidth);
            gameOverSprite.draw(c);
            c.drawText("タップしてスタート画面へ", cWidth/2, cHeight-textPaint.descent(), textPaint);
        }
        holder.unlockCanvasAndPost(c);
    }

    void setStatus(final ScreenStatus status) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ScreenDrawer.this.status = status;
                drawScreen();
            }
        });
    }
    void setBowl(final int direction, final int amount) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (amount == 0) bowlSpriteId = 0;
                else bowlSpriteId = direction*3+amount-3;
                drawScreen();
            }
        });
    }
    void setScore(final int score) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ScreenDrawer.this.score = score;
                drawScreen();
            }
        });
    }

    void setOnTouchListener(View.OnTouchListener l) {
        sv.setOnTouchListener(l);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        drawScreen();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
