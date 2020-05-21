/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import net.osmank3.labebe.R;

public class ImageTextProgressView extends ConstraintLayout implements View.OnClickListener {
    private ImageView imageView;
    private TextView textView;
    private TextView textProgress;
    private ProgressBar progressBar;

    public ImageTextProgressView(Context context) {
        super(context);
        init(context);
    }

    public ImageTextProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageTextProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View lineView = inflater.inflate(R.layout.image_text_progress_view, this);

        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.image);
        textProgress = findViewById(R.id.textProgress);
        progressBar = findViewById(R.id.progress);

        invalidate();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setText(int resid) {
        textView.setText(resid);
    }

    public void setImage(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void setImage(int resid) {
        imageView.setImageResource(resid);
    }

    public void setImageColor(int color) {
        imageView.setImageTintList(ColorStateList.valueOf(color));
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
        textProgress.setText("%" + progress);
    }

    @Override
    public void onClick(View v) {

    }
}
