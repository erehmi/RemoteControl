package com.github.mo0n1andin.remotecontrol;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author WhatsAndroid
 * @date 2019
 */
public class ArrowKeyGroup extends View {
    private static final String TAG = "ArrowKeyGroup";

    public static final int BUTTON_RIGHT = 0;
    public static final int BUTTON_DOWN = 1;
    public static final int BUTTON_LEFT = 2;
    public static final int BUTTON_UP = 3;
    public static final int BUTTON_OK = 4;
    public static final int BUTTON_NONE = -1;

    @IntDef({BUTTON_LEFT, BUTTON_UP, BUTTON_RIGHT, BUTTON_DOWN, BUTTON_OK, BUTTON_NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Button {}

    private Matrix mMatrix;
    private Path mPath;
    private Region mRegion;

    private Drawable mButtonDrawable;
    private ColorStateList mButtonBackgroundColor;
    private Path[] mButtonPaths;
    private Region[] mButtonRegions;
    private ShapeDrawable[] mButtonBackgrounds;
    private Drawable[] mButtonBackgroundsW;
    private Matrix mButtonMatrix;

    private Drawable mOkButtonDrawable;
    private ColorStateList mOkButtonBackgroundColor;
    private CharSequence mOkButtonText;
    private Path mOkButtonPath;
    private Region mOkButtonRegion;
    private ShapeDrawable mOkButtonBackground;
    private Drawable mOkButtonBackgroundW;

    private int mMarginBetweenButtons;

    private float mInnerRadiusRatio;
    private float mThicknessRatio;
    private int mInnerRadius = -1;
    private int mThickness = -1;

    private int mDefaultInnerRadius;
    private int mDefaultThickness;
    private int mDefaultBlurRadius;

    private long mLongClickTickInterval;
    private Runnable mLongClickTickRunnable;

    private int mMaxButtonImageSize;
    private int mMaxOkButtonImageSize;

    private Paint mBlurPaint;

    private @Button
    int mCurrentPressedButton = BUTTON_NONE;

    private OnButtonClickListener mOnButtonClickListener;
    private OnButtonLongClickListener mOnButtonLongClickListener;

    public ArrowKeyGroup(Context context) {
        this(context, null);
    }

    public ArrowKeyGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArrowKeyGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public ArrowKeyGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Resources resources = getResources();
        mDefaultInnerRadius = resources.getDimensionPixelSize(R.dimen.arrow_key_group_inner_radius);
        mDefaultThickness = resources.getDimensionPixelSize(R.dimen.arrow_key_group_thickness);
        mDefaultBlurRadius = resources.getDimensionPixelSize(R.dimen.arrow_key_group_blur_radius);
        mLongClickTickInterval = resources.getInteger(android.R.integer.config_longAnimTime);

        mMatrix = new Matrix();
        mPath = new Path();
        mRegion = new Region();

        mButtonMatrix = new Matrix();

        mButtonPaths = new Path[4];
        mButtonRegions = new Region[4];
        mButtonBackgrounds = new ShapeDrawable[4];
        for (int i = 0; i < 4; ++i) {
            mButtonPaths[i] = new Path();
            mButtonRegions[i] = new Region();
            mButtonBackgrounds[i] = new ShapeDrawable();
        }

        mOkButtonPath = new Path();
        mOkButtonRegion = new Region();
        mOkButtonBackground = new ShapeDrawable();

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ArrowKeyGroup, defStyleAttr, defStyleRes);
        int attrCount = a.getIndexCount();
        for (int i = 0; i < attrCount; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ArrowKeyGroup_button) {
                Drawable button = a.getDrawable(attr);
                if (button != null) {
                    setButtonDrawable(button);
                }
            } else if (attr == R.styleable.ArrowKeyGroup_buttonBackgroundColor) {
                ColorStateList buttonBackgroundColor = a.getColorStateList(attr);
                if (buttonBackgroundColor != null) {
                    setButtonBackgroundColor(buttonBackgroundColor);
                }
            } else if (attr == R.styleable.ArrowKeyGroup_okButton) {
                Drawable okButton = a.getDrawable(attr);
                if (okButton != null) {
                    setOkButtonDrawable(okButton);
                }
            } else if (attr == R.styleable.ArrowKeyGroup_okButtonBackgroundColor) {
                ColorStateList okButtonBackgroundColor = a.getColorStateList(attr);
                if (okButtonBackgroundColor != null) {
                    setOkButtonBackgroundColor(okButtonBackgroundColor);
                }
            } else if (attr == R.styleable.ArrowKeyGroup_okButtonText) {
                setOkButtonText(a.getText(attr));
            } else if (attr == R.styleable.ArrowKeyGroup_innerRadius) {
                setInnerRadius(a.getDimensionPixelSize(attr, -1));
            } else if (attr == R.styleable.ArrowKeyGroup_innerRadiusRatio) {
                setInnerRadiusRatio(a.getFloat(attr, 0));
            } else if (attr == R.styleable.ArrowKeyGroup_thickness) {
                setThickness(a.getDimensionPixelSize(attr, -1));
            } else if (attr == R.styleable.ArrowKeyGroup_thicknessRatio) {
                setThicknessRatio(a.getFloat(attr, 0));
            } else if (attr == R.styleable.ArrowKeyGroup_useDefaultMargin) {
                setUseDefaultMargin(a.getBoolean(attr, false));
            }
        }
        a.recycle();

