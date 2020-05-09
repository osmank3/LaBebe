/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import net.osmank3.labebe.MainActivity;
import net.osmank3.labebe.R;


public class SignUpFragment extends Fragment {
    private View root;
    private TextInputLayout textEmail, textPassword;
    private Button btnLogin, btnSignup, btnForgetPassword;
    private SignInButton btnSignupGoogle;
    private View.OnClickListener btnListener;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_sign_up, container, false);
        initComponents();
        registerEventHandlers();
        return root;
    }

    private void initComponents() {
        mAuth = FirebaseAuth.getInstance();
        textEmail = root.findViewById(R.id.textEmailLayout);
        textPassword = root.findViewById(R.id.textPasswordLayout);
        btnLogin = root.findViewById(R.id.btnLogin);
        btnSignup = root.findViewById(R.id.btnSignUp);
        btnForgetPassword = root.findViewById(R.id.btnForgetPassword);
        btnSignupGoogle = root.findViewById(R.id.btnSignInGoogle);
    }

    private void registerEventHandlers() {
        btnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnSignInGoogle) {
                    signUpGoogle();
                }
                else {
                    String  email = textEmail.getEditText().getText().toString(),
                            password = textPassword.getEditText().getText().toString();
                    if (email.isEmpty())
                        textEmail.setError(getResources().getString(R.string.email_required));
                    else
                        textEmail.setErrorEnabled(false);
                    if (v.getId() == R.id.btnForgetPassword) {
                        if (!email.isEmpty())
                            forgetPassword(email);
                    } else {
                        if (password.isEmpty())
                            textPassword.setError(getResources().getText(R.string.password_required));
                        else
                            textPassword.setErrorEnabled(false);
                        if (!email.isEmpty() && !password.isEmpty()) {
                            if (v.getId() == R.id.btnLogin)
                                login(email, password);
                            else if (v.getId() == R.id.btnSignUp)
                                signUp(email, password);
                        }
                    }
                }
            }
        };

        btnLogin.setOnClickListener(btnListener);
        btnSignup.setOnClickListener(btnListener);
        btnForgetPassword.setOnClickListener(btnListener);
        btnSignupGoogle.setOnClickListener(btnListener);
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user != null) {
                                SharedPreferences preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                                preferences.edit().putString("userEmail", user.getEmail()).apply();
                                preferences.edit().putString("userUid", user.getUid()).apply();
                                MainActivity.navController.navigate(R.id.action_signUp_to_deviceType);
                            }
                        } else {
                            if (task.getException().getClass().equals(FirebaseAuthInvalidUserException.class)) {
                                textEmail.getEditText().setText("");
                                textPassword.getEditText().setText("");
                                Snackbar.make(root, R.string.user_not_found, Snackbar.LENGTH_LONG).show();
                            } else if (task.getException().getClass().equals(FirebaseAuthInvalidCredentialsException.class)) {
                                textPassword.getEditText().setText("");
                                Snackbar.make(root, R.string.password_incorrect, Snackbar.LENGTH_LONG).show();
                            } else if (task.getException().getClass().equals(FirebaseTooManyRequestsException.class)){
                                textPassword.getEditText().setText("");
                                Snackbar.make(root, R.string.too_many_login_try, Snackbar.LENGTH_LONG).show();
                            } else {
                                Log.w(getClass().toString(), "Unknown Issue: " + task.getException());
                                Snackbar.make(root, R.string.unknown_issue, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) {
                                SharedPreferences preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                                preferences.edit().putString("userEmail", user.getEmail()).apply();
                                preferences.edit().putString("userUid", user.getUid()).apply();
                                MainActivity.navController.navigate(R.id.action_signUp_to_deviceType);
                            }
                        } else {
                            Log.w(getClass().toString(), "Unknown Issue: " + task.getException());
                            Snackbar.make(root, R.string.unknown_issue, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void forgetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            Snackbar.make(root, R.string.password_reset_mail_sent, Snackbar.LENGTH_LONG).show();
                        else {
                            Log.w(getClass().toString(), "Unknown Issue: " + task.getException());
                            Snackbar.make(root, R.string.unknown_issue, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void signUpGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this.getActivity(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user != null) {
                                        SharedPreferences preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                                        preferences.edit().putString("userEmail", user.getEmail()).apply();
                                        preferences.edit().putString("userUid", user.getUid()).apply();
                                        MainActivity.navController.navigate(R.id.action_signUp_to_deviceType);
                                    }
                                } else {
                                    Log.w(getClass().toString(), "Unknown Issue: " + task.getException());
                                    Snackbar.make(root, R.string.unknown_issue, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
}
