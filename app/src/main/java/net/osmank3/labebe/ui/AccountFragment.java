/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import net.osmank3.labebe.LaBebeAccessibilityService;
import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.PasswordReturnHandler;
import net.osmank3.labebe.view.PasswordView;

import java.util.HashMap;

public class AccountFragment extends Fragment implements PasswordReturnHandler {
    private View root;
    private AccountFragment instance = this;
    private PasswordView passwordView;
    private AlertDialog passwordAlert;
    private ContextThemeWrapper contextThemeWrapper;
    private Button btnPasswordChange, btnMessages, btnStatistics, btnGeneralAppDecisions, btnChildren, btnLend, btnUninstall;
    private Child child;
    private String hashedPassword;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private Boolean isOldPasswordTrue = false;
    private int passwordCount = 3;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_account, container, false);

        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable("child");
        }

        initComponents();
        registerEventHandlers();

        return root;
    }

    private void initComponents() {
        btnPasswordChange = root.findViewById(R.id.btnPasswordChange);
        btnMessages = root.findViewById(R.id.btnMessages);
        btnStatistics = root.findViewById(R.id.btnStatistics);
        btnGeneralAppDecisions = root.findViewById(R.id.btnGeneralAppDecisions);
        btnChildren = root.findViewById(R.id.btnChildren);
        btnLend = root.findViewById(R.id.btnLend);
        btnUninstall = root.findViewById(R.id.btnUninstall);

        database = FirebaseFirestore.getInstance();
        preferences = getContext().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        if (child == null) {
            btnGeneralAppDecisions.setVisibility(View.VISIBLE);
            btnChildren.setVisibility(View.VISIBLE);
            btnUninstall.setVisibility(View.VISIBLE);
            if (preferences.getBoolean("isDeviceParental", false)) {
                if (checkLoan()) {
                    btnLend.setText(R.string.get_back);
                } else {
                    btnLend.setText(R.string.lend);
                }
                btnLend.setVisibility(View.VISIBLE);
            } else {
                btnLend.setVisibility(View.GONE);
            }
        } else {
            btnGeneralAppDecisions.setVisibility(View.GONE);
            btnChildren.setVisibility(View.GONE);
            btnLend.setVisibility(View.GONE);
            btnUninstall.setVisibility(View.GONE);
        }

        if (child == null) {
            hashedPassword = preferences.getString("parent_password_hash", "");
        } else {
            hashedPassword = child.getPasswordHash();
        }
    }

    private void registerEventHandlers() {
        btnPasswordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.AppTheme_Alert);
                passwordView = new PasswordView(contextThemeWrapper, instance);
                passwordView.setTitle(null);
                passwordAlert = new AlertDialog.Builder(contextThemeWrapper)
                        .setView(passwordView)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                passwordCount = 3;
                                isOldPasswordTrue = false;

                                if (child == null) {
                                    hashedPassword = preferences.getString("parent_password_hash", "");
                                } else {
                                    hashedPassword = child.getPasswordHash();
                                }
                            }
                        })
                        .create();
                passwordAlert.show();
            }
        });

        btnMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (child != null) {
                    Bundle args = new Bundle();
                    args.putSerializable("child", child);
                    MainActivity.navController.navigate(R.id.action_account_to_messages, args);
                } else {
                    MainActivity.navController.navigate(R.id.action_account_to_messages);
                }
            }
        });

        btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (child != null) {
                    Bundle args = new Bundle();
                    args.putSerializable("child", child);
                    MainActivity.navController.navigate(R.id.action_account_to_statistics, args);
                } else {
                    MainActivity.navController.navigate(R.id.action_account_to_statistics);
                }
            }
        });

        btnGeneralAppDecisions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigate(R.id.action_account_to_appDecisions);
            }
        });

        btnChildren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navController.navigate(R.id.action_account_to_children);
            }
        });

        btnLend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLoan()) {
                    preferences.edit().putBoolean("isLent", false).apply();
                    btnLend.setText(R.string.lend);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        LaBebeAccessibilityService.instance.disableSelf();
                    }
                } else {
                    MainActivity.navController.navigate(R.id.action_account_to_permissions);
                }
            }
        });

        btnUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.are_you_sure_to_uninstall)
                        .setMessage(R.string.are_you_sure_to_uninstall_description)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit().putBoolean("isLent", false).apply();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    LaBebeAccessibilityService.instance.disableSelf();
                                }
                                initComponents();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                dialog.show();
            }
        });
    }

    @Override
    public void onPasswordFilled(String passwordHash) {
        if (passwordHash.equals(hashedPassword) && !isOldPasswordTrue) {
            passwordView.setStatusReset();
            passwordView.setTextPassword(R.string.password_new);
            isOldPasswordTrue = true;
            hashedPassword = null;
        } else if (isOldPasswordTrue) {
            if (hashedPassword == null) {
                hashedPassword = passwordHash;
                passwordView.setStatusReset();
                passwordView.setTextPassword(R.string.re_password);
            } else if (hashedPassword.equals(passwordHash)) {
                if (child == null) {
                    preferences.edit().putString("parent_password_hash", hashedPassword);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("parent_password_hash", hashedPassword);
                    database.collection("users")
                            .document(preferences.getString("userUid", ""))
                            .set(data, SetOptions.merge());
                } else {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("passwordHash", hashedPassword);
                    database.collection("users")
                            .document(preferences.getString("userUid", ""))
                            .collection("children")
                            .document(child.getId())
                            .set(data, SetOptions.merge());
                }
                passwordView.setStatusReset();
                passwordAlert.dismiss();
                isOldPasswordTrue = false;
                passwordCount = 3;
            } else {
                passwordCount--;
                if (passwordCount == 0) {
                    passwordAlert.cancel();
                } else {
                    passwordView.setStatusReset();
                    Snackbar.make(passwordView, R.string.password_incorrect, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }
        } else {
            passwordCount--;
            if (passwordCount == 0) {
                passwordAlert.cancel();
            } else {
                passwordView.setStatusReset();
                Snackbar.make(passwordView, R.string.password_incorrect, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }
    }

    private Boolean checkLoan() {
        Boolean status = false;
        if (LaBebeAccessibilityService.instance != null && preferences.getBoolean("isLent", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(getContext())) {
                    status = true;
                }
            } else {
                status = true;
            }
        }

        return status;
    }
}
