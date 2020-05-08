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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.ImageTextView;
import net.osmank3.labebe.view.TitledListView;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;


public class ChildrenFragment extends Fragment {
    private TitledListView listView;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private HashMap<String, Child> children;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new TitledListView(getContext());

        initComponents();
        registerEventHandlers();
        fillChildrenList();

        return listView;
    }

    private void initComponents() {
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        children = new HashMap<>();

        database.collection("users").document(preferences.getString("userUid", ""))
                .collection("children")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Child child = document.toObject(Child.class);
                                children.put(child.getName(), child);
                            }
                            fillChildrenList();
                        }
                    }
                });
    }

    private void registerEventHandlers() {
        listView.setOnButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigate(R.id.action_children_to_permissions);
            }
        });
    }

    private void fillChildrenList() {
        listView.clearList();
        if (children.size() == 0) {
            TextView textView = new TextView(getContext());
            textView.setText(R.string.there_is_no_child);
            textView.setTextSize(getResources().getDimension(R.dimen.big_text));
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            listView.addToList(textView);
        } else {
            for (final Child child : children.values()) {
                ImageTextView line = new ImageTextView(getContext());
                line.setText(child.getName());
                line.setImageColor(child.getColor().hashCode());//TODO this is not color, this line will change
                line.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putString("child", child.getId());
                        MainActivity.navController.navigate(R.id.action_children_to_child, args);
                    }
                });
                listView.addToList(line);
            }
        }
    }
}
