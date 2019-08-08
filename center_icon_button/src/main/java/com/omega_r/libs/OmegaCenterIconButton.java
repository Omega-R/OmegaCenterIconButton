package com.omega_r.libs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.drawable.DrawableCompat;

import com.omega_r.libs.centericonbutton.R;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class OmegaCenterIconButton extends AppCompatButton {

    private static final String DELIMITERS = "\n";
    private static final int DRAWABLE_LEFT_POSITION = 0;
    private static final int DRAWABLE_TOP_POSITION = 1;
    private static final int DRAWABLE_RIGHT_POSITION = 2;
    private static final int DRAWABLE_BOTTOM_POSITION = 3;
    private static final int DRAWABLES_LENGTH = 4;

    private Rect textBoundsRect;
    @ColorInt
    private int mTintColor = Color.TRANSPARENT;
    private int mLeftPadding;
    private int mRightPadding;
    private int mDrawableSize;

    public OmegaCenterIconButton(Context context) {
        super(context);
        init(context, null);
    }

    public OmegaCenterIconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OmegaCenterIconButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OmegaCenterIconButton);
            mTintColor = typedArray.getColor(R.styleable.OmegaCenterIconButton_drawableTint, Color.TRANSPARENT);
            mDrawableSize = typedArray.getDimensionPixelSize(R.styleable.OmegaCenterIconButton_drawableSize, -1);

            float defaultDrawablePadding = getResources().getDimension(R.dimen.omega_default_drawable_padding);
            int drawablePadding = (int) typedArray.getDimension(R.styleable.OmegaCenterIconButton_android_drawablePadding, defaultDrawablePadding);
            setCompoundDrawablePadding(drawablePadding);

            updateDrawables();
            typedArray.recycle();
        }
        mLeftPadding = getPaddingLeft();
        mRightPadding = getPaddingRight();
    }

    private void updateDrawables() {
        if (mTintColor != Color.TRANSPARENT || mDrawableSize != -1) {
            Drawable[] drawables = getCompoundDrawables();
            if (drawables.length != DRAWABLES_LENGTH) return;

            Drawable[] wrappedDrawables = new Drawable[DRAWABLES_LENGTH];
            for (int i = 0; i < DRAWABLES_LENGTH; i++) {
                Drawable drawable = drawables[i];
                if (drawable != null) {
                    Drawable wrappedDrawable = drawable;
                    if (mTintColor != Color.TRANSPARENT) {
                        wrappedDrawable = getTintedDrawable(wrappedDrawable);
                    }
                    if (mDrawableSize > 0) {
                        wrappedDrawable = updateDrawableBounds(wrappedDrawable);
                    }
                    wrappedDrawables[i] = wrappedDrawable;
                }
            }
            if (mDrawableSize > 0) {
                setCompoundDrawables(wrappedDrawables[DRAWABLE_LEFT_POSITION],
                                     wrappedDrawables[DRAWABLE_TOP_POSITION],
                                     wrappedDrawables[DRAWABLE_RIGHT_POSITION],
                                     wrappedDrawables[DRAWABLE_BOTTOM_POSITION]);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(wrappedDrawables[DRAWABLE_LEFT_POSITION],
                                                        wrappedDrawables[DRAWABLE_TOP_POSITION],
                                                        wrappedDrawables[DRAWABLE_RIGHT_POSITION],
                                                        wrappedDrawables[DRAWABLE_BOTTOM_POSITION]);
            }
        }
    }

    @NonNull
    private Drawable getTintedDrawable(@NonNull Drawable drawable) {
        Drawable mutate = DrawableCompat.wrap(drawable).mutate();
        DrawableCompat.setTint(mutate, mTintColor);
        return mutate;
    }

    @NonNull
    private Drawable updateDrawableBounds(@NonNull Drawable drawable) {
        drawable.getBounds().set(0, 0, mDrawableSize, mDrawableSize);
        return drawable;
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        updatePadding();
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        updatePadding();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        updatePadding();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePadding(w);
    }

    private void updatePadding() {
        updatePadding(getMeasuredWidth());
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        mLeftPadding = left;
        mRightPadding = right;
        updatePadding();
    }

    private void updatePadding(int width) {
        if (width == 0) return;

        Drawable[] compoundDrawables = getCompoundDrawables();
        if (compoundDrawables.length != DRAWABLES_LENGTH) return;

        Drawable leftDrawable = compoundDrawables[DRAWABLE_LEFT_POSITION];
        Drawable rightDrawable = compoundDrawables[DRAWABLE_RIGHT_POSITION];
        if (leftDrawable == null && rightDrawable == null) return;

        int textWidth = getTextWidth();
        int iconPadding = Math.max(getCompoundDrawablePadding(), 1);
        int paddingSize;

        int leftWidth = leftDrawable == null ? 0 : leftDrawable.getBounds().width();
        int rightWidth = rightDrawable == null ? 0 : rightDrawable.getBounds().width();

        if (leftDrawable != null && rightDrawable != null) {
            paddingSize = (width - leftWidth - rightWidth - textWidth - iconPadding * 4) / 2;
        } else if (leftDrawable != null) {
            paddingSize = (width - leftWidth - iconPadding * 2 - textWidth) / 2;
        } else {
            paddingSize = (width - rightWidth - iconPadding * 2 - textWidth) / 2;
        }

        super.setPadding(Math.max(mLeftPadding, paddingSize), getPaddingTop(), Math.max(paddingSize, mRightPadding), getPaddingBottom());
    }

    private int getTextWidth() {
        if (textBoundsRect == null) {
            textBoundsRect = new Rect();
        }
        Paint paint = getPaint();
        String text = divideText();
        paint.getTextBounds(text, 0, text.length(), textBoundsRect);
        return textBoundsRect.width();
    }

    private String divideText() {
        String text = getText().toString();
        if (text.isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(text, DELIMITERS, false);
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        if (list.size() == 1) {
            return isAllCaps() ? list.get(0).toUpperCase() : list.get(0);
        }
        String longPart = list.get(0);
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i + 1).length() > list.get(i).length()) {
                longPart = list.get(i + 1);
            }
        }

        return isAllCaps() ? longPart.toUpperCase() : longPart;
    }

    public boolean isAllCaps() {
        TransformationMethod method = getTransformationMethod();
        if (method == null) return false;

        return method.getClass().getSimpleName().equals("AllCapsTransformationMethod");
    }
}