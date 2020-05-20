/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import net.osmank3.labebe.R;
import net.osmank3.labebe.db.App;
import net.osmank3.labebe.db.TimeLimit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static net.osmank3.labebe.db.TimeLimit.Type.AppDailyHours;
import static net.osmank3.labebe.db.TimeLimit.Type.DailyHours;
import static net.osmank3.labebe.db.TimeLimit.Type.LimitedHours;

public class TimeLimitAlertView extends ConstraintLayout {
    private View view;
    private AlertReturnHandler creator;
    private AlertDialog alert;
    private TimeLimit timeLimit;
    private List<App> allowedApps;
    private TextView radioText;
    private RadioGroup radioGroup;
    private ChipGroup chipGroup;
    private Chip chipMonday, chipTuesday, chipWednesday, chipThursday, chipFriday, chipSaturday, chipSunday;
    private Button btnStartTime, btnEndTime, btnDuration, btnChooseApp, btnDelete;
    private OnClickListener timeChooser, dayListener;
    private DateFormat formatter;
    private Boolean isNewTimeLimit;

    public TimeLimitAlertView(Context context) {
        super(context);
        init(context, null);
    }

    public TimeLimitAlertView(Context context, AlertReturnHandler creator) {
        super(context);
        this.creator = creator;
        init(context, null);
    }

