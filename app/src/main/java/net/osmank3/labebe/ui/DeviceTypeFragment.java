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
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;


public class DeviceTypeFragment extends Fragment {
    private View root;
    private Chip chipParent, chipChildren;
    private SharedPreferences preferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_device_type, container, false);

        initComponent();
        registerEventHandlers();

        return root;
    }

    private void initComponent() {
        chipParent = root.findViewById(R.id.chipParent);
        chipChildren = root.findViewById(R.id.chipChildren);
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        if (preferences.getBoolean("isDeviceParental", false))
            chipParent.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        if (!preferences.getBoolean("isDeviceParental", true))
            chipChildren.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
    }

    private void registerEventHandlers() {
        View.OnClickListener chipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.chipParent) {
                    chipParent.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    chipChildren.setChipBackgroundColor(null);
                    preferences.edit().putBoolean("isDeviceParental", true).apply();
                } else {
                    chipParent.setChipBackgroundColor(null);
                    chipChildren.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    preferences.edit().putBoolean("isDeviceParental", false).apply();
                }
                MainActivity.navController.navigate(R.id.action_deviceType_to_password);
            }
        };

        chipParent.setOnClickListener(chipListener);
        chipChildren.setOnClickListener(chipListener);
    }
}
