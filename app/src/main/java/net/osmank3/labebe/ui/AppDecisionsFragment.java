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
import com.google.firebase.firestore.SetOptions;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.App;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.ImageTextSwitchView;
import net.osmank3.labebe.view.TitledListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class AppDecisionsFragment extends Fragment {
    private TitledListView listView;
    private Child child;

    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private PackageManager pm;
    private HashMap<String, App> apps;
    private List<String> generallyAllows;
    private List<String> childAllows;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new TitledListView(getContext());
        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable("child");
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
            HashMap<String, Object> data = new HashMap<>();
            data.put("allowedApps", generallyAllows);
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .set(data, SetOptions.merge())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("LaBebeAppDecisions", "Error adding document", e);
                        }
                    });
        } else {
            HashMap<String, Object> data = new HashMap<>();
            data.put("allowedApps", childAllows);
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .set(data, SetOptions.merge())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("LaBebeAppDecisions", "Error adding document", e);
                        }
                    });
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
                                List<String> apps = (List<String>) document.get("allowedApps");
                                if (apps != null) {
                                    generallyAllows = apps;
                                    fillAppDecisionList();
                                }
                            }
                        }
                    }
                });

        if (preferences.getBoolean("isFirstStart", true)) {
            listView.showButton(true);
        } else {
            listView.showButton(false);
        }
        if (child != null) {
            listView.setTitle(getResources().getText(R.string.child_app_decisions).toString().replace("child", child.getName()));
            childAllows = new ArrayList<>();
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    List<String> apps = (List<String>) document.get("allowedApps");
                                    if (apps != null) {
                                        childAllows = apps;
                                        fillAppDecisionList();
                                    }
                                }
                            }
                        }
                    });
        } else {
            listView.setTitle(R.string.general_app_decisions);
        }
    }

    private void registerEventHandlers() {
        listView.setOnButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (child == null)
                    MainActivity.navController.navigate(R.id.action_appDecisions_to_children);
                else {
                    Bundle args = new Bundle();
                    args.putSerializable("child", child);
                    MainActivity.navController.navigate(R.id.action_appDecisions_to_timeLimits, args);
                }
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
                line.checkSwitchOn(generallyAllows.contains(resolveInfo.activityInfo.packageName));
                line.setOnChangeSwitch(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            generallyAllows.add(resolveInfo.activityInfo.packageName);
                        } else {
                            generallyAllows.remove(resolveInfo.activityInfo.packageName);
                        }
                    }
                });
                listView.addToList(line);
            }
        } else {
            Intent pmIntent = new Intent(Intent.ACTION_MAIN);
            pmIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            for (final ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
                if (generallyAllows.contains(resolveInfo.activityInfo.packageName)) {
                    continue;
                }
                ImageTextSwitchView line = new ImageTextSwitchView(getContext());
                try {
                    line.setImage(pm.getApplicationIcon(resolveInfo.activityInfo.packageName));
                } catch (PackageManager.NameNotFoundException e) {

                }
                line.setText(resolveInfo.activityInfo.loadLabel(pm).toString());
                line.checkSwitchOn(childAllows.contains(resolveInfo.activityInfo.packageName));
                line.setOnChangeSwitch(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            childAllows.add(resolveInfo.activityInfo.packageName);
                        } else {
                            childAllows.remove(resolveInfo.activityInfo.packageName);
                        }
                    }
                });
                listView.addToList(line);
            }
        }
    }
}
