/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class AppDecisionsFragment extends Fragment {
    private View root;
    private TextView textAppDecisions;
    private Button btnNextPage;
    private LinearLayout appDecisionsList;
    private String child;

    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private PackageManager pm;
    private HashMap<String, App> apps;
    private List<String> generallyAllows;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_app_decisions, container, false);
        if (getArguments() != null) {
            child = getArguments().getString("child");
        }

        initComponents();
        registerEventHandlers();
        fillAppDecisionList();

        return root;
    }

    private void initComponents() {
        textAppDecisions = root.findViewById(R.id.textAppDecisions);
        btnNextPage = root.findViewById(R.id.btnNextPage);
        appDecisionsList = root.findViewById(R.id.appDecisionsList);
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        pm = getActivity().getPackageManager();
        generallyAllows = new ArrayList<>();

        database.collection("users").document(preferences.getString("userUid", ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                generallyAllows = (List<String>) document.get("allowedApps");
                                fillAppDecisionList();
                            }
                        }
                    }
                });

        if (preferences.getBoolean("isFirstStart", true)) {
            textAppDecisions.setVisibility(View.VISIBLE);
            btnNextPage.setVisibility(View.VISIBLE);
        } else {
            textAppDecisions.setVisibility(View.GONE);
            btnNextPage.setVisibility(View.GONE);
        }
    }

    private void registerEventHandlers() {
        btnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, List<String>> data = new HashMap<>();
                data.put("allowedApps", generallyAllows);
                database.collection("users")
                            .document(preferences.getString("userUid",""))
                            .set(data)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("LaBebeAppDecisions", "Error adding document", e);
                                }
                            });
                MainActivity.navController.navigate(R.id.action_appDecisions_to_children);
            }
        });
    }

    private void fillAppDecisionList() {
        appDecisionsList.removeAllViews();
        if (child == null) {
            if (preferences.getBoolean("isFirstStart", true)) {
                PackageManager pm = getActivity().getPackageManager();
                apps = new HashMap<>();
                for (ApplicationInfo appInfo: pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
                    App app = new App();
                    app.setId(appInfo.packageName);
                    app.setName(appInfo.loadLabel(pm).toString());
                    app.setType("System");
                    try {
                        app.setVersion(pm.getPackageInfo(appInfo.packageName,0).versionName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    apps.put(appInfo.packageName, app);
                }
                Intent pmIntent = new Intent(Intent.ACTION_MAIN);
                pmIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                for (ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
                    apps.get(resolveInfo.activityInfo.packageName).setType("Launcher");
                }
                pmIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
                pmIntent.addCategory(Intent.CATEGORY_HOME);
                for (ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
                    apps.get(resolveInfo.activityInfo.packageName).setType("Home");
                }

                for (App app : apps.values()) {
                    database.collection("applications")
                            .document(app.getId())
                            .set(app)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("LaBebe", "Error adding document", e);
                                }
                            });
                }
            }

            Intent pmIntent = new Intent(Intent.ACTION_MAIN);
            pmIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            for (final ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
                ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.app_switch_layout, null);
                ImageView appImage = cl.findViewById(R.id.imageApp);
                TextView appNameView = cl.findViewById(R.id.textApp);
                Switch appSwitch = cl.findViewById(R.id.switchApp);
                try {
                    appImage.setImageDrawable(pm.getApplicationIcon(resolveInfo.activityInfo.packageName));
                } catch (PackageManager.NameNotFoundException e) {

                }
                appNameView.setText(resolveInfo.activityInfo.loadLabel(pm).toString());
                appSwitch.setChecked(generallyAllows.contains(resolveInfo.activityInfo.name));
                appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            generallyAllows.add(resolveInfo.activityInfo.name);
                        } else {
                            generallyAllows.remove(resolveInfo.activityInfo.name);
                        }
                    }
                });
                appDecisionsList.addView(cl);
            }
        } else {
           //TODO child config, allowed-blocked
        }
    }
}
