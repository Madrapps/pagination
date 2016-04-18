package com.instrap.apps.pagination.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by anitham on 3/3/16.
 */
public class CheezzaDots extends View {

    private static final String TAG = "CheezzaDots";
    private static final int ANIMATION_DURATION = 2000;
    private static final int FIRST_TRAVERSE = 1, SECOND_TRAVERSE = 2, THIRD_TRAVERSE = 3;
    private Context context;
    private int defaultDotColor = 0xffffff00, currentDotColor = 0xffff0000;
    private int visibleDotsCount = 4;
    private int defaultDotRadius = dpToPx(40), currentDotRadius = dpToPx(50);
    private int dotSpacing = dpToPx(50);
    private int dotMarginOffset = dpToPx(10);

    private int currentDotIndex = 0, repeatCount = 0;
    private Paint defaultDotPaint, currentDotPaint, dotPaint, movementPaint;
    private boolean isAnimating = false, isForward = true;
    private Point currentDotCentre, animGapPointTop, animGapPointBottom, pointLeftTop, pointLeftBottom, pointRightTop, pointRightBottom;
    private int growingRadius, shrinkingRadius, rightTopAngle, rightBottomAngle, leftTopAngle = 0, leftBottomAngle; //wrt path, right: top:360-270, bottom:0-90, left: top:180-270 bottom:180-90
    private PointRange gapXRange, gapYTopRange, gapYBottomRange, angleLeftTopRange, angleLeftBottomRange, angleRightTopRange, angleRightBottomRange;

    public CheezzaDots(Context context) {
        this(context, null, 0);
    }

    public CheezzaDots(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheezzaDots(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        initialiseAttributes(attrs, defStyleAttr);
        initialisePaint();

        currentDotCentre = getCentrePointOfCircleWithIndex(currentDotIndex);

        animGapPointTop = new Point();
        animGapPointBottom = new Point();

    }

    public void initialiseAttributes(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
//            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CheezzaDots);
//            defaultDotColor = attributes.getColor(R.styleable.CheezzaDots_default_dot_color, Color.GRAY);
//            currentDotColor = attributes.getColor(R.styleable.CheezzaDots_current_dot_color, Color.GRAY);
//            visibleDotsCount = attributes.getInt(R.styleable.CheezzaDots_visible_dot_count, 0);
//            defaultDotRadius = (int) attributes.getDimension(R.styleable.CheezzaDots_default_dot_radius, 0);
//            currentDotRadius = (int) attributes.getDimension(R.styleable.CheezzaDots_current_dot_radius, 0);
//            dotSpacing = (int) attributes.getDimension(R.styleable.CheezzaDots_cheezza_dot_spacing, 0);
//            dotMarginOffset = (int) attributes.getDimension(R.styleable.CheezzaDots_view_margin, 0);
        }
    }

    public void initialisePaint() {
        defaultDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultDotPaint.setColor(defaultDotColor);
        defaultDotPaint.setStyle(Paint.Style.FILL);

        currentDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentDotPaint.setColor(defaultDotColor); //currentDotColor
        currentDotPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);

        movementPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        movementPaint.setColor(defaultDotColor); //Color.rgb(255, 105, 180));
        movementPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawDots(canvas);
        if (isAnimating) {
            drawAnim(canvas);
        } else {
            canvas.drawColor(0xFF123456);
            drawDots(canvas);// should b removed and drawDots should come after this
        }

    }

    private void startYAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(ANIMATION_DURATION / 2);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(1);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float ratio = (float) valueAnimator.getAnimatedValue();
                calculateGapY(ratio);
            }
        });
        valueAnimator.start();
    }

