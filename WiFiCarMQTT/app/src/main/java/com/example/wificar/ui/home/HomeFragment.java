package com.example.wificar.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wificar.mqtt.PahoMqttClient;
import com.example.wificar.R;
import com.google.android.material.snackbar.Snackbar;
import com.longdo.mjpegviewer.MjpegView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;


import io.github.controlwear.virtual.joystick.android.JoystickView;

public class HomeFragment extends Fragment {

    private JoystickView joyStickView;
    private TextView batteryTextView, distanceTextView, brokerStatus_TextView, robotStatus_TextView;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;
    private String clientid, urlBroker, username, password, movement = "neutral",
            subscribeTopic, publishTopic, msg_new, video_url, camSubscribeTopic, camPublishTopic;
    private Handler handler;
    private View view;
    private ImageButton accelerateButton, brakeButton, turnCamLeftButton, turnCamRightButton, centerCamButton, lightButton;
    private boolean isSubscribed = false, isRobotConnected = false, pressedUp = false, pressedUp_brake = false,
            pressedUp_turnCamRight = false, pressedUp_turnCamLeft = false, pressedUp_camCenter = false, light_state = false, pressedUp_light = false;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private final static String PREF_BROKER = "PREF_BROKER", PREF_USERNAME = "PREF_USERNAME", PREF_PASSWORD = "PREF_PASSWORD",
            PREF_SUBSCRIBE = "PREF_SUBSCRIBE", PREF_PUBLISH = "PREF_PUBLISH",
            PREF_MAX_SPEED_MOTOR_A = "PREF_MAX_SPEED_MOTOR_A", PREF_MAX_SPEED_MOTOR_B = "PREF_MAX_SPEED_MOTOR_B",
            PREF_CURVE_ANGLE = "PREF CURVE ANGLE", PREF_IPCAMERA_URL = "PREF_IPCAMERA_URL",
            PREF_CAMERA_PUBLISH_TOPIC = "PREF_CAMERA_PUBLISH_TOPIC", PREF_CAMERA_SUBSCRIBE_TOPIC = "PREF_CAMERA_SUBSCRIBE_TOPIC";
    private int speedMotorA = 0, speedMotorB = 0, maxSpeedMOTOR_A = 0, maxSpeedMOTOR_B = 0, pastspeedMotorA = 0,
            moveNumber = 0, pastmoveNumber = 0, camPosition = 0;
    private double curveAngle;
    private MjpegView robotCam;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        joyStickView = root.findViewById(R.id.joystickViewDirection);
        batteryTextView = root.findViewById(R.id.battery_textView);
        distanceTextView = root.findViewById(R.id.distance_textView);
        brokerStatus_TextView = root.findViewById(R.id.status_textView);
        robotStatus_TextView = root.findViewById(R.id.status_textView4);
        accelerateButton = root.findViewById(R.id.accelerateButton);
        brakeButton = root.findViewById(R.id.brakeButton);
        robotCam = root.findViewById(R.id.mjpegview);
        turnCamLeftButton = root.findViewById(R.id.turnCamLeft_Button);
        turnCamRightButton = root.findViewById(R.id.turnCamRight_Button);
        centerCamButton = root.findViewById(R.id.centerCam_Button);
        lightButton = root.findViewById(R.id.light_Button);

        view = getActivity().findViewById(android.R.id.content);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        urlBroker = sharedPreferences.getString(PREF_BROKER, "tcp://test.mosquitto.org:1883");
        username = sharedPreferences.getString(PREF_USERNAME, "");
        password = sharedPreferences.getString(PREF_PASSWORD, "");
        subscribeTopic = sharedPreferences.getString(PREF_SUBSCRIBE, "wificar/status");
        publishTopic = sharedPreferences.getString(PREF_PUBLISH, "wificar/control");
        video_url = sharedPreferences.getString(PREF_IPCAMERA_URL, "http://192.168.15.102:8081");
        camPublishTopic = sharedPreferences.getString(PREF_CAMERA_PUBLISH_TOPIC, "wificar/cam/control");
        camSubscribeTopic = sharedPreferences.getString(PREF_CAMERA_SUBSCRIBE_TOPIC, "wificar/cam/status");


