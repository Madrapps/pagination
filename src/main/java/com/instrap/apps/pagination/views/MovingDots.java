package com.instrap.apps.pagination.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.instrap.apps.pagination.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tsaravana on 8/30/2015.
 */
public class MovingDots extends View {

    private float lastRadius = 0;
    private float firstRadius = 0;

    private float lastXPos = 0;
    private float firstXPos = 0;

    private Handler handler;
    private Runnable runnable;
    private float shift;

    @IntDef({TYPE_BACKWARD, TYPE_FORWARD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    private Paint paintDots;
    private float dotRadius;
    private float dotSpacing;
    private int dotColor;
    private float dotMinRadius;
    private float dotMaxRadius;
    private int dotCount;

    private int currentDot;

    public static final int TYPE_FORWARD = 1;
    public static final int TYPE_BACKWARD = -1;

    private float firstDotXPos = 0;
    private float lastDotXPos = 0;

    public MovingDots(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MovingDots(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MovingDots);
        dotColor = typedArray.getColor(R.styleable.MovingDots_dot_color, Color.WHITE);
        dotMinRadius = typedArray.getDimension(R.styleable.MovingDots_dot_min_radius, 20);
        dotMaxRadius = typedArray.getDimension(R.styleable.MovingDots_dot_max_radius, 30);
        dotCount = typedArray.getInteger(R.styleable.MovingDots_dot_count, 5);
        dotSpacing = typedArray.getDimension(R.styleable.MovingDots_dot_spacing, 10);

        typedArray.recycle();

        paintDots = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDots.setStyle(Paint.Style.FILL);
        paintDots.setColor(dotColor);

        dotRadius = dotMinRadius;
        currentDot = 1;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                initAnim();
            }
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredHeight = (int) Math.ceil(dotMaxRadius * 2);
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        final float drawLength = dotMinRadius * 2 * (dotCount - 1) + 2 * dotMaxRadius + (dotCount - 1) * dotSpacing;
        final float left = (width - drawLength) / 2;
        final float yPos = canvas.getHeight() / 2;
        float xPos = left;

        if (currentDot == dotCount) {
            xPos = lastXPos;
            dotRadius = lastRadius;
            canvas.drawCircle(xPos, yPos, dotRadius, paintDots);

            xPos = firstXPos;
            dotRadius = firstRadius;
            canvas.drawCircle(xPos, yPos, dotRadius, paintDots);

            xPos = left - shift;
            Log.d("TAG", "XPOS SHIFTED = " + xPos);
            dotRadius = dotMinRadius;
            for (int dotIndex = 1; dotIndex <= dotCount; dotIndex++) {
                xPos += dotRadius;
                if (dotIndex != 1) {
                    canvas.drawCircle(xPos, yPos, dotRadius, paintDots);
                }

                // If the currentDot is the last Dot, then save the coordinate of the first and last dot;
                if (currentDot == dotCount) {
                    if (dotIndex == 1) {
                        firstDotXPos = xPos;
                    } else if (dotIndex == dotCount) {
                        lastDotXPos = xPos;
                    }
                }

                xPos += dotRadius + dotSpacing;
            }
        } else {
            xPos = left;
            for (int dotIndex = 1; dotIndex <= dotCount; dotIndex++) {
                if (dotIndex == currentDot) {
                    dotRadius = dotMaxRadius;
                } else {
                    dotRadius = dotMinRadius;
                }

                xPos += dotRadius;
                canvas.drawCircle(xPos, yPos, dotRadius, paintDots);

                // If the currentDot is the last Dot, then save the coordinate of the first and last dot;
                if (currentDot == dotCount) {
                    if (dotIndex == 1) {
                        firstDotXPos = xPos;
                    } else if (dotIndex == dotCount) {
                        lastDotXPos = xPos;
                    }
                }

                xPos += dotRadius + dotSpacing;
            }
        }
    }

    public void paginate(@Type int index) {
        lastRadius = 0;
        firstRadius = dotMinRadius;
        shift = 0;

        lastXPos = lastDotXPos + (dotMaxRadius * 5);
        firstXPos = firstDotXPos;

        Log.d("TAG", "III LAST X POS = " + lastXPos);

        currentDot += index;
        if (currentDot < 1) {
            currentDot = 1;
            invalidate();
        } else if (currentDot > dotCount) {
            currentDot = dotCount;
            runnable.run();
        } else {
            invalidate();
        }
    }

    private void initAnim() {
        lastRadius++;
        firstRadius -= dotMinRadius / dotMaxRadius;
        lastXPos -= 5;
        firstXPos -= 5;
        shift += (dotSpacing + 2 * dotMinRadius) / dotMaxRadius;
        Log.d("TAG", "SHIFT = " + shift);

        if (lastRadius == dotMaxRadius) {
            handler.removeCallbacks(runnable);
        } else {
            handler.postDelayed(runnable, 200);
        }
        invalidate();
    }
}
