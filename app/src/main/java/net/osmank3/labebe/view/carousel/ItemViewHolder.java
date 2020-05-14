/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view.carousel;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.osmank3.labebe.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    private final View view;
    public ItemViewHolder(View view) {
        super(view);
        this.view = view;
    }

    public final void bind(CarouselItem item) {
        TextView textView = this.view.findViewById(R.id.list_item_text);
        ImageView imageView = this.view.findViewById(R.id.list_item_icon);
        textView.setText(item.getTitle());
        textView.setTextColor(item.getColor());
        imageView.setImageResource(item.getIcon());
        imageView.setColorFilter(item.getColor());
    }
}
