package com.classpulse.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer {

    private final float mRadius;
    private final RectF mBarShadowRectBuffer = new RectF();

    public RoundedBarChartRenderer(BarChart chart, ChartAnimator animator,
                                   ViewPortHandler viewPortHandler, float radius) {
        super(chart, animator, viewPortHandler);
        this.mRadius = radius;
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setStyle(Paint.Style.FILL);

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        BarData barData = mChart.getBarData();
        float barWidthHalf = barData.getBarWidth() / 2f;

        int count = (int) Math.ceil(dataSet.getEntryCount() * phaseX);

        for (int i = 0; i < count; i++) {
            BarEntry e = dataSet.getEntryForIndex(i);

            float x = e.getX();
            float left   = x - barWidthHalf;
            float right  = x + barWidthHalf;
            float top    = e.getY() >= 0 ? e.getY() * phaseY : 0f;
            float bottom = 0f;

            mBarRect.set(left, top, right, bottom);
            trans.rectToPixelPhase(mBarRect, phaseY);

            if (!mViewPortHandler.isInBoundsLeft(mBarRect.right))  continue;
            if (!mViewPortHandler.isInBoundsRight(mBarRect.left))  break;

            mRenderPaint.setColor(dataSet.getColor(i));
            mRenderPaint.setStyle(Paint.Style.FILL);

            // Draw fully rounded rect (top-left, top-right rounded; bottom flat)
            float r = Math.min(mRadius, (mBarRect.bottom - mBarRect.top) / 2f);

            // Draw rounded rect for the full bar
            c.drawRoundRect(mBarRect, r, r, mRenderPaint);

            // Overdraw the bottom half with a plain rect to "flatten" bottom corners
            if (mBarRect.height() > r) {
                RectF bottomFill = new RectF(
                        mBarRect.left,
                        mBarRect.top + r,
                        mBarRect.right,
                        mBarRect.bottom);
                c.drawRect(bottomFill, mRenderPaint);
            }
        }
    }
}