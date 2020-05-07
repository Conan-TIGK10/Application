package com.hero.elias.conanapp;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SessionFragment extends Fragment {

    private TextView sessionError;
    private EditText submitForm;
    private Button submitButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionError = view.findViewById(R.id.sessionErrors);

        submitForm = view.findViewById(R.id.sessionForm);

        submitButton = view.findViewById(R.id.sessionSubmit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String form = String.valueOf(submitForm.getText());
                try {
                    WifiHandler.getInstance().createSession(form, new WifiHandler.SessionCreateListener() {
                        @Override
                        public void onFinished(boolean error, String message) {
                            if (error) {
                                sessionError.setVisibility(View.VISIBLE);
                                sessionError.setText(message);
                            } else {
                                closeFragment();
                            }
                        }
                    });
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