//    private void startAnimatorForOutwardToInwardCurve(){
//        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
//        valueAnimator.setDuration(ANIMATION_DURATION);
//        valueAnimator.setInterpolator(new AccelerateInterpolator());
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                float ratio = (float) valueAnimator.getAnimatedValue();
//                calculateOutwardToInwardCurve(ratio);
//
//
//            }
//        });
//
//
//        }

    private void startAnimator() {

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(2);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
                //   animator.setDuration((int) (0.3 * ANIMATION_DURATION));
                repeatCount = 1; // first reversal
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
                currentDotIndex++;
                currentDotCentre = getCentrePointOfCircleWithIndex(currentDotIndex);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                repeatCount++;

                if (repeatCount == SECOND_TRAVERSE) {
                    startYAnimator();
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    dotPaint.setColor(Color.BLUE);

                    angleLeftTopRange = new PointRange(270 - 25, 180 + 7);
                    angleLeftBottomRange = new PointRange(90 + 25, 180 - 7);
                    angleRightTopRange = new PointRange(360 - 7, 270 + 25);
                    angleRightBottomRange = new PointRange(7, 90 - 25);

                } else {

                    dotPaint.setColor(Color.BLACK);

                    animator.setInterpolator(new AccelerateInterpolator());

                    angleLeftTopRange = new PointRange(270 - 25, 180 + 15);
                    angleLeftBottomRange = new PointRange(90 + 25, 180 - 15);
                    angleRightTopRange = new PointRange(360 - 15, 270 + 25);
                    angleRightBottomRange = new PointRange(15, 90 - 25);
                }
//                if (repeatCount == SECOND_TRAVERSE) {
//                    animator.setDuration((int) (0.5 * ANIMATION_DURATION));
//                } else {
//                    animator.setDuration((int) (0.3 * ANIMATION_DURATION));
//                }
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                calculateAngle(animatedValue);
                calculatePointOnCircles();
                calculateGapAndRadiusPoints(animatedValue);
                invalidate();
            }
        });
        valueAnimator.setStartDelay(2000);
        valueAnimator.start();
    }

    private void calculateAngle(float ratio) {
        rightTopAngle = getPointFromRangeWithRatio(angleRightTopRange, ratio);
        rightBottomAngle = getPointFromRangeWithRatio(angleRightBottomRange, ratio);
        leftTopAngle = getPointFromRangeWithRatio(angleLeftTopRange, ratio);
        leftBottomAngle = getPointFromRangeWithRatio(angleLeftBottomRange, ratio);
    }

    private void calculatePointOnCircles() {
        if (repeatCount == FIRST_TRAVERSE) {

            pointLeftTop = getPointOnCircleWithAngle(rightTopAngle, currentDotRadius, currentDotCentre);
            pointLeftBottom = getPointOnCircleWithAngle(rightBottomAngle, currentDotRadius, currentDotCentre);
            pointRightTop = getPointOnCircleWithAngle(leftTopAngle, defaultDotRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1));
            pointRightBottom = getPointOnCircleWithAngle(leftBottomAngle, defaultDotRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1));

