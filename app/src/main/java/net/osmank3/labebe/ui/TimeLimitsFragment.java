/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

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
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.db.TimeLimit;
import net.osmank3.labebe.view.AlertReturnHandler;
import net.osmank3.labebe.view.ImageTextSwitchView;
import net.osmank3.labebe.view.TimeLimitAlertView;
import net.osmank3.labebe.view.TitledListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;


public class TimeLimitsFragment extends Fragment implements AlertReturnHandler {
    private TitledListView listView;
    private Child child;
    private List<String> allowedApps;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private List<TimeLimit> timeLimits;
    private DateFormat formatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new TitledListView(getContext());
        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable("child");
        }

        initComponents();
        registerEventHandlers();
        fillTimeLimitsList();

        return listView;
    }

    @Override
    public void onPause() {
        super.onPause();
        HashMap<String, Object> data = new HashMap<>();
        data.put("timeLimits", timeLimits);
        database.collection("users")
                .document(preferences.getString("userUid", ""))
                .collection("children")
                .document(child.getId())
                .set(data, SetOptions.merge())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("LaBebeTimeLimits", "Error adding document", e);
                    }
                    });
    }

    private void initComponents() {
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();

        if (preferences.getBoolean("isFirstStart", true)) {
            listView.showButton(true);
        } else {
            listView.showButton(false);
        }
        listView.setTitle(R.string.child_time_limitations);

        timeLimits = new ArrayList<>();
        allowedApps = new ArrayList<>();

        database.collection("users").document(preferences.getString("userUid", ""))
                .collection("children").document(child.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<HashMap> limits = (List<HashMap>) document.get("timeLimits");
                                if (limits != null) {
                                    timeLimits = new ArrayList<>();
                                    for (HashMap hashMap: limits) {
                                        timeLimits.add(TimeLimit.timeLimitFromMap(hashMap));
                                    }
                                    fillTimeLimitsList();
                                }
                                List<String> apps = (List<String>) document.get("allowedApps");
                                if (apps != null) {
                                    allowedApps = apps;
                                }
                            }
                        }
                    }
                });

        formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void registerEventHandlers() {
        listView.setOnButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigate(R.id.action_timeLimits_to_children);
            }
        });

        listView.setOnFabClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popAlertDialog();
            }
        });
    }

    private void fillTimeLimitsList() {
        listView.clearList();
        PackageManager pm = getContext().getPackageManager();
        for (final TimeLimit timeLimit: timeLimits) {
            ImageTextSwitchView line = new ImageTextSwitchView(getContext());
            switch (timeLimit.getType()) {
                case LimitedHours:
                    line.setImage(R.drawable.limited_time_black_36dp);
                    line.setText(String.format("%s - %s", formatter.format(timeLimit.getStart()), formatter.format(timeLimit.getEnd())));
                    line.checkSwitchOn(timeLimit.isStatus());
                    break;
                case DailyHours:
                    line.setImage(R.drawable.limit_timer_black_36dp);
                    line.setText(String.format("%s - %s", getResources().getText(R.string.daily), formatter.format(timeLimit.getDuration())));
                    line.checkSwitchOn(timeLimit.isStatus());
                    break;
                case AppDailyHours:
                    line.setImage(R.drawable.limit_app_timer_black_24dp);
                    try {
                        line.setText(String.format("%s - %s", pm.getApplicationInfo(timeLimit.getAppName(), 0).loadLabel(pm), formatter.format(timeLimit.getDuration())));
                        line.checkSwitchOn(timeLimit.isStatus());
                    } catch (PackageManager.NameNotFoundException e) {
                        continue;
                    }
                    break;
            }
            line.setOnChangeSwitch(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    timeLimit.setStatus(isChecked);
                }
            });
            line.setOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popAlertDialog(timeLimit);
                }
            });
            listView.addToList(line);
        }
    }

    private void popAlertDialog() {
        popAlertDialog(null);
    }

    private void popAlertDialog(TimeLimit timeLimit) {
        TimeLimitAlertView alert = new TimeLimitAlertView(getContext(), this);
        alert.setParameters(timeLimit, allowedApps);
        alert.showDialog();
    }

    @Override
    public void onAlertSuccess(Object object) {
        TimeLimit timeLimit = (TimeLimit) object;
        if (!timeLimits.contains(timeLimit))
            timeLimits.add(timeLimit);
        fillTimeLimitsList();
    }

    @Override
    public void onAlertFunction(Object object) {
        HashMap<String, Object> func = (HashMap<String, Object>) object;
        if (func.containsKey("delete")) {
            timeLimits.remove((TimeLimit) func.get("delete"));
        }
        if (func.containsKey("toast")) {
            Toast.makeText(getContext(), (String) func.get("toast"), Toast.LENGTH_LONG).show();
        }
        fillTimeLimitsList();
    }
}
