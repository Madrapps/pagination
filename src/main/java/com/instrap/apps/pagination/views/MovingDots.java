package com.instrap.apps.pagination.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import com.instrap.apps.pagination.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * View for Pagination.
 * Created by Henry on 8/30/2015.
 */
public class MovingDots extends View {

    public static final int TYPE_FORWARD = 1;
    public static final int TYPE_BACKWARD = -1;
    // For drawing/animating the first and last dots
    private float lastRadius = 0;
    private float firstRadius = 0;
    private float lastXPos = 0;
    private float firstXPos = 0;
    // For animation
    private Handler handlerTraverse;
    private Runnable runnableTraverse;
    private Handler handlerVisibility;
    private Runnable runnableVisibility;
    private float shift;
    private Paint paintDots;
    private float dotRadius;
    private int currentDot;
    // Stuff to get from the attributes
    private float dotSpacing;
    private int dotColor;
    private float dotMinRadius;
    private float dotMaxRadius;
    private int dotCount;
    // The xPosition of the first and last dot. Needed for the animation
    private float firstDotXPos = 0;
    private float lastDotXPos = 0;
    private boolean isVisible = true;
    private boolean tempVisible = true;
    private int visibilityRadius;

    public MovingDots(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MovingDots(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    /**
     * Hides the pagination bar
     */
    public void hide() {
        visibilityRadius = 0;
        tempVisible = false;
        runnableVisibility.run();
    }

    /**
     * Moves the Dot front or back
     *
     * @param index {@code TYPE_FORWARD} to step front, {@code TYPE_BACKWARD} to step back
     */
    public void paginate(@Type int index) {

        // Reset global stuff
        lastRadius = 0;
        firstRadius = dotMinRadius;
        shift = 0;

        currentDot += index;
        if (currentDot > dotCount) {
            // If currentDot is more than the dotCount, then animate. Always make sure that the currentDot is only one greater than the dotCount.
            currentDot = dotCount + 1;
            runnableTraverse.run();
        } else {
            // Draw static stuff, no animation
            invalidate();
        }
    }

    /**
     * Shows the pagination bar
     */
    public void show() {
        visibilityRadius = 0;
        tempVisible = true;
        runnableVisibility.run();
    }

    private void animateTraverse() {
        // Increase all the parameters in slow increments for each onDraw, so as to animate.
        lastRadius++;
        firstRadius -= dotMinRadius / dotMaxRadius;
        final float gap = dotSpacing + 2 * dotMinRadius;
        shift += gap / dotMaxRadius;

        lastXPos = lastDotXPos + gap - shift;
        firstXPos = firstDotXPos - shift;

        if (Float.compare(lastRadius, dotMaxRadius) > 0) {
            // Stop animation.
            handlerTraverse.removeCallbacks(runnableTraverse);
            // Make the last dot as the currentDot, so that going backward makes sense
            currentDot = dotCount;
        } else {
            handlerTraverse.postDelayed(runnableTraverse, 20);
        }
        invalidate();
    }

    private void animateVisibility() {
        visibilityRadius++;

        if (visibilityRadius > dotMaxRadius) {
            // Stop animation.
            handlerVisibility.removeCallbacks(runnableVisibility);
            this.isVisible = tempVisible;
        } else {
            handlerVisibility.postDelayed(runnableVisibility, 20);
        }
        invalidate();
    }

    private void drawDotsShiftAnimation(Canvas canvas, float left, float yPos) {
        float xPos;// Draw the last dot
        xPos = lastXPos;
        dotRadius = lastRadius;
        canvas.drawCircle(xPos, yPos, dotRadius, paintDots);

        // Draw the first dot
        xPos = firstXPos;
        dotRadius = firstRadius;
        canvas.drawCircle(xPos, yPos, dotRadius, paintDots);

        // Draw all the dots between them. Shift them on each onDraw call
        xPos = left - shift;
        dotRadius = dotMinRadius;
        for (int dotIndex = 1; dotIndex <= dotCount; dotIndex++) {
            xPos += dotRadius;
            if (dotIndex != 1) {
                canvas.drawCircle(xPos, yPos, dotRadius, paintDots);
            }
            xPos += dotRadius + dotSpacing;
        }
    }

    private void drawStaticDots(Canvas canvas, float left, float yPos) {
        float xPos;
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

    private void drawVisibilityAnimation(Canvas canvas, float left, float yPos) {
        float xPos;
        xPos = left;
        for (int dotIndex = 1; dotIndex <= dotCount; dotIndex++) {
            if (dotIndex == currentDot) {
                dotRadius = dotMaxRadius;
            } else {
                dotRadius = dotMinRadius;
            }
            xPos += dotRadius;

            // Calculate radius for both show and hide scenario
            float radius;
            if (tempVisible) {
                radius = Math.min(visibilityRadius, dotMinRadius);
                if (dotIndex == currentDot) {
                    radius = visibilityRadius;
                }
            } else {
                radius = dotRadius - visibilityRadius;
            }

            canvas.drawCircle(xPos, yPos, radius, paintDots);

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

        // Initialize some parameters
        dotRadius = dotMinRadius;
        currentDot = 1;

        // Handler for the traverse animation
        handlerTraverse = new Handler();
        runnableTraverse = new Runnable() {
            @Override
            public void run() {
                animateTraverse();
            }
        };

        // Handler for the show/hide animation
        handlerVisibility = new Handler();
        runnableVisibility = new Runnable() {

            @Override
            public void run() {
                animateVisibility();
            }
        };
    }

    public int getCurrentDot() {
        return currentDot;
    }

    public void setCurrentDot(int currentDot) {
        this.currentDot = currentDot;
        invalidate();
    }

    public int getDotColor() {
        return dotColor;
    }

    public void setDotColor(int dotColor) {
        this.dotColor = dotColor;
        invalidate();
    }

    @IntDef({TYPE_BACKWARD, TYPE_FORWARD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // The total length to be drawn
        final float drawLength = dotMinRadius * 2 * (dotCount - 1) + 2 * dotMaxRadius + (dotCount - 1) * dotSpacing;
        // The left most point to be drawn
        final float left = (canvas.getWidth() - drawLength) / 2;

        // yPos is a constant and never varies, xPos varies one by one so each dot can be drawn
        final float yPos = canvas.getHeight() / 2;

        if (tempVisible != isVisible) {
            drawVisibilityAnimation(canvas, left, yPos);
            return;
        } else if (!isVisible) {
            return;
        }

        // Do not go before the first dot.
        if (currentDot < 1) {
            currentDot = 1;
        }

        if (currentDot > dotCount) {
            // Animate if currentDot has exceeded the dotCount
            drawDotsShiftAnimation(canvas, left, yPos);
        } else {
            // If currentDot is between 1 and dotCount, do not animate. Just draw static stuff.
            drawStaticDots(canvas, left, yPos);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The width is the width of parent. The height is the maximum height of the dot
        final int measuredHeight = (int) Math.ceil(dotMaxRadius * 2);
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

}
