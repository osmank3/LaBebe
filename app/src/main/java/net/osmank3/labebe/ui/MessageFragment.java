/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import net.osmank3.labebe.R;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.db.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class MessageFragment extends Fragment {
    private View root;
    private ScrollView scrollView;
    private LinearLayout llMessages;
    private TextView textTo;
    private TextInputLayout messageLayout;
    private ImageButton btnSend;
    private SharedPreferences preferences;
    private FirebaseFirestore database;
    private String TAG = "LaBebeMessageLayout";
    private Child child, messageTo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_message, container, false);

        if (getArguments() != null && getArguments().containsKey("child")) {
            child = (Child) getArguments().getSerializable("child");
        }
        if (getArguments() != null && getArguments().containsKey("messageTo")) {
            messageTo = (Child) getArguments().getSerializable("messageTo");
        }

        initComponents();
        registerEventHandlers();

        return root;
    }

    private void initComponents() {
        scrollView = root.findViewById(R.id.scroll);
        llMessages = root.findViewById(R.id.llMessages);
        textTo = root.findViewById(R.id.textTo);
        messageLayout = root.findViewById(R.id.messageLayout);
        btnSend = root.findViewById(R.id.btnSend);

        preferences = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();

        if (messageTo != null)
            textTo.setText(messageTo.getName());
        else
            textTo.setText(R.string.parent);

        getMessagesFromDB();
    }

    private void registerEventHandlers() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageLayout.getEditText().getText().toString().equals("")) {
                    messageLayout.setError(getText(R.string.message_required));
                } else {
                    messageLayout.setErrorEnabled(false);
                    Message message = new Message();
                    message.setMessage(messageLayout.getEditText().getText().toString());
                    message.setTo(messageTo == null ? null : messageTo.getId());
                    message.setFrom(child == null ? null : child.getId());
                    message.setSendDate(Calendar.getInstance().getTime());
                    DocumentReference ref =database.collection("users")
                            .document(preferences.getString("userUid", ""))
                            .collection("messages")
                            .document();
                    message.setId(ref.getId());
                    ref.set(message, SetOptions.merge());
                    messageLayout.getEditText().setText("");
                    messageLayout.clearFocus();
                }
            }
        });
    }

    private void getMessagesFromDB() {
        database.collection("users")
                .document(preferences.getString("userUid", ""))
                .collection("messages")
                .orderBy("sendDate")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed", e);
                            return;
                        }

                        String source = snapshots != null && snapshots.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";

                        List<Message> messages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            if (doc.contains("message")) {
                                Message message = doc.toObject(Message.class);
                                if (child != null) {
                                    if (messageTo != null) {
                                        if (child.getId().equals(message.getFrom()) || child.getId().equals(message.getTo())) {
                                            if (messageTo.getId().equals(message.getFrom()) || messageTo.getId().equals(message.getTo()))
                                                messages.add(message);
                                        }
                                    } else {
                                        if (child.getId().equals(message.getFrom()) || child.getId().equals(message.getTo())) {
                                            if (message.getFrom() == null || message.getTo() == null)
                                                messages.add(message);
                                        }
                                    }
                                } else {
                                    if (messageTo.getId().equals(message.getFrom()) || messageTo.getId().equals(message.getTo())) {
                                        if (message.getFrom() == null || message.getTo() == null) {
                                            messages.add(message);
                                        }
                                    }
                                }
                            }
                        }
                        fillMessages(messages);
                    }
                });
    }

    private void fillMessages(List<Message> messages) {
        llMessages.removeAllViews();
        for (Message message: messages) {
            CardView card = new CardView(root.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, root.getResources().getDimensionPixelSize(R.dimen.margin_normal)/2, 0, root.getResources().getDimensionPixelSize(R.dimen.margin_normal)/2);
            card.setContentPadding(root.getResources().getDimensionPixelSize(R.dimen.margin_normal),
                    root.getResources().getDimensionPixelSize(R.dimen.margin_normal),
                    root.getResources().getDimensionPixelSize(R.dimen.margin_normal),
                    root.getResources().getDimensionPixelSize(R.dimen.margin_normal));
            TextView messageView = new TextView(root.getContext());
            messageView.setText(message.getMessage());
            messageView.setTextColor(ColorStateList.valueOf(root.getResources().getColor(R.color.white)));
            Boolean isSender = false;
            if (child != null) {
                if (message.getFrom() != null && message.getFrom().equals(child.getId())) {
                    isSender = true;
                }
            } else {
                if (message.getFrom() == null) {
                    isSender = true;
                }
            }

            if (isSender) {
                card.setCardBackgroundColor(root.getResources().getColor(R.color.colorAccent));
                params.gravity = Gravity.RIGHT;
                card.setLayoutParams(params);
            } else {
                card.setCardBackgroundColor(root.getResources().getColor(R.color.colorPrimary));
                params.gravity = Gravity.LEFT;
                card.setLayoutParams(params);
                if (!message.getRead()) {
                    message.setRead(true);
                    message.setReadDate(Calendar.getInstance().getTime());
                    database.collection("users")
                            .document(preferences.getString("userUid", ""))
                            .collection("messages")
                            .document(message.getId())
                            .set(message, SetOptions.merge());
                }
            }
            card.addView(messageView);
            card.setRadius(root.getResources().getDimension(R.dimen.margin_big));
            llMessages.addView(card);
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