//          gapXRange = new PointRange(currentDotCentre.x + 2 * currentDotRadius, pointLeftTop.x + 2 * (currentDotRadius - (pointLeftTop.x - currentDotCentre.x) + dotSpacing));
            gapXRange = new PointRange(currentDotCentre.x + currentDotRadius + defaultDotRadius, pointLeftTop.x + 2 * (currentDotRadius - (pointLeftTop.x - currentDotCentre.x) + dotSpacing));

        } else if (repeatCount == SECOND_TRAVERSE) {

            pointLeftTop = getPointOnCircleWithAngle(rightTopAngle, shrinkingRadius, currentDotCentre);
            pointLeftBottom = getPointOnCircleWithAngle(rightBottomAngle, shrinkingRadius, currentDotCentre);
            pointRightTop = getPointOnCircleWithAngle(leftTopAngle, growingRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1));
            pointRightBottom = getPointOnCircleWithAngle(leftBottomAngle, growingRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1));

            gapXRange = new PointRange(currentDotCentre.x + defaultDotRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1).x - defaultDotRadius); //- (int) (0.2 * dotSpacing)

        } else if (repeatCount == THIRD_TRAVERSE) {

            pointLeftTop = getPointOnCircleWithAngle(rightTopAngle, currentDotRadius, currentDotCentre);
            pointLeftBottom = getPointOnCircleWithAngle(rightBottomAngle, currentDotRadius, currentDotCentre);

            pointRightTop = getPointOnCircleWithAngle(leftTopAngle, currentDotRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1));
            pointRightBottom = getPointOnCircleWithAngle(leftBottomAngle, currentDotRadius, getCentrePointOfCircleWithIndex(currentDotIndex + 1));

            Point nextCircleCentre = getCentrePointOfCircleWithIndex(currentDotIndex + 1);
            gapXRange = new PointRange(pointRightTop.x - 2 * (currentDotRadius - (nextCircleCentre.x - pointRightTop.x) + dotSpacing), nextCircleCentre.x - currentDotRadius - defaultDotRadius);
        }
    }

    private void calculateGapAndRadiusPoints(float ratio) {
        animGapPointTop.x = getPointFromRangeWithRatio(gapXRange, ratio);
//        animGapPointTop.y = getPointFromRangeWithRatio(gapYTopRange, ratio);

        animGapPointBottom.x = getPointFromRangeWithRatio(gapXRange, ratio);
//        animGapPointBottom.y = getPointFromRangeWithRatio(gapYBottomRange, ratio);

        if (repeatCount == SECOND_TRAVERSE) {
            shrinkingRadius = defaultDotRadius + (int) ((currentDotRadius - defaultDotRadius) * ratio);
            growingRadius = currentDotRadius + (int) ((defaultDotRadius - currentDotRadius) * ratio);
        }
    }

    private void calculateGapY(float ratio) {
        animGapPointTop.y = getPointFromRangeWithRatio(gapYTopRange, ratio);
        animGapPointBottom.y = getPointFromRangeWithRatio(gapYBottomRange, ratio);
    }


    private void paginate() {

        //wrt path, right: top:360-270, bottom:0-90, left: top:270-180 bottom:90-180
        angleLeftTopRange = new PointRange(270 - 25, 180 + 15);
        angleLeftBottomRange = new PointRange(90 + 25, 180 - 15);
        angleRightTopRange = new PointRange(360 - 15, 270 + 25);
        angleRightBottomRange = new PointRange(15, 90 - 25);

        startAnimator();
    }

    public void paginate(int index) {
        if (currentDotIndex > index) {
            isForward = false;
            paginatePrevious();
        } else {
            isForward = true;
            paginateNext();
        }
    }

    public void paginateNext() {

        gapXRange = new PointRange(currentDotCentre.x + 2 * currentDotRadius, (int) (getCentrePointOfCircleWithIndex(currentDotIndex + 1).x + defaultDotRadius + 0.5 * dotSpacing));
        gapYTopRange = new PointRange(currentDotCentre.y, currentDotCentre.y - currentDotRadius / 4);
        gapYBottomRange = new PointRange(currentDotCentre.y, currentDotCentre.y + currentDotRadius / 4);

        isForward = true;
        paginate();

    }

    public void paginatePrevious() {

        gapXRange = new PointRange(currentDotCentre.x + 2 * currentDotRadius, (int) (getCentrePointOfCircleWithIndex(currentDotIndex + 1).x + defaultDotRadius + 0.5 * dotSpacing));
        gapYTopRange = new PointRange(currentDotCentre.y, currentDotCentre.y - currentDotRadius / 4);
        gapYBottomRange = new PointRange(currentDotCentre.y, currentDotCentre.y + currentDotRadius / 4);

        isForward = false;

        startAnimator();
    }




    private void drawDots(Canvas canvas) {

        for (int dotCount = 0; dotCount < visibleDotsCount; dotCount++) {
            Point centre = getCentrePointOfCircleWithIndex(dotCount);

            if (isAnimating && repeatCount == SECOND_TRAVERSE) {
                if (dotCount == currentDotIndex) {
                    canvas.drawCircle(centre.x, centre.y, shrinkingRadius, currentDotPaint);
                } else if (dotCount == currentDotIndex + 1) {
                    canvas.drawCircle(centre.x, centre.y, growingRadius, defaultDotPaint);
                } else {
                    canvas.drawCircle(centre.x, centre.y, defaultDotRadius, defaultDotPaint);
                }
            } else if (isAnimating && repeatCount == THIRD_TRAVERSE) {
                if (dotCount == currentDotIndex + 1) {
                    canvas.drawCircle(centre.x, centre.y, currentDotRadius, currentDotPaint);
                } else {
                    canvas.drawCircle(centre.x, centre.y, defaultDotRadius, defaultDotPaint);
                }
            } else {
                if (dotCount == currentDotIndex) {
                    canvas.drawCircle(centre.x, centre.y, currentDotRadius, currentDotPaint);
                } else {
                    canvas.drawCircle(centre.x, centre.y, defaultDotRadius, defaultDotPaint);
                }
            }
        }
    }

    private void drawAnim(Canvas canvas) {

        Path path = new Path();
        if (repeatCount == FIRST_TRAVERSE) {

            path.moveTo(pointLeftTop.x, pointLeftTop.y);
            path.cubicTo(pointLeftTop.x, pointLeftTop.y, animGapPointTop.x, currentDotCentre.y, pointLeftBottom.x, pointLeftBottom.y);

        } else if (repeatCount == SECOND_TRAVERSE) {

            path.moveTo(pointLeftTop.x, pointLeftTop.y);
            path.cubicTo(pointLeftTop.x, pointLeftTop.y, animGapPointTop.x, animGapPointTop.y, pointRightTop.x, pointRightTop.y);
            path.lineTo(pointRightBottom.x, pointRightBottom.y);
            path.cubicTo(pointRightBottom.x, pointRightBottom.y, animGapPointBottom.x, animGapPointBottom.y, pointLeftBottom.x, pointLeftBottom.y);

        } else if (repeatCount == THIRD_TRAVERSE) {
            path.moveTo(pointRightTop.x, pointRightTop.y);
            path.cubicTo(pointRightTop.x, pointRightTop.y, animGapPointTop.x, currentDotCentre.y, pointRightBottom.x, pointRightBottom.y);

        }
        canvas.drawPath(path, movementPaint);

        if (repeatCount == FIRST_TRAVERSE) {
            canvas.drawCircle(pointLeftTop.x, pointLeftTop.y, 5, dotPaint);
            canvas.drawCircle(pointLeftBottom.x, pointLeftBottom.y, 5, dotPaint);
            canvas.drawCircle(animGapPointTop.x, currentDotCentre.y, 5, dotPaint);
        } else if (repeatCount == SECOND_TRAVERSE) {
            canvas.drawCircle(pointLeftTop.x, pointLeftTop.y, 5, dotPaint);
            canvas.drawCircle(animGapPointTop.x, animGapPointTop.y, 5, dotPaint);
            canvas.drawCircle(pointRightTop.x, pointRightTop.y, 5, dotPaint);
            canvas.drawCircle(pointRightBottom.x, pointRightBottom.y, 5, dotPaint);
            canvas.drawCircle(animGapPointBottom.x, animGapPointBottom.y, 5, dotPaint);
            canvas.drawCircle(pointLeftBottom.x, pointLeftBottom.y, 5, dotPaint);
        } else if (repeatCount == THIRD_TRAVERSE) {
            canvas.drawCircle(pointRightTop.x, pointRightTop.y, 5, dotPaint);
            canvas.drawCircle(animGapPointTop.x, currentDotCentre.y, 5, dotPaint);
            canvas.drawCircle(pointRightBottom.x, pointRightBottom.y, 5, dotPaint);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getPointFromRangeWithRatio(PointRange range, float ratio) {
        return (range.start + (int) ((range.end - range.start) * ratio));
    }

    private Point getPointOnCircleWithAngle(double angle, int radius, Point centre) {
        angle = Math.toRadians(angle);
        return new Point(centre.x + (int) (radius * Math.cos(angle)), centre.y + (int) (radius * Math.sin(angle)));
    }

    private Point getCentrePointOfCircleWithIndex(int index) {
        Point point = new Point();
        point.x = dotMarginOffset + currentDotRadius + (index * (2 * defaultDotRadius + dotSpacing));
        point.y = dotMarginOffset + currentDotRadius;
        return point;
    }

    private class PointRange {
        int start, end;

        public PointRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return "(" + start + ", " + end + ")";
        }
    }

}