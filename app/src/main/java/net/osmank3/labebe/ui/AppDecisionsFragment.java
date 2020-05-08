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
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.App;
import net.osmank3.labebe.view.ImageTextSwitchView;
import net.osmank3.labebe.view.TitledListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class AppDecisionsFragment extends Fragment {
    private TitledListView listView;
    private String child;

    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private PackageManager pm;
    private HashMap<String, App> apps;
    private List<String> generallyAllows;
    private HashMap<String, Object> dbDocumentPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new TitledListView(getContext());
        if (getArguments() != null) {
            child = getArguments().getString("child");
        }

        initComponents();
        registerEventHandlers();
        fillAppDecisionList();

        return listView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (child == null) {
            HashMap<String, Object> data;
            if (dbDocumentPreferences != null)
                data = dbDocumentPreferences;
            else
                data = new HashMap<>();
            data.put("allowedApps", generallyAllows);
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .set(data)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("LaBebeAppDecisions", "Error adding document", e);
                        }
                    });
        } else {

        }
    }

    private void initComponents() {
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
                                dbDocumentPreferences = (HashMap<String, Object>) document.getData();
                                setVariables();
                            }
                        }
                    }
                });

        if (preferences.getBoolean("isFirstStart", true)) {
            listView.showButton(true);
        } else {
            listView.showButton(false);
        }
        listView.setTitle(R.string.general_app_decisions);
    }

    private void setVariables() {
        if (dbDocumentPreferences != null) {
            if (dbDocumentPreferences.containsKey("allowedApps")) {
                generallyAllows = (List<String>) dbDocumentPreferences.get("allowedApps");
                fillAppDecisionList();
            }
        }
    }

    private void registerEventHandlers() {
        listView.setOnButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigate(R.id.action_appDecisions_to_children);
            }
        });
    }

    private void fillAppDecisionList() {
        listView.clearList();
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

                //TODO this database process must send background
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
                ImageTextSwitchView line = new ImageTextSwitchView(getContext());
                try {
                    line.setImage(pm.getApplicationIcon(resolveInfo.activityInfo.packageName));
                } catch (PackageManager.NameNotFoundException e) {

                }
                line.setText(resolveInfo.activityInfo.loadLabel(pm).toString());
                line.checkSwitchOn(generallyAllows.contains(resolveInfo.activityInfo.name));
                line.setOnChangeSwitch(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            generallyAllows.add(resolveInfo.activityInfo.name);
                        } else {
                            generallyAllows.remove(resolveInfo.activityInfo.name);
                        }
                    }
                });
                listView.addToList(line);
            }
        } else {
           //TODO child config, allowed-blocked
        }
    }
}
