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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import net.osmank3.labebe.R;

public class ImageTextView extends ConstraintLayout implements View.OnClickListener {
    private ImageView imageView;
    private TextView textView;

    public ImageTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ImageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        String text;
        Drawable image;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View lineView = inflater.inflate(R.layout.image_text_view, this);

        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.image);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageTextView);

            text = typedArray.getString(R.styleable.ImageTextSwitchView_text);
            image = typedArray.getDrawable(R.styleable.ImageTextSwitchView_image);

            textView.setText(text);
            imageView.setImageDrawable(image);
        } else {
            return;
        }

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

    @Override
    public void onClick(View v) {

    }
}
