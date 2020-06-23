package com.example.wificar.ui.tools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.wificar.R;
import com.google.android.material.snackbar.Snackbar;

public class ToolsFragment extends Fragment {

    private ToolsViewModel toolsViewModel;
    public SharedPreferences.Editor editor;
    public SharedPreferences sharedPreferences;
    public ImageButton saveButton;
    public EditText hostEditText,userNameEditText,passwordEditText,
            subscribeEditText,publishEditText, speedMotorA_EditText, speedMotorB_EditText,
            curveAngle_EditText, cameraIP_EditText, cameraPublishTopic, cameraSubscribeTopic;
    public final static String PREF_BROKER = "PREF_BROKER", PREF_USERNAME = "PREF_USERNAME"
            , PREF_PASSWORD ="PREF_PASSWORD", PREF_SUBSCRIBE="PREF_SUBSCRIBE",PREF_PUBLISH="PREF_PUBLISH",
            PREF_MAX_SPEED_MOTOR_A="PREF_MAX_SPEED_MOTOR_A",PREF_MAX_SPEED_MOTOR_B="PREF_MAX_SPEED_MOTOR_B",
            PREF_CURVE_ANGLE = "PREF CURVE ANGLE",PREF_IPCAMERA_URL="PREF_IPCAMERA_URL",
            PREF_CAMERA_PUBLISH_TOPIC = "PREF_CAMERA_PUBLISH_TOPIC", PREF_CAMERA_SUBSCRIBE_TOPIC = "PREF_CAMERA_SUBSCRIBE_TOPIC";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        saveButton = root.findViewById(R.id.save_button);
        hostEditText = root.findViewById(R.id.host_editText);
        userNameEditText = root.findViewById(R.id.username_editText);
        passwordEditText = root.findViewById(R.id.password_editText);
        subscribeEditText = root.findViewById(R.id.subscriber_editText);
        publishEditText = root.findViewById(R.id.publisher_editText);
        speedMotorA_EditText = root.findViewById(R.id.speedMotorA_editText);
        speedMotorB_EditText = root.findViewById(R.id.speedMotorB_editText);
        curveAngle_EditText = root.findViewById(R.id.curveAngle_editText);
        cameraIP_EditText = root.findViewById(R.id.cameraIp_EditText);
        cameraPublishTopic = root.findViewById(R.id.cameraPublishTopic_EditText);
        cameraSubscribeTopic = root.findViewById(R.id.cameraSubscribeTopic_EditText);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        hostEditText.setText (sharedPreferences.getString(PREF_BROKER, "tcp://broker.hivemq.com:1883"));
        userNameEditText.setText(sharedPreferences.getString(PREF_USERNAME,""));
        passwordEditText.setText(sharedPreferences.getString(PREF_PASSWORD,""));
        subscribeEditText.setText(sharedPreferences.getString(PREF_SUBSCRIBE,"wificar/status"));
        publishEditText.setText(sharedPreferences.getString(PREF_PUBLISH,"wificar/control"));
        speedMotorA_EditText.setText(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_A,"100"));
        speedMotorB_EditText.setText(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_B,"100"));
        curveAngle_EditText.setText(sharedPreferences.getString(PREF_CURVE_ANGLE,"0.4"));
        cameraIP_EditText.setText(sharedPreferences.getString(PREF_IPCAMERA_URL ,"http://192.168.15.102:8081"));
        cameraSubscribeTopic.setText(sharedPreferences.getString(PREF_CAMERA_SUBSCRIBE_TOPIC, "wificar/cam/status"));
        cameraPublishTopic.setText(sharedPreferences.getString(PREF_CAMERA_PUBLISH_TOPIC, "wificar/cam/control"));




        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.valueOf(speedMotorA_EditText.getText().toString())>100 ) speedMotorA_EditText.setText("100");
                if(Integer.valueOf(speedMotorB_EditText.getText().toString())>100 ) speedMotorB_EditText.setText("100");
                if(Double.valueOf(curveAngle_EditText.getText().toString())>1) curveAngle_EditText.setText("1");
                editor.putString(PREF_BROKER, hostEditText.getText().toString());
                editor.putString(PREF_USERNAME,userNameEditText.getText().toString());
                editor.putString(PREF_PASSWORD, passwordEditText.getText().toString());
                editor.putString(PREF_SUBSCRIBE,subscribeEditText.getText().toString());
                editor.putString(PREF_PUBLISH,publishEditText.getText().toString());
                editor.putString(PREF_MAX_SPEED_MOTOR_A,speedMotorA_EditText.getText().toString());
                editor.putString(PREF_MAX_SPEED_MOTOR_B,speedMotorB_EditText.getText().toString());
                editor.putString(PREF_CURVE_ANGLE,curveAngle_EditText.getText().toString());
                editor.putString(PREF_IPCAMERA_URL,cameraIP_EditText.getText().toString());
                editor.putString(PREF_CAMERA_PUBLISH_TOPIC, cameraPublishTopic.getText().toString());
                editor.putString(PREF_CAMERA_SUBSCRIBE_TOPIC, cameraSubscribeTopic.getText().toString());
                editor.commit();
                Snackbar.make(getActivity().findViewById(android.R.id.content),"Configuration set saved successfully!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        saveButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        saveButton.setImageDrawable(getResources().getDrawable(R.mipmap.savebutton_clicked));
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        saveButton.setImageDrawable(getResources().getDrawable(R.mipmap.savebutton));
                        if (isAClick(startX, endX, startY, endY)) {

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
                        break;
                }
                v.getParent().requestDisallowInterceptTouchEvent(true); //specific to my project
                return false; //specific to my project
            }

            private boolean isAClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
            }
        });


        return root;
    }
}