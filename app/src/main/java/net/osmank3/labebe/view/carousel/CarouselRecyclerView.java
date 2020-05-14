/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view.carousel;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CarouselRecyclerView extends RecyclerView {
    private List<Integer> viewsToChangeColor;

    public CarouselRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CarouselRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CarouselRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        viewsToChangeColor = new ArrayList<>();
        setLayoutManager(new LinearLayoutManager(context, HORIZONTAL, false));
    }

    public final void initialize(CarouselItemAdapter adapter)    {
        adapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                post(new Runnable() {
                    @Override
                    public void run() {
                        Integer width = getWidth() / 2;
                        Integer childWidth = getChildAt(0).getWidth() / 2;
                        int sidePadding = width - childWidth;
                        setPadding(sidePadding, 0, sidePadding, 0);
                        scrollToPosition(0);
                        addOnScrollListener(new OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                onScrollChanged();
                            }
                        });
                    }
                });
            }
        });
        setAdapter(adapter);
    }

    public final void setViewsToChangeColor(List<Integer> viewIds) {
        viewsToChangeColor = viewIds;
    }

    private void onScrollChanged() {
        post(new Runnable() {
            @Override
            public void run() {
                for (int position = 0; position < getChildCount(); position++) {
                    View child = getChildAt(position);
                    Integer childCenterX = (child.getLeft() + child.getRight()) / 2;
                    Float scaleValue = getGaussianScale(childCenterX, 1.0, 1.0, 150.0);
                    child.setScaleX(scaleValue);
                    child.setScaleY(scaleValue);
                    colorView(child, scaleValue);
                }
            }
        });
    }

    private Float getGaussianScale(Integer childCenterX, Double minScaleOffest, Double scaleFactor, Double spreadFactor) {
        Integer recyclerCenterX = (this.getLeft() + this.getRight()) / 2;
        return (float) (Math.pow(
                2.718281828459045D,
                -Math.pow(
                        (double)childCenterX - (double)recyclerCenterX,
                        (double)2) / ((double)2 * Math.pow(
                                spreadFactor,
                                (double)2
                        ))
                ) * (double)scaleFactor + (double)minScaleOffest);
    }

    private void colorView(View child, Float scaleValue) {
        Float saturationPercent = (scaleValue - 1) / 1;
        Float alphaPercent = scaleValue / 2;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturationPercent);
        for (Integer viewId: viewsToChangeColor) {
            View viewToChangeColor = child.findViewById(viewId);
            if (viewToChangeColor instanceof ImageView) {
                ((ImageView) viewToChangeColor).setColorFilter(new ColorMatrixColorFilter(matrix));
                ((ImageView) viewToChangeColor).setImageAlpha((int)(alphaPercent * 255));
            } else if (viewToChangeColor instanceof TextView) {
                /*Maybe I think text color changes later
                 *
                 *ArgbEvaluator evaluator = new ArgbEvaluator();
                 *((TextView) viewToChangeColor).setTextColor((int) evaluator.evaluate(saturationPercent, getResources().getColor(R.color.colorPrimary), Color.BLACK));
                 */
            }
        }
    }

}
