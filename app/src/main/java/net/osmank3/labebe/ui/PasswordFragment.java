/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import net.osmank3.labebe.view.PasswordReturnHandler;
import net.osmank3.labebe.view.PasswordView;

import java.util.HashMap;

public class PasswordFragment extends Fragment implements PasswordReturnHandler {
    private PasswordView passwordView;
    private String hashedPassword;
    private Boolean hashedFromDB;
    private SharedPreferences preferences;
    private FirebaseFirestore database;
    private HashMap<String, Object> dbDocumentPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        passwordView = new PasswordView(getContext(), this);

        initComponents();
        //registerEventHandlers();

        return passwordView;
    }

    private void initComponents() {
        passwordView.setTextPassword(R.string.password);
        passwordView.setTitle(R.string.set_parent_password);

        preferences = getContext().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();

        hashedPassword = preferences.getString("parent_password_hash", "");
        hashedFromDB = false;

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
    }

    @Override
    public void onPause() {
        super.onPause();
        passwordView.setStatusReset();
    }

    private void setVariables() {
        if (dbDocumentPreferences != null) {
            if (dbDocumentPreferences.containsKey("parent_password_hash")) {
                if (hashedPassword.isEmpty())
                    hashedPassword = (String) dbDocumentPreferences.get("parent_password_hash");
                hashedFromDB = true;
            }
        }
    }

    @Override
    public void onPasswordFilled(String passwordHash) {
        if (hashedPassword.isEmpty()) {
            hashedPassword = passwordHash;
            passwordView.setStatusReset();
            passwordView.setTextPassword(R.string.re_password);
        } else if (hashedFromDB) {
            if (hashedPassword.equals(passwordHash)) {
                preferences.edit().putString("parent_password_hash", hashedPassword).apply();
                passwordView.setStatusReset();
                MainActivity.navController.navigate(R.id.action_password_to_appDecisions);
            } else {
                Toast.makeText(getContext(), R.string.password_incorrect, Toast.LENGTH_LONG).show();
                passwordView.setStatusReset();
            }
        } else {
            if (hashedPassword.equals(passwordHash)) {
                preferences.edit().putString("parent_password_hash", hashedPassword).apply();
                HashMap<String, Object> data = new HashMap<>();
                data.put("parent_password_hash", hashedPassword);
                database.collection("users")
                        .document(preferences.getString("userUid", ""))
                        .set(data, SetOptions.merge())
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("LaBebePasswordFragment", "Error adding document", e);
                            }
                        });
                passwordView.setStatusReset();
                MainActivity.navController.navigate(R.id.action_password_to_appDecisions);
            } else {
                Toast.makeText(getContext(), R.string.password_mismatch, Toast.LENGTH_LONG).show();
                passwordView.setStatusReset();
            }
        }
    }
}