        maxSpeedMOTOR_A = Integer.valueOf(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_A, "255"));
        maxSpeedMOTOR_B = Integer.valueOf(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_B, "255"));
        curveAngle = Double.parseDouble(sharedPreferences.getString(PREF_CURVE_ANGLE, "0.4"));


        setListener();

        connectToBroker();
        subscribeTopicHandler(camSubscribeTopic);
        subscribeTopicHandler(subscribeTopic);

        // Called when a subscribed message is received

        mqttCallback();


        verifyConnection();

        robotCam.setAdjustHeight(true);
        robotCam.setAdjustWidth(true);
        robotCam.setMode(MjpegView.MODE_FIT_WIDTH);
        robotCam.setMsecWaitAfterReadImageError(1000);
        robotCam.setUrl(video_url);
        robotCam.setRecycleBitmap(true);
        robotCam.startStream();

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        client.unregisterResources();
        client.close();
        robotCam.stopStream();
    }

    @Override
    public void onResume() {
        super.onResume();
        robotCam.startStream();
    }

    @Override
    public void onPause() {
        super.onPause();
        client.unregisterResources();
        client.close();
        robotCam.stopStream();
    }

    @Override
    public void onStop() {
        client.unregisterResources();
        client.close();
        robotCam.stopStream();
        super.onStop();

    }

    protected void mqttCallback() {

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                //msg("Connection lost...");
                robotStatus_TextView.setTextColor(Color.RED);
                robotStatus_TextView.setText("Disconnected.");
                isRobotConnected = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                // msg_new = "Topic: " + topic + " Message: "+ message + "Cam Topic: " + camSubscribeTopic;
                // Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                // .setAction("Action", null).show();


                if (topic.equals("mycustomtopic1")) {
                    //Add custom message handling here (if topic = "mycustomtopic1")
                } else if (topic.equals(camSubscribeTopic)) {

                    if (message.toString().equals("ON")) {
                        light_state = true;
                        //lightButton.setImageDrawable(getResources().getDrawable(R.mipmap.lightbutton_clicked));
                    } else if (message.toString().equals("OFF")) {
                        light_state = false;
                        //lightButton.setImageDrawable(getResources().getDrawable(R.mipmap.lightbutton));
                    }
                } else if (topic.equals(subscribeTopic)) {

                    String msg = "Robot connected!";

                    JSONObject recievedJSON = new JSONObject(message.toString());
                    // get employee name and salary
                    double battery_double = Double.valueOf(recievedJSON.getString("voltage"));
                    battery_double = (((battery_double - 5.8) / 1.7) * 100);
                    battery_double = (double) Math.round(battery_double * 100) / 100;
                    if (battery_double < 0) battery_double = 0;
                    if (battery_double > 100) battery_double = 100;
                    batteryTextView.setText(battery_double + "%");
                    if (battery_double < 35)
                        batteryTextView.setTextColor(Color.RED);
                    else
                        batteryTextView.setTextColor(Color.GREEN);
                    int distance = Integer.valueOf(recievedJSON.getString("distance"));
                    if (distance < 16)
                        distanceTextView.setTextColor(Color.RED);
                    else
                        distanceTextView.setTextColor(Color.GREEN);
                    distanceTextView.setText(recievedJSON.getString("distance") + " cm");
                    if (!isRobotConnected) Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    isRobotConnected = true;
                    robotStatus_TextView.setText("Connected.");
                    robotStatus_TextView.setTextColor(Color.GREEN);
                }
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }


    private void connectToBroker() {
        Random r = new Random();        //Unique Client ID for connection
        int i1 = r.nextInt(5000 - 1) + 1;
        clientid = "mqtt" + i1;
        //clientid = "wificarcontrol";


        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getContext(),                        // Connect to MQTT Broker
                urlBroker,
                clientid,
                username,
                password
        );


        if (pahoMqttClient.mqttAndroidClient.isConnected()) {
            //Disconnect and Reconnect to  Broker
            try {
                //Disconnect from Broker
                pahoMqttClient.disconnect(client);
                //Connect to Broker
                client = pahoMqttClient.getMqttClient(getContext(), urlBroker, clientid, username, password);
                //Set Mqtt Message Callback
                mqttCallback();
            } catch (MqttException e) {
            }
        } else {
            //Connect to Broker
            client = pahoMqttClient.getMqttClient(getContext(), urlBroker, clientid, username, password);
            //Set Mqtt Message Callback
            mqttCallback();
        }

    }

    private void verifyConnection() {
        handler = new Handler();

        final Runnable a = new Runnable() {
            public void run() {
                //Check MQTT Connection Status
                String msg_new = "";

                if (pahoMqttClient.mqttAndroidClient.isConnected()) {
                    msg_new = "Connected.";
                    brokerStatus_TextView.setTextColor(Color.GREEN); //Green if connected
                    brokerStatus_TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    if (!isSubscribed) {
                        subscribeTopicHandler(subscribeTopic);
                        subscribeTopicHandler(camSubscribeTopic);
                        isSubscribed = true;
                    }
                } else {
                    msg_new = "Disconnected.";
                    brokerStatus_TextView.setTextColor(Color.RED); //Red if not connected
                    brokerStatus_TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    robotStatus_TextView.setTextColor(Color.RED);
                    robotStatus_TextView.setText(msg_new);
                    isSubscribed = false;
                }
                brokerStatus_TextView.setText(msg_new);
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(a, 1000);
    }

    private void subscribeTopicHandler(String topic) {
        if (!pahoMqttClient.mqttAndroidClient.isConnected()) {
            msg_new = "Currently not connected to MQTT broker: Must be connected to subscribe to a topic\r\n";
            Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            //tvMessage.append(msg_new);
            return;
        }
        //String topic = subscribeTopic.trim();
        if (!topic.isEmpty()) {
            try {
                pahoMqttClient.subscribe(client, topic.trim(), 1);
                // msg_new = "Added subscription topic: " + topic.trim() + "\r\n";
                //tvMessage.append(msg_new);
                // Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                //  .setAction("Action", null).show();

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void publishTopicHandler(String topic, String publishMessage) {
        //Check if connected to broker
        if (!pahoMqttClient.mqttAndroidClient.isConnected()) {
            msg_new = "Currently not connected to MQTT broker: Must be connected to publish message to a topic\r\n";
            Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        //Publish non-blank message
        //String pubtopic = etPubTopic.getText().toString().trim();
        //String msg      = etPubMsg.getText().toString().trim();
        if (!publishMessage.isEmpty()) {
            try {
                pahoMqttClient.publishMessage(client, publishMessage, 1, topic);
                //msg_new = "Message sent to pub topic: " + publishTopic + "\r\n";
                //Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                //.setAction("Action", null).show();
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    private void joystickInterpreter(int angle, int strength) {
        speedMotorA = strength * 1023 * maxSpeedMOTOR_A / 10000;
        speedMotorB = strength * 1023 * maxSpeedMOTOR_B / 10000;

        if (angle >= 60 && angle <= 120) {
            movement = "forward";
            moveNumber = 1;
        } else if (angle >= 240 && angle <= 300) {
            movement = "backward";
            moveNumber = 2;
        } else if (angle > 20 && angle < 60) {
            moveNumber = 3;
            movement = "forward";
            speedMotorA = (int) (curveAngle * speedMotorA);
            //speedMotorA = (int) ((-0.14*angle+1.28)*speedMotorA);
            //speedMotorB = (int) (0.75*speedMotorB);

        } else if (angle > 300 && angle < 339) {
            moveNumber = 4;
            movement = "backward";
            speedMotorA = (int) (curveAngle * speedMotorA);
            // speedMotorA = (int) ((0.0142857142*angle-3.8428571428)*speedMotorA);
            // speedMotorB = (int) (0.75*speedMotorB);

        } else if (angle > 120 && angle <= 160) {
            moveNumber = 5;
            movement = "forward";
            speedMotorB = (int) (curveAngle * speedMotorB);
            //speedMotorB = (int) ((0.014*angle-1.24)*speedMotorB);
            //speedMotorA = (int) (0.75*speedMotorA);

        } else if (angle > 215 && angle < 240) {
            moveNumber = 6;
            movement = "backward";
            speedMotorB = (int) (curveAngle * speedMotorB);
            // speedMotorB = (int) ((-0.02*angle+5.3)*speedMotorB);
            //speedMotorA = (int) (0.75*speedMotorA);
        } else if (angle > 300 && angle < 339 || angle > 0 && angle < 20) {
            moveNumber = 7;
            movement = "right";
        } else if (angle > 160 && angle < 215) {
            moveNumber = 8;
            movement = "left";
        } else if (angle == 0 || angle > 359) {
            moveNumber = 9;
            movement = "neutral";
        }


        return;

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {

        joyStickView.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                //publishTopicHandler(" Direction: Angle: " + angle + " Strength: " + strength);
                // Log.d("Joystick Direction:", " angulo: " + angle + " força: " + strength);
                joystickInterpreter(angle, strength);

            }
        });



       /* lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (light_state) {
                    light_state = false;
                    publishTopicHandler(camPublishTopic, "OFF");
                    lightButton.setImageDrawable(getResources().getDrawable(R.mipmap.lightbutton));
                } else {
                    light_state = true;
                    publishTopicHandler(camPublishTopic, "ON");
                    lightButton.setImageDrawable(getResources().getDrawable(R.mipmap.lightbutton_clicked));
                }
            }
        });*/

       lightButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        lightButton.setImageDrawable(getResources().getDrawable(R.mipmap.lightbutton_clicked));
                        if (pressedUp_light == false) {
                            pressedUp_light= true;
                            new ButtonAsyncTask().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        pressedUp_light = false;
                        lightButton.setImageDrawable(getResources().getDrawable(R.mipmap.lightbutton));
                        if (isAClick(startX, endX, startY, endY)) {
                            // Snackbar.make(view, "Released!", Snackbar.LENGTH_LONG)
                            //  .setAction("Action", null).show();
                            //Log.d("Accelerate Button:", " Released!");
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



        turnCamRightButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        turnCamRightButton.setImageDrawable(getResources().getDrawable(R.mipmap.rightbutton_clicked));
                        if (pressedUp_turnCamRight == false) {
                            pressedUp_turnCamRight = true;
                            new ButtonAsyncTask().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        pressedUp_turnCamRight = false;
                        turnCamRightButton.setImageDrawable(getResources().getDrawable(R.mipmap.rightbutton));
                        if (isAClick(startX, endX, startY, endY)) {
                            // Snackbar.make(view, "Released!", Snackbar.LENGTH_LONG)
                            //  .setAction("Action", null).show();
                            //Log.d("Accelerate Button:", " Released!");
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

        turnCamLeftButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        turnCamLeftButton.setImageDrawable(getResources().getDrawable(R.mipmap.leftbutton_clicked));
                        if (pressedUp_turnCamLeft == false) {
                            pressedUp_turnCamLeft = true;
                            new ButtonAsyncTask().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        pressedUp_turnCamLeft = false;
                        turnCamLeftButton.setImageDrawable(getResources().getDrawable(R.mipmap.leftbutton));
                        if (isAClick(startX, endX, startY, endY)) {
                            // Snackbar.make(view, "Released!", Snackbar.LENGTH_LONG)
                            //  .setAction("Action", null).show();
                            //Log.d("Accelerate Button:", " Released!");
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

        centerCamButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        centerCamButton.setImageDrawable(getResources().getDrawable(R.mipmap.centerbutton_clicked));
                        if (pressedUp_camCenter == false) {
                            pressedUp_camCenter = true;
                            new ButtonAsyncTask().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        pressedUp_camCenter = false;
                        centerCamButton.setImageDrawable(getResources().getDrawable(R.mipmap.centerbutton));
                        if (isAClick(startX, endX, startY, endY)) {
                            // Snackbar.make(view, "Released!", Snackbar.LENGTH_LONG)
                            //  .setAction("Action", null).show();
                            //Log.d("Accelerate Button:", " Released!");
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


        accelerateButton.setOnTouchListener(new View.OnTouchListener() {

            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();

                        if (pressedUp == false) {
                            pressedUp = true;
                            accelerateButton.setImageDrawable(getResources().getDrawable(R.mipmap.acceleratebutton_clicked));
                            new ButtonAsyncTask().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        pressedUp = false;
                        accelerateButton.setImageDrawable(getResources().getDrawable(R.mipmap.acceleratebutton));
                        if (isAClick(startX, endX, startY, endY)) {
                            //Snackbar.make(view, "Released!", Snackbar.LENGTH_LONG)
                            // .setAction("Action", null).show();
                            // Log.d("Accelerate Button:", " Released!");
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
        brakeButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        brakeButton.setImageDrawable(getResources().getDrawable(R.mipmap.brakebutton_clicked));
                        if (pressedUp_brake == false) {
                            pressedUp_brake = true;
                            moveNumber = 10;
                            new ButtonAsyncTask().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        pressedUp_brake = false;
                        brakeButton.setImageDrawable(getResources().getDrawable(R.mipmap.brakebutton));
                        if (isAClick(startX, endX, startY, endY)) {
                            // Snackbar.make(view, "Released!", Snackbar.LENGTH_LONG)
                            //  .setAction("Action", null).show();
                            //Log.d("Accelerate Button:", " Released!");
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

    }

    public JSONObject movementJSONCreate() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("move", movement);
            obj.put("pwma", speedMotorA);
            obj.put("pwmb", speedMotorB);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return obj;
    }

    class ButtonAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            while (pressedUp) {
                int compare = Math.abs(speedMotorA - pastspeedMotorA);
                //if (movement != pastMovement || compare > 30) {
                if (moveNumber != pastmoveNumber || compare > 275) {

                    publishTopicHandler(publishTopic, movementJSONCreate().toString());
                    pastspeedMotorA = speedMotorA;
                    //pastMovement = movement;
                    pastmoveNumber = moveNumber;

                }
                //Snackbar.make(view, "move=" + movement + " speedA=" + speedMotorA + " speedB=" + speedMotorB, Snackbar.LENGTH_LONG)
                //.setAction("Action", null).show();

            }
            while (pressedUp_brake) {
                movement = "brake";
                pastmoveNumber = moveNumber;
                speedMotorA = 1023;
                speedMotorB = 1023;
                publishTopicHandler(publishTopic, movementJSONCreate().toString());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (pressedUp_turnCamLeft) {
                if (camPosition < 180) camPosition += 10;
                else camPosition = 180;
                publishTopicHandler(camPublishTopic, String.valueOf(camPosition));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (pressedUp_turnCamRight) {
                if (camPosition > 0) camPosition -= 10;
                else camPosition = 0;
                publishTopicHandler(camPublishTopic, String.valueOf(camPosition));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (pressedUp_camCenter) {
                camPosition = 90;
                publishTopicHandler(camPublishTopic, String.valueOf(camPosition));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while(pressedUp_light) {
                if(light_state){
                    publishTopicHandler(camPublishTopic, "OFF");
                    light_state = false;
                } else{
                    light_state = true;
                    publishTopicHandler(camPublishTopic, "ON");
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}