    public TimeLimitAlertView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public TimeLimitAlertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null);
    }


    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.time_limits_alert_dialog, this);

        initComponents();
        registerEventHandlers();
    }

    private void initComponents() {
        radioText = findViewById(R.id.textViewRadioGroup);
        radioGroup = findViewById(R.id.radioGroup);
        chipGroup = findViewById(R.id.chipGroup);
        chipMonday = findViewById(R.id.chipMonday);
        chipTuesday = findViewById(R.id.chipTuesday);
        chipWednesday = findViewById(R.id.chipWednesday);
        chipThursday = findViewById(R.id.chipThursday);
        chipFriday = findViewById(R.id.chipFriday);
        chipSaturday = findViewById(R.id.chipSaturday);
        chipSunday = findViewById(R.id.chipSunday);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnDuration = findViewById(R.id.btnDuration);
        btnChooseApp = findViewById(R.id.btnChooseApp);
        btnDelete = findViewById(R.id.btnDelete);

        formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void registerEventHandlers() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonLimited:
                        chipGroup.setVisibility(View.VISIBLE);
                        btnStartTime.setVisibility(View.VISIBLE);
                        btnEndTime.setVisibility(View.VISIBLE);
                        btnDuration.setVisibility(View.GONE);
                        btnChooseApp.setVisibility(View.GONE);
                        timeLimit.setType(LimitedHours);
                        break;
                    case R.id.radioButtonDaily:
                        chipGroup.setVisibility(View.VISIBLE);
                        btnStartTime.setVisibility(View.GONE);
                        btnEndTime.setVisibility(View.GONE);
                        btnDuration.setVisibility(View.VISIBLE);
                        btnChooseApp.setVisibility(View.GONE);
                        timeLimit.setType(TimeLimit.Type.DailyHours);
                        break;
                    case R.id.radioButtonAppDaily:
                        chipGroup.setVisibility(View.GONE);
                        btnStartTime.setVisibility(View.GONE);
                        btnEndTime.setVisibility(View.GONE);
                        btnDuration.setVisibility(View.VISIBLE);
                        btnChooseApp.setVisibility(View.VISIBLE);
                        timeLimit.setType(AppDailyHours);
                        break;
                }
            }
        });

        timeChooser = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Button timeText = (Button) v;
                final Calendar time = Calendar.getInstance();
                time.setTimeZone(TimeZone.getTimeZone("UTC"));
                time.clear(Calendar.YEAR);
                time.clear(Calendar.MONTH);
                time.clear(Calendar.SECOND);
                time.set(Calendar.DAY_OF_YEAR, 1);
                time.setTimeInMillis(time.getTimeInMillis());
                if (v.getId() == btnStartTime.getId() && timeLimit.getStart() != null) {
                    time.setTime(timeLimit.getStart());
                } else if (v.getId() == btnEndTime.getId() && timeLimit.getEnd() != null) {
                    time.setTime(timeLimit.getEnd());
                } else if (v.getId() == btnDuration.getId() && timeLimit.getDuration() != null) {
                    time.setTime(timeLimit.getDuration());
                }

                TimePickerDialog timePicker = new TimePickerDialog(
                        v.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                time.set(Calendar.MINUTE, minute);
                                timeText.setText(String.format("%s %02d:%02d", timeText.getTag().toString(), hourOfDay, minute));

                                if (v.getId() == btnStartTime.getId()) {
                                    timeLimit.setStart(time.getTime());
                                } else if (v.getId() == btnEndTime.getId()) {
                                    timeLimit.setEnd(time.getTime());
                                } else if (v.getId() == btnDuration.getId()) {
                                    timeLimit.setDuration(time.getTime());
                                }
                            }
                        },
                        (isNewTimeLimit && v.getId() == btnDuration.getId()) ? 0 : time.get(Calendar.HOUR_OF_DAY),
                        (isNewTimeLimit && v.getId() == btnDuration.getId()) ? 0 : time.get(Calendar.MINUTE),
                        true
                );
                timePicker.show();
            }
        };

        btnStartTime.setOnClickListener(timeChooser);
        btnEndTime.setOnClickListener(timeChooser);
        btnDuration.setOnClickListener(timeChooser);

        btnChooseApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), btnChooseApp);
                for (App app: allowedApps) {
                    popupMenu.getMenu().add(app.getName());
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        btnChooseApp.setText(item.getTitle());
                        for (App app: allowedApps) {
                            if (app.getName().equals(item.getTitle())) {
                                timeLimit.setAppName(app.getId());
                                break;
                            }
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        dayListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean chipStatus = ((Chip) v).isChecked();
                if (v.getId() == R.id.chipMonday) timeLimit.getDays().Monday = chipStatus;
                else if (v.getId() == R.id.chipTuesday) timeLimit.getDays().Tuesday = chipStatus;
                else if (v.getId() == R.id.chipWednesday) timeLimit.getDays().Wednesday = chipStatus;
                else if (v.getId() == R.id.chipThursday) timeLimit.getDays().Thursday = chipStatus;
                else if (v.getId() == R.id.chipFriday) timeLimit.getDays().Friday = chipStatus;
                else if (v.getId() == R.id.chipSaturday) timeLimit.getDays().Saturday = chipStatus;
                else if (v.getId() == R.id.chipSunday) timeLimit.getDays().Sunday = chipStatus;
                timeLimit.getDays().getCode();//for calculating code
            }
        };
        chipMonday.setOnClickListener(dayListener);
        chipTuesday.setOnClickListener(dayListener);
        chipWednesday.setOnClickListener(dayListener);
        chipThursday.setOnClickListener(dayListener);
        chipFriday.setOnClickListener(dayListener);
        chipSaturday.setOnClickListener(dayListener);
        chipSunday.setOnClickListener(dayListener);

        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                HashMap<String, Object> func = new HashMap<>();
                func.put("delete", timeLimit);
                creator.onAlertFunction(func);
            }
        });
    }

    public void setParameters(TimeLimit timeLimit, List<String> allowedAppsList){
        allowedApps = new ArrayList<>();
        Intent pmIntent = new Intent(Intent.ACTION_MAIN);
        PackageManager pm = getContext().getPackageManager();
        pmIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo resolveInfo: pm.queryIntentActivities(pmIntent, 0)) {
            if (allowedAppsList.contains(resolveInfo.activityInfo.packageName)) {
                App app = new App();
                app.setId(resolveInfo.activityInfo.packageName);
                app.setName(resolveInfo.loadLabel(pm).toString());
                app.setType("Launcher");
                allowedApps.add(app);
            }
        }

        this.timeLimit = timeLimit;
        prepareComponents();
    }

    private void prepareComponents() {
        if (timeLimit != null) {
            isNewTimeLimit = false;
            switch (timeLimit.getType()) {
                case LimitedHours:
                    radioGroup.clearCheck();
                    radioGroup.check(R.id.radioButtonLimited);
                    radioGroup.setVisibility(View.GONE);
                    radioText.setVisibility(View.GONE);

                    chipMonday.setChecked(timeLimit.getDays().Monday);
                    chipTuesday.setChecked(timeLimit.getDays().Tuesday);
                    chipWednesday.setChecked(timeLimit.getDays().Wednesday);
                    chipThursday.setChecked(timeLimit.getDays().Thursday);
                    chipFriday.setChecked(timeLimit.getDays().Friday);
                    chipSaturday.setChecked(timeLimit.getDays().Saturday);
                    chipSunday.setChecked(timeLimit.getDays().Sunday);

                    btnStartTime.setText(String.format("%s %s", btnStartTime.getTag().toString(), formatter.format(timeLimit.getStart())));
                    btnEndTime.setText(String.format("%s %s", btnEndTime.getTag().toString(), formatter.format(timeLimit.getEnd())));
                    break;

                case DailyHours:
                    radioGroup.check(R.id.radioButtonDaily);
                    radioGroup.setVisibility(View.GONE);
                    radioText.setVisibility(View.GONE);

                    chipMonday.setChecked(timeLimit.getDays().Monday);
                    chipTuesday.setChecked(timeLimit.getDays().Tuesday);
                    chipWednesday.setChecked(timeLimit.getDays().Wednesday);
                    chipThursday.setChecked(timeLimit.getDays().Thursday);
                    chipFriday.setChecked(timeLimit.getDays().Friday);
                    chipSaturday.setChecked(timeLimit.getDays().Saturday);
                    chipSunday.setChecked(timeLimit.getDays().Sunday);

                    btnDuration.setText(String.format("%s %s", btnDuration.getTag().toString(), formatter.format(timeLimit.getDuration())));
                    break;

                case AppDailyHours:
                    radioGroup.check(R.id.radioButtonAppDaily);
                    radioGroup.setVisibility(View.GONE);
                    radioText.setVisibility(View.GONE);

                    for (App app: allowedApps) {
                        if (app.getId().equals(timeLimit.getAppName())) {
                            btnChooseApp.setText(app.getName());
                            break;
                        }
                    }
                    btnDuration.setText(String.format("%s %s", btnDuration.getTag().toString(), formatter.format(timeLimit.getDuration())));
                    break;
            }
        } else {
            isNewTimeLimit = true;
            timeLimit = new TimeLimit();
            radioGroup.clearCheck();
            radioGroup.check(R.id.radioButtonLimited);
            btnDelete.setVisibility(View.GONE);
            timeLimit.setDays(new TimeLimit.Days(127));
            timeLimit.setStatus(true);
        }
    }

    public void showDialog() {
        alert = new AlertDialog.Builder(getContext())
                .setView(this)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Boolean success = true;
                        if (timeLimit.getType() == LimitedHours) {
                            if (timeLimit.getStart() == null || timeLimit.getEnd() == null || timeLimit.getDays().getCode() == 0)
                                success = false;
                        }
                        else if (timeLimit.getType() == DailyHours) {
                            if (timeLimit.getDuration() == null || timeLimit.getDays().getCode() == 0)
                                success = false;
                        }
                        else if (timeLimit.getType() == AppDailyHours) {
                            if (timeLimit.getDuration() == null || timeLimit.getAppName() == null || timeLimit.getAppName().isEmpty())
                                success = false;
                        }
                        if (success) {
                            creator.onAlertSuccess(timeLimit);
                        }
                        else {
                            HashMap<String, Object> func = new HashMap<>();
                            func.put("toast", getResources().getString(R.string.cannot_save_limitation));
                            creator.onAlertFunction(func);
                        }
                    }
                })
                .create();
        if (isNewTimeLimit) alert.setTitle(getResources().getString(R.string.new_time_limit));
        else if (timeLimit.getType() == LimitedHours) alert.setTitle(R.string.limited_hours);
        else if (timeLimit.getType() == DailyHours) alert.setTitle(R.string.daily_time_limits);
        else if (timeLimit.getType() == AppDailyHours) alert.setTitle(R.string.app_time_limit);
        alert.show();
    }
}
