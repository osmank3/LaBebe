/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.carousel.CarouselItem;
import net.osmank3.labebe.view.carousel.CarouselItemAdapter;
import net.osmank3.labebe.view.carousel.CarouselRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {
    private View root;
    private List<CarouselItem> items;
    private List<Child> children;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private CarouselItemAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        initComponents();
        registerEventHandlers();

        return root;
    }

    private void initComponents() {
        children = new ArrayList<>();
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        database.collection("users")
                .document(preferences.getString("userUid", ""))
                .collection("children")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Child child = document.toObject(Child.class);
                                child.setId(document.getId());
                                children.add(child);
                            }
                            fillItems();
                        }
                    }
                });


        List<Integer> viewIds = new ArrayList<>();
        viewIds.add(R.id.list_item_background);
        viewIds.add(R.id.list_item_text);

        CarouselRecyclerView carousel = root.findViewById(R.id.carouselRecyclerView);
        carousel.setViewsToChangeColor(viewIds);

        adapter = new CarouselItemAdapter(carousel);
        carousel.initialize(adapter);

        items = new ArrayList<>();
        fillItems();
    }

    private void fillItems() {
        items.clear();
        items.add(new CarouselItem(root.getContext()));
        for (Child child: children) {
            items.add(new CarouselItem(root.getContext(), child));
        }
        adapter.setItems(items);
    }

    private void registerEventHandlers() {

    }
}
