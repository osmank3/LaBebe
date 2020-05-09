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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import net.osmank3.labebe.R;

public class TitledListView extends ConstraintLayout {
    private TextView title;
    private Button button;
    private LinearLayout list;
    private ScrollView scroll;

    public TitledListView(Context context) {
        super(context);
        init(context, null);
    }

    public TitledListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TitledListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attrs) {
        String text;
        Boolean buttonStatus;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View listView = inflater.inflate(R.layout.titled_list_view, this);

        title = findViewById(R.id.title);
        button = findViewById(R.id.button);
        list = findViewById(R.id.list);
        scroll = findViewById(R.id.scroll);


        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitledListView);

            text = typedArray.getString(R.styleable.TitledListView_title);
            buttonStatus = typedArray.getBoolean(R.styleable.TitledListView_buttonStatus, false);

            setTitle(text);
            showButton(buttonStatus);
        } else {
            return;
        }

        invalidate();
    }

    public void setTitle(String text) {
        if (text == null) {
            title.setVisibility(GONE);
        } else {
            title.setText(text);
            title.setVisibility(VISIBLE);
        }
    }

    public void setTitle(int resid) {
        title.setText(resid);
        title.setVisibility(VISIBLE);
    }

    public void addToList(View view) {
        list.addView(view);
        invalidate();
    }

    public void clearList() {
        list.removeAllViews();
    }

    public void setOnButtonClick(View.OnClickListener listener) {
        button.setOnClickListener(listener);
    }

    public void showButton(Boolean status){
        LayoutParams params = (LayoutParams) scroll.getLayoutParams();
        if (status) {
            button.setVisibility(VISIBLE);
            params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.margin_normal), 0, getResources().getDimensionPixelSize(R.dimen.margin_normal));
        } else {
            button.setVisibility(GONE);
            params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.margin_normal), 0, 0);
        }
        scroll.setLayoutParams(params);
    }
}
