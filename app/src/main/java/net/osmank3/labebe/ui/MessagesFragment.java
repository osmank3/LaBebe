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

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.ImageTextView;
import net.osmank3.labebe.view.TitledListView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class MessagesFragment extends Fragment {
    private TitledListView listView;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private List<Child> children;
    private Child child;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new TitledListView(getContext());

        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable("child");
        }

        initComponents();
        registerEventHandlers();

        return listView;
    }

    private void initComponents() {
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();

        listView.setTitle(null);
        listView.showButton(false);

        children = new ArrayList<>();

        database.collection("users").document(preferences.getString("userUid", ""))
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
                            fillList();
                        }
                    }
                });

        fillList();
    }

    private void registerEventHandlers() {

    }

    private void fillList() {
        listView.clearList();
        if (child != null) {
            ImageTextView parent = new ImageTextView(getContext());
            parent.setText(R.string.parent);
            parent.setImage(R.drawable.baseline_supervisor_account_black_24dp);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putSerializable("child", child);
                    MainActivity.navController.navigate(R.id.action_messages_to_message, args);
                }
            });
            listView.addToList(parent);
        }
        for (final Child aChild: children) {
            if (child != null && child.getId().equals(aChild.getId()))
                continue;
            ImageTextView childLine = new ImageTextView(getContext());
            childLine.setText(aChild.getName());
            childLine.setImage(R.drawable.round_person_outline_black_48dp);
            childLine.setImageColor(aChild.getColor());
            childLine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putSerializable("messageTo", aChild);
                    if(child != null) {
                        args.putSerializable("child", child);
                    }
                    MainActivity.navController.navigate(R.id.action_messages_to_message, args);
                }
            });
            listView.addToList(childLine);
        }
    }
}
