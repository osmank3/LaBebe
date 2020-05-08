/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import net.osmank3.labebe.R;

public class ImageTextSwitchView extends ConstraintLayout {
    private ImageView imageView;
    private TextView textView;
    private Switch checkSwitch;

    public ImageTextSwitchView(Context context) {
        super(context);
        init(context, null);
    }

    public ImageTextSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageTextSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        String text;
        Boolean status;
        Drawable image;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View lineView = inflater.inflate(R.layout.image_text_switch_view, this);

        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.image);
        checkSwitch = findViewById(R.id.checkSwitch);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageTextSwitchView);

            text = typedArray.getString(R.styleable.ImageTextSwitchView_text);
            status = typedArray.getBoolean(R.styleable.ImageTextSwitchView_status, false);
            image = typedArray.getDrawable(R.styleable.ImageTextSwitchView_image);

            textView.setText(text);
            checkSwitchOn(status);
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

    public void checkSwitchOn(Boolean status) {
        checkSwitch.setChecked(status);
    }

    public void setOnChangeSwitch(CompoundButton.OnCheckedChangeListener listener) {
        checkSwitch.setOnCheckedChangeListener(listener);
    }

    public void setOnClick(OnClickListener listener) {
        textView.setOnClickListener(listener);
        imageView.setOnClickListener(listener);
    }
}
