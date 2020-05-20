/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.osmank3.labebe.db.App;
import net.osmank3.labebe.db.AppLog;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.db.TimeLimit;
import net.osmank3.labebe.view.carousel.CarouselItem;
import net.osmank3.labebe.view.carousel.CarouselItemAdapter;
import net.osmank3.labebe.view.carousel.CarouselRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LaBebeAccessibilityService extends AccessibilityService {
    public static LaBebeAccessibilityService instance;
    private String TAG = "LaBebeAccessibility";
    private FirebaseAuth auth;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private List<Child> children;
    private HashMap<String, App> homeApps;
    private HashMap<String, App> launcherApps;
    private HashMap<String, App> generallyAllows;
    private HashMap<Child, HashMap<String, App>> childrenAllowedApp;
    private HashMap<Child, List<TimeLimit>> childrenTimeLimits;
    private HashMap<Child, TimeCounter> childrenTimeCounter;
    private HashMap<Child, ListenerRegistration> childrenAppLogsReg;
    private Timer timer;
    private String focusedApp;
    private AlertDialog dialog;
    private List<Integer> viewIds;

    @Override
    public void onServiceConnected() {
        instance = this;
        initServiceRequirements();
        Log.i(TAG, "Accessibility Service started");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (preferences.getBoolean("isLent", true)) {
            //TODO block uninstalling this app in here
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                    !event.getPackageName().equals(focusedApp) &&
                    !generallyAllows.containsKey(event.getPackageName().toString()) &&
                    launcherApps.containsKey(event.getPackageName().toString()) &&
                    !homeApps.containsKey(event.getPackageName().toString()) &&
                    !event.getPackageName().equals(getPackageName())) {

                focusedAppChanged(event.getPackageName().toString());
                Log.d(TAG, "Focused app: " + focusedApp);

                CarouselRecyclerView carousel = new CarouselRecyclerView(getBaseContext());
                carousel.setViewsToChangeColor(viewIds);
                carousel.setClipToPadding(false);
                carousel.setOverScrollMode(View.OVER_SCROLL_NEVER);

                CarouselItemAdapter adapter = new CarouselItemAdapter(carousel);
                carousel.initialize(adapter);

                List<CarouselItem> items = new ArrayList<>();
                items.add(new CarouselItem(getBaseContext(), true));
                for (Child child : children) {
                    items.add(new CarouselItem(getBaseContext(), child, true));
                }

                adapter.setItems(items);

                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                dialog = new AlertDialog.Builder(getBaseContext(), android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
                        .setView(carousel)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.dismiss();
                                performGlobalAction(GLOBAL_ACTION_HOME);
                            }
                        })
                        .setTitle(R.string.select_for_login)
                        .create();

                int alertType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
                dialog.getWindow().setType(alertType);
                dialog.show();
            } else {
                Log.d(TAG, "Passed app: " + event.getPackageName().toString());
            }
            if (homeApps.containsKey(event.getPackageName().toString())) {
                focusedAppChanged(null);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void initServiceRequirements() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        preferences = getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);

        children = new ArrayList<>();
        homeApps = new HashMap<>();
        launcherApps = new HashMap<>();
        generallyAllows = new HashMap<>();
        childrenAllowedApp = new HashMap<>();
        childrenTimeLimits = new HashMap<>();
        childrenTimeCounter = new HashMap<>();
        childrenAppLogsReg = new HashMap<>();

        viewIds = new ArrayList<>();
        viewIds.add(R.id.list_item_background);
        viewIds.add(R.id.list_item_text);

        fillPackageLists();
        setDatabaseQueries();
        setDailyUpdate();
    }

    private void fillPackageLists() {
        launcherApps.clear();
        homeApps.clear();
        PackageManager pm = getPackageManager();
        Intent pmIntent = new Intent(Intent.ACTION_MAIN);
        pmIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
            App app = new App();
            app.setId(resolveInfo.activityInfo.packageName);
            app.setType("Launcher");
            app.setName(resolveInfo.loadLabel(pm).toString());
            try {
                app.setVersion(pm.getPackageInfo(resolveInfo.activityInfo.packageName,0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            launcherApps.put(app.getId(), app);
        }
        pmIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
        pmIntent.addCategory(Intent.CATEGORY_HOME);
        for (ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
            App app = new App();
            app.setId(resolveInfo.activityInfo.packageName);
            app.setType("Home");
            app.setName(resolveInfo.loadLabel(pm).toString());
            try {
                app.setVersion(pm.getPackageInfo(resolveInfo.activityInfo.packageName,0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            homeApps.put(app.getId(), app);
            if (launcherApps.containsKey(app.getId())) {
                launcherApps.remove(app.getId());
            }
        }
    }

    private void setDatabaseQueries() {
        database.collection("users")
                .document(preferences.getString("userUid", ""))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed", e);
                            return;
                        }

                        String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";

                        if (snapshot != null && snapshot.exists()) {
                            setUserChanges(snapshot.getData());
                        }
                    }
                });

        database.collection("users")
                .document(preferences.getString("userUid", ""))
                .collection("children")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed", e);
                            return;
                        }

                        String source = snapshots != null && snapshots.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";

                        List<QueryDocumentSnapshot> children = new ArrayList<>();
                        for (QueryDocumentSnapshot doc: snapshots) {
                            if (doc.contains("name")) {
                                children.add(doc);
                            }
                        }
                        setChildrenChanges(children);
                    }
                });
    }

    private void setDailyUpdate() {
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                        @Override
                        public void run() {
                            database.collection("users")
                                    .document(preferences.getString("userUid", ""))
                                    .collection("children")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot snapshots) {
                                            List<QueryDocumentSnapshot> children = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : snapshots) {
                                                if (doc.contains("name")) {
                                                    children.add(doc);
                                                }
                                            }
                                            setChildrenChanges(children);
                                        }
                                    });
                        }
            },
            tomorrow.getTimeInMillis() - now.getTimeInMillis(),
            86400000
        );
    }

    private void setChildrenChanges(List<QueryDocumentSnapshot> children) {
        if (children != null && children.size() > 0) {
            this.children.clear();
            for (ListenerRegistration reg: childrenAppLogsReg.values()) {
                reg.remove();
            }
            childrenAppLogsReg = new HashMap<>();
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.setTimeInMillis(now.getTimeInMillis());
            for (QueryDocumentSnapshot doc: children) {
                final Child child = doc.toObject(Child.class);
                childrenAppLogsReg.put(
                        child,
                        database.collection("users")
                                .document(preferences.getString("userUid", ""))
                                .collection("children")
                                .document(child.getId())
                                .collection("logs")
                                .orderBy("start")
                                .startAt(now.getTime())
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen failed", e);
                                            return;
                                        }

                                        String source = snapshots != null && snapshots.getMetadata().hasPendingWrites()
                                                ? "Local" : "Server";

                                        List<QueryDocumentSnapshot> logs = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc: snapshots) {
                                            if (doc.contains("appName")) {
                                                logs.add(doc);
                                            }
                                        }
                                        setUsageLogs(child, logs);
                                    }
                                })
                );

                this.children.add(child);
                childrenAllowedApp.put(child, new HashMap<String, App>());
                if (doc.contains("allowedApps")) {
                    for (String appName : (List<String>) doc.get("allowedApps")) {
                        if (launcherApps.containsKey(appName)) {
                            childrenAllowedApp.get(child).put(appName, launcherApps.get(appName));
                        }
                    }
                }
                childrenTimeLimits.put(child, new ArrayList<TimeLimit>());
                if (doc.contains("timeLimits")) {
                    for (HashMap limitMap : (List<HashMap>) doc.get("timeLimits")) {
                        if (limitMap != null) {
                            childrenTimeLimits.get(child).add(TimeLimit.timeLimitFromMap(limitMap));
                        }
                    }
                }
                Log.i(TAG, "Child: " + child.getName() + " information is ready");
            }
            Log.i(TAG, "Children list, limits and decisions updated");
        }
    }

    private void setUsageLogs(Child child, List<QueryDocumentSnapshot> logs) {
        List<AppLog> appLogs = new ArrayList<>();
        for (QueryDocumentSnapshot log: logs) {
            appLogs.add(log.toObject(AppLog.class));
        }
        for (AppLog appLog: appLogs) {
            for (TimeLimit timeLimit: childrenTimeLimits.get(child)) {
                if (timeLimit.getType().equals(TimeLimit.Type.DailyHours) ||
                        (timeLimit.getType().equals(TimeLimit.Type.AppDailyHours) && timeLimit.getAppName().equals(appLog.getAppName()))) {
                    Date newDate = new Date();
                    newDate.setTime(timeLimit.getDuration().getTime() - (appLog.getEnd().getTime() - appLog.getStart().getTime()));
                    timeLimit.setDuration(newDate);
                }
            }
        }
    }

    private void setUserChanges(Map<String, Object> data) {
        if (data != null) {
            if (data.containsKey("parent_password_hash"))
                preferences.edit().putString("parent_password_hash", data.get("parent_password_hash").toString()).apply();
            if (data.containsKey("allowedApps") && data.get("allowedApps") != null) {
                generallyAllows.clear();
                for (String appName: (List<String>) data.get("allowedApps")) {
                    if (launcherApps.containsKey(appName)) {
                        generallyAllows.put(appName, launcherApps.get(appName));
                        Log.d(TAG, launcherApps.get(appName).getName() + " added to generally allowed apps (" + appName + ")");
                    }
                }
                Log.i(TAG, "Generally allowed apps updated");
            }
        }
    }

    public void doAction(Child child) {
        if (child == null) {
            Log.d(TAG, "Parent can access the app");
            dialog.dismiss();
        } else {
            if (childrenAllowedApp.get(child).containsKey(focusedApp)) {
                TimeCounter timeCounter;
                if (childrenTimeCounter.containsKey(child))
                    timeCounter = childrenTimeCounter.get(child);
                else {
                    timeCounter = new TimeCounter(child, childrenTimeLimits.get(child), focusedApp);
                    childrenTimeCounter.put(child, timeCounter);
                }
                timeCounter.setFocusedApp(focusedApp);
                if (timeCounter.canAccessNow()) {
                    Log.d(TAG, child.getName() + " can access the application");
                    timeCounter.start();
                    dialog.dismiss();
                } else {
                    dialog.cancel();
                    String message = "";
                    if (timeCounter.getReason() == TimeLimit.Type.LimitedHours)
                        message = getString(R.string.you_are_in_limited_hours);
                    else if (timeCounter.getReason() == TimeLimit.Type.DailyHours)
                        message = getString(R.string.you_cannot_use_today);
                    else if (timeCounter.getReason() == TimeLimit.Type.AppDailyHours)
                        message = getString(R.string.you_cannot_use_today_app);
                    Toast toast = Toast.makeText(getBaseContext(), message,Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } else {
                dialog.cancel();
                Toast toast = Toast.makeText(getBaseContext(), R.string.you_cannot_use, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    private void focusedAppChanged(String newFocus) {
        if (childrenTimeCounter.size() > 0) {
            for (TimeCounter timeCounter : childrenTimeCounter.values()) {
                timeCounter.stop();
            }
        }
        focusedApp = newFocus;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }
}
