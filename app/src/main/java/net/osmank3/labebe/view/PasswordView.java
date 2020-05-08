/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import net.osmank3.labebe.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class PasswordView extends ConstraintLayout {
    private List<RadioButton> rbtnPswd;
    private List<Button> btnPswd;
    private TextView descriptionPswd, textPswd;
    private StringBuilder password = new StringBuilder();
    private String hashedPassword;
    private boolean status;
    private PasswordReturnHandler creator;

    public PasswordView(Context context) {
        super(context);
        init(context, null);
    }

    public PasswordView(Context context, PasswordReturnHandler creator){
        super(context);
        this.creator = creator;
        init(context, null);
    }

    public PasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        status = false;
        rbtnPswd = new ArrayList<>();
        btnPswd = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View listView = inflater.inflate(R.layout.password_view, this);

        descriptionPswd = findViewById(R.id.descriptionPswd);
        textPswd = findViewById(R.id.textPswd);
        rbtnPswd.add((RadioButton) findViewById(R.id.rbtnPswd1));
        rbtnPswd.add((RadioButton) findViewById(R.id.rbtnPswd2));
        rbtnPswd.add((RadioButton) findViewById(R.id.rbtnPswd3));
        rbtnPswd.add((RadioButton) findViewById(R.id.rbtnPswd4));
        rbtnPswd.add((RadioButton) findViewById(R.id.rbtnPswd5));
        rbtnPswd.add((RadioButton) findViewById(R.id.rbtnPswd6));
        btnPswd.add((Button) findViewById(R.id.btnPswd0));
        btnPswd.add((Button) findViewById(R.id.btnPswd1));
        btnPswd.add((Button) findViewById(R.id.btnPswd2));
        btnPswd.add((Button) findViewById(R.id.btnPswd3));
        btnPswd.add((Button) findViewById(R.id.btnPswd4));
        btnPswd.add((Button) findViewById(R.id.btnPswd5));
        btnPswd.add((Button) findViewById(R.id.btnPswd6));
        btnPswd.add((Button) findViewById(R.id.btnPswd7));
        btnPswd.add((Button) findViewById(R.id.btnPswd8));
        btnPswd.add((Button) findViewById(R.id.btnPswd9));
        btnPswd.add((Button) findViewById(R.id.btnPswdBackspace));
        Button btnPswdOK = findViewById(R.id.btnPswdOK);
        btnPswdOK.setVisibility(INVISIBLE);

        View.OnClickListener btnPswdOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.length() < 6) {
                    if (v.getId() == R.id.btnPswdBackspace) {
                        if (password.length() > 0) {
                            rbtnPswd.get(password.length() - 1).setChecked(false);
                            password.deleteCharAt(password.length() - 1);
                        }
                    }
                    else {
                        rbtnPswd.get(password.length()).setChecked(true);
                        password.append(((Button) v).getText().toString());
                        if (password.length() == 6) {
                            MessageDigest digest = null;
                            try {
                                digest = MessageDigest.getInstance("SHA-256");
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            digest.reset();
                            byte[] hashed = digest.digest(password.toString().getBytes());
                            StringBuilder hash = new StringBuilder(hashed.length * 2);
                            for (byte b : hashed) {
                                hash.append(String.format("%02x", b & 0xFF));
                            }

                            hashedPassword = hash.toString();
                            creator.onPasswordFilled(hashedPassword);
                        }
                    }
                }
            }
        };

        for (Button btn: btnPswd) {
            btn.setOnClickListener(btnPswdOnClickListener);
        }
    }

    public void setCreator(PasswordReturnHandler creator) {
        this.creator = creator;
    }

    public void setTitle(String text) {
        descriptionPswd.setText(text);
    }

    public void setTitle(int resid) {
        descriptionPswd.setText(resid);
    }

    public void setTextPassword(String text) {
        textPswd.setText(text);
    }

    public void setTextPassword(int resid) {
        textPswd.setText(resid);
    }

    public void setStatusReset() {
        for (RadioButton radio: rbtnPswd) {
            radio.setChecked(false);
        }
        password.delete(0, 6);
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
