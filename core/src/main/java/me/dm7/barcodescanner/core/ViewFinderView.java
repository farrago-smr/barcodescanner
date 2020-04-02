package me.dm7.barcodescanner.core;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;

    private static final float PORTRAIT_WIDTH_RATIO = 6.5f / 8;
    private static final float PORTRAIT_WIDTH_HEIGHT_RATIO = 0.75f;

    private static final float LANDSCAPE_HEIGHT_RATIO = 5f / 8;
    private static final float LANDSCAPE_WIDTH_HEIGHT_RATIO = 1.4f;
    private static final int MIN_DIMENSION_DIFF = 50;

    private static final float DEFAULT_SQUARE_DIMENSION_RATIO = 5f / 8;

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80l;

    private final int mDefaultLaserColor = getResources().getColor(R.color.viewfinder_laser);
    private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask);
    private final int mDefaultBorderColor = getResources().getColor(R.color.viewfinder_border);
    private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.viewfinder_border_width);
    private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.viewfinder_border_length);

    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint, mFinderClearPaint;
    protected Paint mBorderPaint, mOuterBorderPaint;
    protected int mBorderLineLength;
    protected boolean mSquareViewFinder;
    private boolean mIsLaserEnabled;
    private float mBordersAlpha;
    private int mViewFinderOffset = 0;

    private int mViewFinderOuterBorderWidth = 0;

    private final int mDefaultMaskBorderStrokeWidth = getResources().getInteger(R.integer.viewfinder_outer_border_width);

    private int borderOffset = mDefaultMaskBorderStrokeWidth / 2;

    private int mViewFinderTopOffset = 0;

    int mOuterCornerRadius;
    private int mViewFinderSideMargin = 0;

    @Override
    public void setViewFinderMeasureCallback(ViewFinderMeasureCallback viewFinderMeasureCallback) {
        this.viewFinderMeasureCallback = viewFinderMeasureCallback;
    }

    @Override
    public void setViewFinderMargin(int mViewFinderMargin) {
        this.mViewFinderSideMargin = mViewFinderMargin;
    }

    ViewFinderMeasureCallback viewFinderMeasureCallback;

    public ViewFinderView(Context context) {
        super(context);
        init();
    }

    public ViewFinderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        //set up laser paint
        mLaserPaint = new Paint();
        mLaserPaint.setColor(mDefaultLaserColor);
        mLaserPaint.setStyle(Paint.Style.FILL);

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);


        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);
        mBorderPaint.setAntiAlias(true);

        mOuterBorderPaint = new Paint(mBorderPaint);

        mFinderClearPaint = new Paint();
        mFinderClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mBorderLineLength = mDefaultBorderLineLength;
    }

    @Override
    public void setLaserColor(int laserColor) {
        mLaserPaint.setColor(laserColor);
    }

    @Override
    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }

    @Override
    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);

    }

    @Override
    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }

    @Override
    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    @Override
    public void setLaserEnabled(boolean isLaserEnabled) {
        mIsLaserEnabled = isLaserEnabled;
    }

    @Override
    public void setBorderCornerRounded(boolean isBorderCornersRounded) {
        if (isBorderCornersRounded) {
            mBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        } else {
            mBorderPaint.setStrokeJoin(Paint.Join.BEVEL);
        }
    }

    @Override
    public void setBorderAlpha(float alpha) {
        int colorAlpha = (int) (255 * alpha);
        mBordersAlpha = alpha;
        mBorderPaint.setAlpha(colorAlpha);
    }

    @Override
    public void setBorderCornerRadius(int borderCornersRadius) {
        mBorderPaint.setPathEffect(new CornerPathEffect(borderCornersRadius));

    }

    @Override
    public void setViewFinderOffset(int offset) {
        mViewFinderOffset = offset;
    }

    // TODO: Need a better way to configure this. Revisit when working on 2.0
    @Override
    public void setSquareViewFinder(boolean set) {
        mSquareViewFinder = set;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void setOuterBorderStrokeWidth(int outerBorderWidth) {
        mViewFinderOuterBorderWidth = outerBorderWidth;
        mOuterBorderPaint.setStrokeWidth(outerBorderWidth);
    }

    @Override
    public void setOuterBorderColor(int outerBorderColor) {
        mOuterBorderPaint.setColor(outerBorderColor);
    }

    @Override
    public void setOuterBorderCornerRadius(int outerCornerRadius) {
        this.mOuterCornerRadius = outerCornerRadius;
        mOuterBorderPaint.setPathEffect(new CornerPathEffect(outerCornerRadius));
        //mFinderClearPaint.setPathEffect(new CornerPathEffect(mOuterCornerRadius));
    }

    @Override
    public void setViewFinderTopOffset(int mViewFinderTopOffset) {
        this.mViewFinderTopOffset = mViewFinderTopOffset;
    }

    View bottomViewl;

    @Override
    public void setBottomView(View v) {
        bottomViewl = v;
        invalidate();
    }

    @Override
    public int getTotalPreviewHeight() {
        Rect frm = getFramingRect();
        if (frm != null) {
            return frm.bottom + mDefaultMaskBorderStrokeWidth + mViewFinderOuterBorderWidth;
        }
        return 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getFramingRect() == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
        drawViewFinderOuterBorder(canvas);

        drawBottom(canvas);

        if (mIsLaserEnabled) {
            drawLaser(canvas);
        }
        invalidate();
    }

    private void drawBottom(Canvas canvas) {
        if (bottomViewl == null) return;
        Rect frm = getFramingRect();
        Rect rect = new Rect();
        rect.set(0, frm.bottom + mDefaultMaskBorderStrokeWidth + mViewFinderOuterBorderWidth, getWidth(), getHeight());
        bottomViewl.measure(rect.width(), rect.height());
        bottomViewl.layout(rect.left, rect.top, rect.right, rect.bottom);
        //Translate the Canvas into position and draw it
        canvas.save();
        canvas.translate(rect.left, rect.top);
        bottomViewl.draw(canvas);
        canvas.restore();
    }

    public void drawViewFinderMask(Canvas canvas) {

        canvas.drawColor(mFinderMaskPaint.getColor());

        Rect framingRect = getFramingRect();

        canvas.drawRoundRect(framingRect.left, framingRect.top, framingRect.right, framingRect.bottom, mOuterCornerRadius, mOuterCornerRadius, mFinderClearPaint);

    }

    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = getFramingRect();

        // Top-left corner
        Path path = new Path();
        path.moveTo(framingRect.left + borderOffset, framingRect.top + borderOffset + mBorderLineLength);
        path.lineTo(framingRect.left + borderOffset, framingRect.top + borderOffset);
        path.lineTo(framingRect.left + borderOffset + mBorderLineLength, framingRect.top + borderOffset);
        canvas.drawPath(path, mBorderPaint);

        // Top-right corner
        path.moveTo(framingRect.right - borderOffset, framingRect.top + borderOffset + mBorderLineLength);
        path.lineTo(framingRect.right - borderOffset, framingRect.top + borderOffset);
        path.lineTo(framingRect.right - borderOffset - mBorderLineLength, framingRect.top + borderOffset);
        canvas.drawPath(path, mBorderPaint);

        // Bottom-right corner
        path.moveTo(framingRect.right - borderOffset, framingRect.bottom - borderOffset - mBorderLineLength);
        path.lineTo(framingRect.right - borderOffset, framingRect.bottom - borderOffset);
        path.lineTo(framingRect.right - borderOffset - mBorderLineLength, framingRect.bottom - borderOffset);
        canvas.drawPath(path, mBorderPaint);

        // Bottom-left corner
        path.moveTo(framingRect.left + borderOffset, framingRect.bottom - borderOffset - mBorderLineLength);
        path.lineTo(framingRect.left + borderOffset, framingRect.bottom - borderOffset);
        path.lineTo(framingRect.left + borderOffset + mBorderLineLength, framingRect.bottom - borderOffset);
        canvas.drawPath(path, mBorderPaint);
    }

    public void drawLaser(Canvas canvas) {
        Rect framingRect = getFramingRect();

        // Draw a red "laser scanner" line through the middle to show decoding is active
        mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = framingRect.height() / 2 + framingRect.top;
        canvas.drawRect(framingRect.left + 2, middle - 1, framingRect.right - 1, middle + 2, mLaserPaint);

        postInvalidateDelayed(ANIMATION_DELAY,
                framingRect.left - POINT_SIZE,
                framingRect.top - POINT_SIZE,
                framingRect.right + POINT_SIZE,
                framingRect.bottom + POINT_SIZE);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if (mSquareViewFinder) {
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (int) (getHeight() * DEFAULT_SQUARE_DIMENSION_RATIO);
                width = height;
            } else {
                width = (int) (getWidth() * DEFAULT_SQUARE_DIMENSION_RATIO);
                height = width;
            }
        } else {
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (int) (getHeight() * LANDSCAPE_HEIGHT_RATIO);
                width = (int) (LANDSCAPE_WIDTH_HEIGHT_RATIO * height);
            } else {
                // width = (int) (getWidth() * PORTRAIT_WIDTH_RATIO);
                width = getWidth() - 2 * (mViewFinderSideMargin + mViewFinderOuterBorderWidth + mDefaultMaskBorderStrokeWidth);
                height = (int) (PORTRAIT_WIDTH_HEIGHT_RATIO * width);
            }
        }

        if (width > getWidth()) {
            width = getWidth() - MIN_DIMENSION_DIFF;
        }

        if (height > getHeight()) {
            height = getHeight() - MIN_DIMENSION_DIFF;
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = mViewFinderOuterBorderWidth + mDefaultBorderStrokeWidth + mViewFinderTopOffset;
        mFramingRect = new Rect(leftOffset + mViewFinderOffset, topOffset + mViewFinderOffset, leftOffset + width - mViewFinderOffset, topOffset + height - mViewFinderOffset);
        if (viewFinderMeasureCallback != null) {
            viewFinderMeasureCallback.onMeasured();
        }
    }

    public void setBorderOffset(int borderOffset) {
        if (borderOffset > 70) {
            borderOffset = 70;
        }
        this.borderOffset = mDefaultMaskBorderStrokeWidth / 2 + borderOffset;
    }

    private void drawViewFinderOuterBorder(Canvas canvas) {
        if (mViewFinderOuterBorderWidth == 0) return;
        int correction = mViewFinderOuterBorderWidth / 2 + mDefaultMaskBorderStrokeWidth;

        Rect framingRect = getFramingRect();

        canvas.drawRect(framingRect.left - correction, framingRect.top - correction, framingRect.right + correction, framingRect.bottom + correction, mOuterBorderPaint);

    }

}

