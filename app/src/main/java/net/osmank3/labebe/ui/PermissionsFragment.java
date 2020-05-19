/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.osmank3.labebe.LaBebeAccessibilityService;
import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;


public class PermissionsFragment extends Fragment {
    private View root;
    private TextView textPermissions;
    private LinearLayout llAppOverlay, llAccessibility;
    private CheckBox cbAppOverlay, cbAccessibility;
    private Button btnPermission;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_permissions, container, false);

        initComponents();
        registerEventHandlers();

        return root;
    }

    private void initComponents() {
        textPermissions = root.findViewById(R.id.textPermissions);
        llAppOverlay = root.findViewById(R.id.llAppOverlay);
        llAccessibility = root.findViewById(R.id.llAccessibility);
        cbAppOverlay = root.findViewById(R.id.cbAppOverlay);
        cbAccessibility = root.findViewById(R.id.cbAccessibility);
        btnPermission = root.findViewById(R.id.btnPermission);
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        if (preferences.getBoolean("isDeviceParental", false)) {
            textPermissions.setText(textPermissions.getText().toString().concat(getResources().getText(R.string.permission_on_parental_device).toString()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cbAppOverlay.setChecked(Settings.canDrawOverlays(getContext()));
        }
        else {
            cbAppOverlay.setChecked(true);
        }
        cbAccessibility.setChecked(LaBebeAccessibilityService.instance != null);
        if ((preferences.getBoolean("isDeviceParental", false) && preferences.getBoolean("isFirstStart", true)) || (cbAccessibility.isChecked() && cbAppOverlay.isChecked()))
            btnPermission.setEnabled(true);
        else
            btnPermission.setEnabled(false);
    }

    private void registerEventHandlers() {
        llAppOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                        startActivityForResult(intent, 1);
                    }
                }
            }
        });

        llAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LaBebeAccessibilityService.instance == null) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, 2);
                }
            }
        });

        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferences.getBoolean("isFirstStart", true)) {
                    MainActivity.navController.navigate(R.id.action_permissions_to_firstStartFinish);
                } else {
                    preferences.edit().putBoolean("isLent", true).apply();
                    MainActivity.navController.navigate(R.id.action_permissions_to_account);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            cbAppOverlay.setChecked(Settings.canDrawOverlays(getContext()));
        cbAccessibility.setChecked(LaBebeAccessibilityService.instance != null);
        if (preferences.getBoolean("isDeviceParental", false) || (cbAccessibility.isChecked() && cbAppOverlay.isChecked()))
            btnPermission.setEnabled(true);
        else
            btnPermission.setEnabled(false);
    }
}
