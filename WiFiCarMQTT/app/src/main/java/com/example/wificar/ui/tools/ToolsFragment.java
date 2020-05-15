package com.example.wificar.ui.tools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.wificar.R;
import com.google.android.material.snackbar.Snackbar;

public class ToolsFragment extends Fragment {

    private ToolsViewModel toolsViewModel;
    public SharedPreferences.Editor editor;
    public SharedPreferences sharedPreferences;
    public Button saveButton;
    public EditText hostEditText,userNameEditText,passwordEditText,
            subscribeEditText,publishEditText, speedMotorA_EditText, speedMotorB_EditText;
    public final static String PREF_BROKER = "PREF_BROKER", PREF_USERNAME = "PREF_USERNAME"
            , PREF_PASSWORD ="PREF_PASSWORD", PREF_SUBSCRIBE="PREF_SUBSCRIBE",PREF_PUBLISH="PREF_PUBLISH",
            PREF_MAX_SPEED_MOTOR_A="PREF_MAX_SPEED_MOTOR_A",PREF_MAX_SPEED_MOTOR_B="PREF_MAX_SPEED_MOTOR_B";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        saveButton = root.findViewById(R.id.save_button);
        hostEditText = root.findViewById(R.id.host_editText);
        userNameEditText = root.findViewById(R.id.username_editText);
        passwordEditText = root.findViewById(R.id.password_editText);
        subscribeEditText = root.findViewById(R.id.subscribe_editText);
        publishEditText = root.findViewById(R.id.publish_editText);
        speedMotorA_EditText = root.findViewById(R.id.speedMotorA_editText);
        speedMotorB_EditText = root.findViewById(R.id.speedMotorB_editText);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        hostEditText.setText (sharedPreferences.getString(PREF_BROKER, "tcp://test.mosquitto.org:1883"));
        userNameEditText.setText(sharedPreferences.getString(PREF_USERNAME,""));
        passwordEditText.setText(sharedPreferences.getString(PREF_PASSWORD,""));
        subscribeEditText.setText(sharedPreferences.getString(PREF_SUBSCRIBE,"wificar/status"));
        publishEditText.setText(sharedPreferences.getString(PREF_PUBLISH,"wificar/control"));
        speedMotorA_EditText.setText(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_A,"100"));
        speedMotorB_EditText.setText(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_A,"100"));



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.valueOf(speedMotorA_EditText.getText().toString())>100 ) speedMotorA_EditText.setText("100");
                if(Integer.valueOf(speedMotorB_EditText.getText().toString())>100 ) speedMotorB_EditText.setText("100");
                editor.putString(PREF_BROKER, hostEditText.getText().toString());
                editor.putString(PREF_USERNAME,userNameEditText.getText().toString());
                editor.putString(PREF_PASSWORD, passwordEditText.getText().toString());
                editor.putString(PREF_SUBSCRIBE,subscribeEditText.getText().toString());
                editor.putString(PREF_PUBLISH,publishEditText.getText().toString());
                editor.putString(PREF_MAX_SPEED_MOTOR_A,speedMotorA_EditText.getText().toString());
                editor.putString(PREF_MAX_SPEED_MOTOR_B,speedMotorB_EditText.getText().toString());
                editor.commit();
                Snackbar.make(getActivity().findViewById(android.R.id.content),"Configuration set saved successfully!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });


        return root;
    }
}