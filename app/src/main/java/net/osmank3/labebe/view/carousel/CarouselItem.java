/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view.carousel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.view.PasswordReturnHandler;
import net.osmank3.labebe.view.PasswordView;

import static android.content.Context.MODE_PRIVATE;

public class CarouselItem implements PasswordReturnHandler {
    private String title;
    private Integer icon;
    private Integer color;
    private Context context;
    private Child child;
    private String hashedPassword;
    private AlertDialog passwordAlert;
    private PasswordView passwordView;
    private Boolean isNewPassword = false;
    private Integer alertCounter = 3;
    private FirebaseFirestore database;
    private SharedPreferences preferences;

    public CarouselItem(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(context.getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();

        title = context.getResources().getString(R.string.parent);
        hashedPassword = preferences.getString("parent_password_hash", "");
        icon = R.drawable.baseline_supervisor_account_black_24dp;
        color = Color.BLACK;
    }

    public CarouselItem(Context context, Child child) {
        this(context);
        setChild(child);
    }

    public void setChild(Child child) {
        this.child = child;
        hashedPassword = child.getPasswordHash();
        isNewPassword = hashedPassword == null;
        title = child.getName();
        icon = R.drawable.round_person_outline_black_48dp;
        color = child.getColor();
    }

    public String getTitle() {return title;}
    public Integer getIcon() {return icon;}
    public Integer getColor() {return color;}

    public void performClick() {
        passwordView = new PasswordView(context, this);
        passwordView.setTitle(null);
        passwordAlert = new AlertDialog.Builder(context)
                .setView(passwordView)
                .create();
        passwordAlert.show();
    }

    @Override
    public void onPasswordFilled(String passwordHash) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            hashedPassword = passwordHash;
            passwordView.setStatusReset();
            passwordView.setTextPassword(R.string.re_password);
        } else {
            if (hashedPassword.equals(passwordHash)) {
                passwordAlert.dismiss();
                if (child != null && isNewPassword) {
                    child.setPasswordHash(passwordHash);
                    database.collection("users")
                            .document(preferences.getString("userUid", ""))
                            .collection("children")
                            .document(child.getId())
                            .set(child, SetOptions.merge())
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("LaBebePasswordFragment", "Error adding document", e);
                                }
                            });

                    Bundle args = new Bundle();
                    args.putSerializable("child", child);
                    MainActivity.navController.navigate(R.id.action_home_to_account, args);
                } else {
                    MainActivity.navController.navigate(R.id.action_home_to_account);
                }
            } else {
                alertCounter--;
                if (alertCounter == 0) {
                    passwordAlert.dismiss();
                    alertCounter = 3;
                    if (isNewPassword)
                        hashedPassword = null;
                }
                passwordView.setStatusReset();
                Snackbar.make(passwordView, R.string.password_incorrect, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }
    }
}