        // TODO use defStyleRes
        if (mButtonBackgroundColor == null) {
            setButtonBackgroundColor(ContextCompat.getColorStateList(context, R.color.arrow_key_button));
        }
    }

    public void setAntiAliasWhenDefaultMarginNotEnabled(boolean antiAlias) {
        // TODO opt
        setRotation(antiAlias ? 45.f : 0.f);
    }

    public void setUseDefaultMargin(boolean useDefaultMargin) {
        if (useDefaultMargin && mMarginBetweenButtons != 0
                || !useDefaultMargin && mMarginBetweenButtons == 0) {
            return;
        }

        Resources resources = getResources();
        mMarginBetweenButtons = useDefaultMargin
                ? resources.getDimensionPixelOffset(R.dimen.arrow_key_group_default_margin)
                : 0;
        requestLayout();
        invalidate();
    }

    public void setInnerRadius(int innerRadius) {
        if (mInnerRadius == innerRadius) {
            return;
        }

        mInnerRadius = innerRadius;
        requestLayout();
        invalidate();
    }

    public void setInnerRadiusRatio(float innerRadiusRatio) {
        if (mInnerRadiusRatio == innerRadiusRatio) {
            return;
        }

        mInnerRadiusRatio = innerRadiusRatio;
        requestLayout();
        invalidate();
    }

    public void setThickness(int thickness) {
        if (mThickness == thickness) {
            return;
        }

        mThickness = thickness;
        requestLayout();
        invalidate();
    }

    public void setThicknessRatio(float thicknessRatio) {
        if (mThicknessRatio == thicknessRatio) {
            return;
        }

        mThicknessRatio = thicknessRatio;
        requestLayout();
        invalidate();
    }

    public void setButtonDrawable(Drawable drawable) {
        if (mButtonDrawable == drawable) {
            return;
        }

        mButtonDrawable = drawable;
        invalidate();
    }

    public void setButtonBackgroundColor(int color) {
        setButtonBackgroundColor(ColorStateList.valueOf(color));
    }

    public void setButtonBackgroundColor(ColorStateList color) {
        if (mButtonBackgroundColor == color) {
            return;
        }

        mButtonBackgroundColor = color;

        mButtonBackgroundsW = new Drawable[mButtonBackgrounds.length];
        for (int i = 0; i < mButtonBackgrounds.length; ++i) {
            Drawable drawable = mButtonBackgrounds[i];
            Drawable wrapped = mButtonBackgroundsW[i] = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(wrapped, color);
        }

        invalidate();
    }

    public void setOkButtonDrawable(Drawable drawable) {
        if (mOkButtonDrawable == drawable) {
            return;
        }

        mOkButtonDrawable = drawable;
        invalidate();
    }

    public void setOkButtonBackgroundColor(int color) {
        setOkButtonBackgroundColor(ColorStateList.valueOf(color));
    }

    public void setOkButtonBackgroundColor(ColorStateList color) {
        if (mOkButtonBackgroundColor == color) {
            return;
        }

        mOkButtonBackgroundColor = color;
        mOkButtonBackgroundW = DrawableCompat.wrap(mOkButtonBackground);
        DrawableCompat.setTintList(mOkButtonBackgroundW, color);
        invalidate();
    }

    public void setOkButtonText(CharSequence text) {
        // TODO draw text
        if (TextUtils.isEmpty(text) || TextUtils.equals(mOkButtonText, text)) {
            return;
        }

        mOkButtonText = text;
        requestLayout();
        invalidate();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        //super.setOnClickListener(listener);
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mOnButtonClickListener = listener;
    }

    public void setOnButtonLongClickListener(OnButtonLongClickListener listener) {
        mOnButtonLongClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure size
        int paddingStart = ViewCompat.getPaddingStart(this);
        int paddingEnd = ViewCompat.getPaddingEnd(this);
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int marginBetweenButtons = mMarginBetweenButtons;

        int innerRadius = mInnerRadius;
        int thickness = mThickness;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if ((widthSpecMode == MeasureSpec.EXACTLY || heightSpecMode == MeasureSpec.EXACTLY)
                && (innerRadius == -1 || thickness == -1)) {
            int width = getDefaultSize(0, widthMeasureSpec);
            int height = getDefaultSize(0, heightMeasureSpec);
            int unused = Math.min(width, height) - (paddingStart + paddingEnd) - (marginBetweenButtons * 2);
            if (innerRadius > 0) {
                unused -= innerRadius;
            }
            if (thickness > 0) {
                unused -= thickness;
            }
            float innerRadiusRatio = mInnerRadiusRatio;
            float thicknessRatio = mThicknessRatio;
            if (innerRadiusRatio <= 0 && thicknessRatio <= 0) {
                innerRadiusRatio = 1;
                thicknessRatio = 1;
            }
            float ratio = innerRadiusRatio + thicknessRatio;
            if (innerRadius == -1) {
                innerRadius = (int) ((unused * innerRadiusRatio) / (ratio * 2));
            }
            if (thickness == -1) {
                thickness = (int) ((unused * thicknessRatio) / (ratio * 2));
            }
        }

        if (innerRadius == -1) {
            innerRadius = mDefaultInnerRadius;
        }
        if (thickness == -1) {
            thickness = mDefaultThickness;
        }

        int expectedSize = (innerRadius + thickness + marginBetweenButtons) * 2;
        int expectedWidth = expectedSize + paddingStart + paddingEnd;
        int expectedHeight = expectedSize + paddingTop + paddingBottom;
        if (canDrawBlur()) {
            expectedWidth += mDefaultBlurRadius * 2;
            expectedHeight += mDefaultBlurRadius * 2;
        }

        int measuredWidth = resolveSizeAndState(expectedWidth, widthMeasureSpec, 0);
        int measuredHeight = resolveSizeAndState(expectedHeight, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);

        // Compute OK-button & buttons regions
        int outerRadius = expectedSize / 2;
        setViewShape(outerRadius);
        setOkButtonShape(innerRadius);
        for (int i = BUTTON_RIGHT; i <= BUTTON_UP; ++i) {
            setButtonShape(i, outerRadius, thickness);
        }

        mMaxOkButtonImageSize = innerRadius * 2 / 3;
        mMaxButtonImageSize = thickness * 2 / 3;
    }

    private void setViewShape(int radius) {
        mPath.reset();
        mPath.addCircle(0, 0, radius, Direction.CW);
        mRegion.set(-radius, -radius, radius, radius);
    }

    private void setOkButtonShape(float radius) {
        mOkButtonPath.reset();
        mOkButtonPath.addCircle(0, 0, radius, Direction.CW);
        mOkButtonRegion.setPath(mOkButtonPath, mRegion);

        updateShapeDrawable(mOkButtonRegion, mOkButtonPath, mOkButtonBackground);
    }

    private void updateShapeDrawable(Region region, Path path, ShapeDrawable drawable) {
        Rect rect = region.getBounds();
        int width = rect.width();
        int height = rect.height();
        drawable.setShape(new PathShape(path, width, height));
        drawable.setIntrinsicWidth(width);
        drawable.setIntrinsicHeight(height);
        drawable.setBounds(0, 0, width, height);

        Paint paint = drawable.getPaint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
    }

    private void setButtonShape(@Button int button, int radius, int thickness) {
        Path path = mButtonPaths[button];
        Region region = mButtonRegions[button];
        ShapeDrawable background = mButtonBackgrounds[button];

        if (button == BUTTON_RIGHT) {
            float margin = mMarginBetweenButtons;
            float outerArcAngle = computeButtonAngle(radius, margin);
            float innerArcAngle = computeButtonAngle(radius - thickness, margin);
            float outerStartAngle = 360 - (outerArcAngle / 2);
            float innerStartAngle = (innerArcAngle / 2);

            RectF outerRect = new RectF(-radius, -radius, radius, radius);
            RectF innerRect = new RectF(outerRect);
            innerRect.inset(thickness, thickness);

            path.reset();
            path.addArc(outerRect, outerStartAngle, outerArcAngle);
            path.arcTo(innerRect, innerStartAngle, -innerArcAngle);
            path.close();
        } else {
            Path benchmark = mButtonPaths[button - 1];
            path.set(benchmark);
            mButtonMatrix.setRotate(90, 0, 0);
            path.transform(mButtonMatrix);
        }
        region.setPath(path, mRegion);
        updateShapeDrawable(region, path, background);
    }

    private float computeButtonAngle(float radius, float paddingBetweenButtons) {
        double length = radius * Math.PI;
        return (float) (90f * (length - paddingBetweenButtons * 2) / length);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] state = getDrawableState();
        int button = mCurrentPressedButton;
        Drawable drawable = null;
        switch (button) {
            case BUTTON_RIGHT:
            case BUTTON_DOWN:
            case BUTTON_LEFT:
            case BUTTON_UP:
                drawable = getButtonBackgroundW(button);
                break;

            case BUTTON_OK:
                drawable = mOkButtonBackgroundW;
                break;
        }

        if (drawable != null) {
            drawable.setState(state);
        }
    }

    private Drawable getButtonBackgroundW(@Button int button) {
        return mButtonBackgroundsW != null ? mButtonBackgroundsW[button] : null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        transformCanvasCoordinate(canvas);
        drawButtons(canvas);
        drawOkButton(canvas);
    }

    private void transformCanvasCoordinate(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        canvas.translate(width / 2, height / 2);
        canvas.rotate(-getRotation());

        if (mMatrix.isIdentity()) {
            canvas.getMatrix().invert(mMatrix);
        }
    }

    private void drawButtons(Canvas canvas) {
        ensureBlurPaint();
        int marginBetweenButtons = mMarginBetweenButtons;
        if (willDrawBlur() && marginBetweenButtons <= 0) {
            canvas.drawPath(mPath, mBlurPaint);
        }

        Drawable drawable = mButtonDrawable;
        int canvasOffsetX = 0;
        int canvasOffsetY = 0;
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            int maxSize = Math.max(width, height);
            if (maxSize > mMaxButtonImageSize) {
                float ratio = (float) mMaxButtonImageSize / maxSize;
                int dw = (int) (width * ratio);
                int dh = (int) (height * ratio);
                drawable.setBounds(0, 0, dw, dh);
            } else {
                drawable.setBounds(0, 0, width, height);
            }

            Rect circumscribedRect = mRegion.getBounds();
            Rect okButtonRect = mOkButtonRegion.getBounds();
            Rect drawableBounds = drawable.getBounds();
            int thickness = (circumscribedRect.width() - okButtonRect.width()) / 2 - marginBetweenButtons;
            canvasOffsetX = (okButtonRect.width() + thickness - drawableBounds.width()) / 2 + marginBetweenButtons;
            canvasOffsetY = drawableBounds.height() / 2;
        }

        int canvasRotate = 0;
        for (int i = BUTTON_RIGHT; i <= BUTTON_UP; ++i) {
            Path path = mButtonPaths[i];
            if (willDrawBlur() && marginBetweenButtons > 0) {
                canvas.drawPath(path, mBlurPaint);
            }

            Drawable backgroundW = getButtonBackgroundW(i);
            Drawable background = backgroundW != null ? backgroundW : mButtonBackgrounds[i];
            int width = background.getIntrinsicWidth();
            int height = background.getIntrinsicHeight();
            background.setBounds(0, 0, width, height);
            background.draw(canvas);

            if (drawable != null) {
                int rotate = canvasRotate;
                canvasRotate += 90;
                canvas.save();
                canvas.rotate(rotate);
                canvas.translate(canvasOffsetX, -canvasOffsetY);
                drawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    private void drawOkButton(Canvas canvas) {
        // draw background
        ensureBlurPaint();
        if (willDrawBlur()) {
            canvas.drawPath(mOkButtonPath, mBlurPaint);
        }

        Drawable backgroundW = mOkButtonBackgroundW;
        Drawable background = backgroundW != null ? backgroundW : mOkButtonBackground;
        int width = background.getIntrinsicWidth();
        int height = background.getIntrinsicHeight();
        background.setBounds(0, 0, width, height);
        background.draw(canvas);

        // draw image
        Drawable drawable = mOkButtonDrawable;
        if (drawable != null) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            int maxSize = Math.max(drawableWidth, drawableHeight);
            if (maxSize > mMaxOkButtonImageSize) {
                float ratio = (float) mMaxOkButtonImageSize / maxSize;
                int dw = (int) (drawableWidth * ratio);
                int dh = (int) (drawableHeight * ratio);
                drawable.setBounds(0, 0, dw, dh);
            } else {
                drawable.setBounds(0, 0, drawableWidth, drawableHeight);
            }

            canvas.save();
            Rect drawableBounds = drawable.getBounds();
            canvas.translate(-drawableBounds.width() / 2, -drawableBounds.height() / 2);
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    private void ensureBlurPaint() {
        if (getLayerType() == LAYER_TYPE_SOFTWARE && mBlurPaint == null) {
            Context context = getContext();
            int color = ContextCompat.getColor(context, android.R.color.darker_gray);
            color = Color.argb(0x7f, Color.red(color), Color.green(color), Color.blue(color));

            mBlurPaint = new Paint();
            mBlurPaint.setAntiAlias(true);
            mBlurPaint.setDither(true);
            mBlurPaint.setColor(color);
            mBlurPaint.setStyle(Paint.Style.FILL);
            mBlurPaint.setMaskFilter(new BlurMaskFilter(mDefaultBlurRadius, Blur.OUTER));
        }
    }

    private boolean canDrawBlur() {
        return (getLayerType() == LAYER_TYPE_SOFTWARE);
    }

    private boolean willDrawBlur() {
        return (getLayerType() == LAYER_TYPE_SOFTWARE && mBlurPaint != null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float[] point = {event.getX(), event.getY()};
        mMatrix.mapPoints(point);
        float x = point[0];
        float y = point[1];
        int button = getPressedButton(x, y);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setPressedButton(button);
                buttonLongClickTick();
                break;

            case MotionEvent.ACTION_UP:
                cancelButtonLongClickTick();
                if (isPressed() && mCurrentPressedButton != BUTTON_NONE) {
                    buttonClick();
                }
                setPressedButton(BUTTON_NONE);
                break;

            case MotionEvent.ACTION_CANCEL:
                cancelButtonLongClickTick();
                setPressedButton(BUTTON_NONE);
                break;

            case MotionEvent.ACTION_MOVE:
                if (button == BUTTON_NONE || mCurrentPressedButton != button) {
                    cancelButtonLongClickTick();
                    setPressedButton(BUTTON_NONE);
                }
                break;
        }
        return true;
    }

    private @Button
    int getPressedButton(float x, float y) {
        for (int i = BUTTON_RIGHT; i <= BUTTON_UP; ++i) {
            if (mButtonRegions[i].contains((int) x, (int) y)) {
                return i;
            }
        }
        if (mOkButtonRegion.contains((int) x, (int) y)) {
            return BUTTON_OK;
        }
        return BUTTON_NONE;
    }

    private void setPressedButton(int button) {
        if (button != BUTTON_NONE) {
            mCurrentPressedButton = button;
            setPressed(true);
        } else {
            setPressed(false);
            mCurrentPressedButton = button;
        }
        invalidate();
    }

    private void buttonClick() {
        int button = mCurrentPressedButton;
        Log.d(TAG, "Button clicked: " + getButtonName(button));

        if (mOnButtonClickListener != null) {
            mOnButtonClickListener.onButtonClick(this, button);
        }
    }

    private void buttonLongClickTick() {
        int button = mCurrentPressedButton;
        Log.d(TAG, "Button long-click tick: " + getButtonName(button));

        ensureLongClickTickRunnable();
        postDelayed(mLongClickTickRunnable, mLongClickTickInterval);
        if (mOnButtonLongClickListener != null && button != BUTTON_NONE) {
            mOnButtonLongClickListener.onButtonTick(this, button);
        }
    }

    private String getButtonName(@Button final int button) {
        final String[] texts = {"Right", "Down", "Left", "UP", "OK"};
        if (button != BUTTON_NONE) {
            return texts[button];
        }
        return "none.";
    }

    private void ensureLongClickTickRunnable() {
        if (mLongClickTickRunnable == null) {
            mLongClickTickRunnable = new Runnable() {
                @Override
                public void run() {
                    buttonLongClickTick();
                }
            };
        }
    }

    private void cancelButtonLongClickTick() {
        if (mLongClickTickRunnable != null) {
            removeCallbacks(mLongClickTickRunnable);
        }
    }

    public interface OnButtonClickListener {
        void onButtonClick(ArrowKeyGroup v, @Button int button);
    }

    public interface OnButtonLongClickListener {
        void onButtonTick(ArrowKeyGroup v, @Button int button);
    }
}
