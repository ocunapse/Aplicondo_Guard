package com.ocunapse.aplicondo.guard.barcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.view.View;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

public final class BarcodeBoxView extends View {
    private final Paint paint;
    private RectF mRect;

    protected void onDraw(@NotNull Canvas canvas) {
        super.onDraw(canvas);
        float cornerRadius = 10.0F;
        this.paint.setStyle(Style.STROKE);
        this.paint.setColor(Color.RED);
        this.paint.setStrokeWidth(5.0F);
        canvas.drawRoundRect(this.mRect, cornerRadius, cornerRadius, this.paint);
    }

    public final void setRect(@NotNull RectF rect) {
        this.mRect = rect;
        this.invalidate();
        this.requestLayout();
    }

    public BarcodeBoxView(@NotNull Context context) {
        super(context);
        this.paint = new Paint();
        this.mRect = new RectF();
    }
}
