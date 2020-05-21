/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.osmank3.labebe.R;
import net.osmank3.labebe.db.AppLog;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.ImageTextProgressView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class StatisticsFragment extends Fragment {
    private View root;
    private SharedPreferences preferences;
    private FirebaseFirestore database;
    private PackageManager pm;
    private String TAG = "LaBebeStatisticsFragment";
    private LinearLayout llTimeChooser, llDateChooserButtons, llChildChooser, llAppLogs;
    private Spinner spinTimeChooser, spinChildren;
    private Button btnStart, btnEnd;
    private Date start, end;
    private DateFormat formatter;
    private ArrayAdapter timeChooserArray;
    private List<Child> children;
    private Child child;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_statistics, container, false);

        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable("child");
        }

        initComponents();
        registerEventHandlers();

        return root;
    }

    private void initComponents() {
        llTimeChooser = root.findViewById(R.id.llTimeChooser);
        llDateChooserButtons = root.findViewById(R.id.llDateChooserButtons);
        llChildChooser = root.findViewById(R.id.llChildChooser);
        llAppLogs = root.findViewById(R.id.llAppLogs);
        spinTimeChooser = root.findViewById(R.id.spinTimeChooser);
        spinChildren = root.findViewById(R.id.spinChildren);
        btnStart = root.findViewById(R.id.btnStart);
        btnEnd = root.findViewById(R.id.btnEnd);

        database = FirebaseFirestore.getInstance();
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        pm = getContext().getPackageManager();
        children = new ArrayList<>();

        llDateChooserButtons.setVisibility(View.GONE);
        if (child != null) {
            llChildChooser.setVisibility(View.GONE);
        }

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
                            fillChildrenList();
                        }
                    }
                });

        getDataFromDatabase();
        formatter = new SimpleDateFormat("dd MM yyyy");
    }

    private void registerEventHandlers() {
        spinTimeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                llDateChooserButtons.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                getDataFromDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                child = children.get(position);
                getDataFromDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        View.OnClickListener btnListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Calendar now = Calendar.getInstance();
                if (v.getId() == btnStart.getId() && start != null) {
                    now.setTime(start);
                } else if (v.getId() == btnEnd.getId() && end != null) {
                    now.setTime(end);
                }
                DatePickerDialog dialog = new DatePickerDialog(v.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar date = Calendar.getInstance();
                                date.clear();
                                date.set(Calendar.YEAR, year);
                                date.set(Calendar.MONTH, month);
                                date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                if (v.getId() == btnStart.getId()) {
                                    start = date.getTime();
                                    btnStart.setText(formatter.format(start));
                                } else if (v.getId() == btnEnd.getId()) {
                                    date.add(Calendar.DAY_OF_YEAR, 1);
                                    date.add(Calendar.SECOND, -1);
                                    end = date.getTime();
                                    btnEnd.setText(formatter.format(end));
                                }
                                getDataFromDatabase();
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                        );
                dialog.show();
            }
        };

        btnStart.setOnClickListener(btnListener);
        btnEnd.setOnClickListener(btnListener);
    }

    private void fillChildrenList() {
        List<String> list = new ArrayList<>();
        for (Child child: children) list.add(child.getName());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, list);
        spinChildren.setAdapter(arrayAdapter);
    }

    private void getDataFromDatabase() {
        if (child == null) return;
        if (spinTimeChooser.getSelectedItemPosition() == 0) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.setTimeInMillis(today.getTimeInMillis());
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .collection("logs")
                    .orderBy("start")
                    .startAt(today.getTime())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {
                            List<QueryDocumentSnapshot> logs = new ArrayList<>();
                            for (QueryDocumentSnapshot doc: snapshots) {
                                if (doc.contains("appName")) {
                                    logs.add(doc);
                                }
                            }
                            fillAppLogsList(logs);
                        }
                    });
        }

        if (spinTimeChooser.getSelectedItemPosition() == 1 && start != null && end != null) {
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .collection("logs")
                    .orderBy("start")
                    .startAt(start)
                    .endAt(end)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {
                            List<QueryDocumentSnapshot> logs = new ArrayList<>();
                            for (QueryDocumentSnapshot doc: snapshots) {
                                if (doc.contains("appName")) {
                                    logs.add(doc);
                                }
                            }
                            fillAppLogsList(logs);
                        }
                    });
        }

        if (spinTimeChooser.getSelectedItemPosition() == 2) {
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .collection("logs")
                    .orderBy("start")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot snapshots) {
                            List<QueryDocumentSnapshot> logs = new ArrayList<>();
                            for (QueryDocumentSnapshot doc: snapshots) {
                                if (doc.contains("appName")) {
                                    logs.add(doc);
                                }
                            }
                            fillAppLogsList(logs);
                        }
                    });
        }
    }

    private void fillAppLogsList(List<QueryDocumentSnapshot> snapshots) {
        HashMap<String, Long> appUsageList = new HashMap<>();
        llAppLogs.removeAllViews();
        Long sum = 0L;
        List<AppLog> appLogs = new ArrayList<>();
        for (QueryDocumentSnapshot log: snapshots) {
            appLogs.add(log.toObject(AppLog.class));
        }
        for (AppLog appLog: appLogs) {
            Long duration = appUsageList.containsKey(appLog.getAppName()) ? appUsageList.get(appLog.getAppName()) : 0;
            duration += (appLog.getEnd().getTime() - appLog.getStart().getTime());
            sum += (appLog.getEnd().getTime() - appLog.getStart().getTime());
            appUsageList.put(appLog.getAppName(), duration);
        }
        //TODO sorting results by usage needed in here
        for (String key: appUsageList.keySet()) {
            ImageTextProgressView line = new ImageTextProgressView(getContext());
            try {
                line.setImage(pm.getApplicationIcon(key));
                line.setText(pm.getApplicationLabel(pm.getApplicationInfo(key, 0)).toString());
                line.setProgress((int)(appUsageList.get(key)*100/sum));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            llAppLogs.addView(line);
        }
    }
}
