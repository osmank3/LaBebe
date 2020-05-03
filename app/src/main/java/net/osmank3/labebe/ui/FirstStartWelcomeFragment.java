/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import net.osmank3.labebe.R;


public class FirstStartWelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_first_start_welcome, container, false);
        Button btnFirstWelcome = root.findViewById(R.id.btnFirstWelcome);
        btnFirstWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_firstStartWelcome_to_signUp);
            }
        });
        Button btnFirstCancel = root.findViewById(R.id.btnFirstCancel);
        btnFirstCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        return root;
    }
}
