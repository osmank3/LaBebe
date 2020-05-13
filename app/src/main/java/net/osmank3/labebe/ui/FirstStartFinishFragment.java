/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;


public class FirstStartFinishFragment extends Fragment {
    View root;
    private Button btnFirstStartFinish;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_first_start_finish, container, false);

        initComponents();
        registerEventHandlers();

        return root;
    }

    private void initComponents() {
        btnFirstStartFinish = root.findViewById(R.id.btnFirstStartFinish);
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    private void registerEventHandlers() {
        btnFirstStartFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putBoolean("isFirstStart", false).apply();
                MainActivity.navController.navigate(R.id.action_firstStartFinish_to_home);
            }
        });
    }
}
