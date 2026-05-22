package com.classpulse.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class BarcodeView extends View {

    private Bitmap barcodeBitmap;
    private String data = "CLASSPULSE";

    public BarcodeView(Context context) {
        super(context);
    }

    public BarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Call this with the user's name to generate their unique barcode.
     */
    public void setData(String text) {
        if (text == null || text.isEmpty()) text = "USER";
        this.data = text.toUpperCase();
        generateBarcode();
        invalidate();
    }

    private void generateBarcode() {
        int width  = getWidth()  > 0 ? getWidth()  : 800;
        int height = getHeight() > 0 ? getHeight() : 80;

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    data,
                    BarcodeFormat.CODE_128,
                    width,
                    height
            );

            barcodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    barcodeBitmap.setPixel(x, y,
                            bitMatrix.get(x, y) ? Color.BLACK : Color.TRANSPARENT);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        // Regenerate when size is known
        if (w > 0 && h > 0) generateBarcode();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (barcodeBitmap != null) {
            canvas.drawBitmap(barcodeBitmap, 0, 0, null);
        }
    }
}