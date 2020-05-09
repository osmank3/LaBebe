/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;

import static android.content.Context.MODE_PRIVATE;

public class ChildFragment extends Fragment {
    private View root;
    private Child child;
    private FirebaseFirestore database;
    private SharedPreferences preferences;
    private TextInputLayout name, born;
    private ImageView childImage;
    private ImageButton btnColorPicker;
    private ChipGroup chipGender;
    private Chip chipBoy, chipGirl;
    private Button btnAppDecisions, btnTimeLimitations, btnResetPassword, btnDelete;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_child, container, false);

        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable("child");
        }

        initComponents();
        registerEventHandlers();
        return root;
    }

    @Override
    public void onPause(){
        super.onPause();
        doDatabaseUpdates();
    }

    private void initComponents() {
        childImage = root.findViewById(R.id.childImage);
        name = root.findViewById(R.id.textName);
        born = root.findViewById(R.id.textBorn);
        btnColorPicker = root.findViewById(R.id.btnColorPicker);
        chipGender = root.findViewById(R.id.chipGender);
        chipBoy = root.findViewById(R.id.chipBoy);
        chipGirl = root.findViewById(R.id.chipGirl);
        btnAppDecisions = root.findViewById(R.id.btnAppDecisions);
        btnTimeLimitations = root.findViewById(R.id.btnTimeLimitations);
        btnResetPassword = root.findViewById(R.id.btnResetPassword);
        btnDelete = root.findViewById(R.id.btnDelete);

        database = FirebaseFirestore.getInstance();
        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);

        if (child == null){
            child = new Child();
            DocumentReference childDBRef = database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document();
            child.setId(childDBRef.getId());
        } else {
            name.getEditText().setText(child.getName());
            colorChanged(child.getColor());
            born.getEditText().setText(child.getBorn().toString());
            if (child.getGender() != null && child.getGender().equals("male"))
                chipBoy.setChecked(true);
            else if (child.getGender() != null && child.getGender().equals("female"))
                chipGirl.setChecked(true);
        }
    }

    private void registerEventHandlers() {
        btnColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerDialog.Builder(getContext())
                        .setTitle(R.string.pick_color)
                        .setPositiveButton(getString(android.R.string.ok),
                                new ColorEnvelopeListener() {
                                    @Override
                                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                        colorChanged(envelope.getColor());
                                    }
                                })
                        .setNegativeButton(getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .attachAlphaSlideBar(false)
                        .show();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = name.getEditText().getText().toString(),
                        bornString = born.getEditText().getText().toString();
                if (nameString.isEmpty())
                    name.setError(getResources().getText(R.string.field_required));
                else {
                    child.setName(nameString);
                    name.setErrorEnabled(false);
                }
                if (bornString.isEmpty())
                    born.setError(getResources().getText(R.string.field_required));
                else {
                    child.setBorn(Integer.decode(bornString));
                    born.setErrorEnabled(false);
                }
                if (!nameString.isEmpty() && !bornString.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putSerializable("child", child);
                    if (v.getId() == btnAppDecisions.getId()) {
                        MainActivity.navController.navigate(R.id.action_child_to_appDecisions, args);
                    } else if (v.getId() == btnTimeLimitations.getId()) {
                        MainActivity.navController.navigate(R.id.action_child_to_timeLimits, args);
                    }
                }
            }
        };

        btnTimeLimitations.setOnClickListener(listener);
        btnAppDecisions.setOnClickListener(listener);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                child.setPasswordHash(null);
                Toast.makeText(getContext(), R.string.password_reset, Toast.LENGTH_LONG).show();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alert = new AlertDialog.Builder(getContext())
                        .setTitle(getResources().getText(R.string.delete) + "?")
                        .setMessage(R.string.are_you_sure_to_delete)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database.collection("users")
                                        .document(preferences.getString("userUid", ""))
                                        .collection("children")
                                        .document(child.getId())
                                        .delete();
                                child = null;
                                MainActivity.navController.popBackStack();
                            }
                        })
                        .create();
                alert.show();
            }
        });

        chipGender.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId == R.id.chipBoy)
                    child.setGender("male");
                else
                    child.setGender("female");
            }
        });

        name.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                child.setName(s.toString());
            }
        });

        born.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())
                    child.setBorn(Integer.decode(s.toString()));
            }
        });
    }

    private void doDatabaseUpdates() {
        if (!name.getEditText().getText().toString().isEmpty() && !born.getEditText().getText().toString().isEmpty() && child != null) {
            //child.setName(name.getEditText().getText().toString());
            //child.setBorn(Integer.decode(born.getEditText().getText().toString()));
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .set(child, SetOptions.merge());
        }
    }

    private void colorChanged(int color) {
        child.setColor(color);
        childImage.setImageTintList(ColorStateList.valueOf(color));
        btnColorPicker.setImageTintList(ColorStateList.valueOf(color));
    }
}
