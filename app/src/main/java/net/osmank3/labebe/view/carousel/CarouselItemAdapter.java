/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view.carousel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.osmank3.labebe.R;

import java.util.List;

public class CarouselItemAdapter extends RecyclerView.Adapter {
    private List<CarouselItem> items;
    private CarouselRecyclerView creator;

    public CarouselItemAdapter(CarouselRecyclerView creator) {
        this.creator = creator;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemHolderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_list_item, parent, false);
        return new ItemViewHolder(itemHolderView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        itemViewHolder.bind(this.items.get(position));
        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.get(position).performClick();
                creator.smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<CarouselItem> newItems) {
        this.items = newItems;
        this.notifyDataSetChanged();
    }

    public void setCreator(CarouselRecyclerView carouselRecyclerView) {
        creator = carouselRecyclerView;
    }
}


