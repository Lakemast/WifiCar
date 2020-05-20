package com.example.wificar.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wificar.mqtt.PahoMqttClient;
import com.example.wificar.R;
import com.google.android.material.snackbar.Snackbar;

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
    private TextView batteryTextView, obstacleTextView, distanceTextView, brokerStatus_TextView, robotStatus_TextView;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;
    private String clientid = "", urlBroker = "", username = "", password = "", subscribeTopic = "", publishTopic = "", msg_new;
    private Handler handler;
    private View view;
    private ImageButton accelerateButton, brakeButton;
    private boolean isSubscribed = false, isRobotConnected = false, pressedUp = false,pressedUp_brake=false;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private final static String PREF_BROKER = "PREF_BROKER", PREF_USERNAME = "PREF_USERNAME", PREF_PASSWORD = "PREF_PASSWORD",
            PREF_SUBSCRIBE = "PREF_SUBSCRIBE", PREF_PUBLISH = "PREF_PUBLISH",
            PREF_MAX_SPEED_MOTOR_A = "PREF_MAX_SPEED_MOTOR_A", PREF_MAX_SPEED_MOTOR_B = "PREF_MAX_SPEED_MOTOR_B";
    private String movement = "neutral", pastMovement ="neutral";
    private int speedMotorA = 0, speedMotorB = 0, maxSpeedMOTOR_A = 0, maxSpeedMOTOR_B = 0, pastspeedMotorA = 0, pastspeedMotorB = 0;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        joyStickView = root.findViewById(R.id.joystickViewDirection);
        batteryTextView = root.findViewById(R.id.battery_textView);
        obstacleTextView = root.findViewById(R.id.obstacle_textView);
        distanceTextView = root.findViewById(R.id.distance_textView);
        brokerStatus_TextView = root.findViewById(R.id.status_textView);
        robotStatus_TextView = root.findViewById(R.id.status_textView4);
        accelerateButton = root.findViewById(R.id.accelerateButton);
        brakeButton = root.findViewById(R.id.brakeButton);

        view = getActivity().findViewById(android.R.id.content);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        urlBroker = sharedPreferences.getString(PREF_BROKER, "tcp://test.mosquitto.org:1883");
        username = sharedPreferences.getString(PREF_USERNAME, "");
        password = sharedPreferences.getString(PREF_PASSWORD, "");
        subscribeTopic = sharedPreferences.getString(PREF_SUBSCRIBE, "wificar/status");
        publishTopic = sharedPreferences.getString(PREF_PUBLISH, "wificar/control");

        maxSpeedMOTOR_A = Integer.valueOf(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_A, "255"));
        maxSpeedMOTOR_B = Integer.valueOf(sharedPreferences.getString(PREF_MAX_SPEED_MOTOR_B, "255"));

        setListener();

        connectToBroker();
        subscribeTopicHandler();

        // Called when a subscribed message is received

        mqttCallback();


        verifyConnection();


        return root;
    }

    protected void mqttCallback() {

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                //msg("Connection lost...");
                robotStatus_TextView.setTextColor(getResources().getColor(R.color.colorDisConnected));
                robotStatus_TextView.setText("Disconnected.");
                robotStatus_TextView.setTextColor(getResources().getColor(R.color.colorDisConnected));
                isRobotConnected = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals("mycustomtopic1")) {
                    //Add custom message handling here (if topic = "mycustomtopic1")
                } else if (topic.equals("mycustomtopic2")) {
                    //Add custom message handling here (if topic = "mycustomtopic2")
                } else {

                    String msg = "Robot connected!";

                    JSONObject recievedJSON = new JSONObject(message.toString());
                    // get employee name and salary
                    double battery_double = Double.valueOf(recievedJSON.getString("voltage"));
                    battery_double = (((battery_double - 5.8) / 1.7) * 100);
                    battery_double = (double) Math.round(battery_double * 100) / 100;
                    if (battery_double < 0) battery_double = 0;
                    if (battery_double > 100) battery_double = 100;
                    batteryTextView.setText(battery_double + "%");
                    if (battery_double < 35) batteryTextView.setTextColor(getResources().getColor(R.color.colorDisConnected));
                    else batteryTextView.setTextColor(getResources().getColor(R.color.colorConnected));
                    obstacleTextView.setText(recievedJSON.getString("obstacle"));
                    int distance = Integer.valueOf(recievedJSON.getString("distance"));
                    if(distance<16) distanceTextView.setTextColor(getResources().getColor(R.color.colorDisConnected));
                    else distanceTextView.setTextColor(getResources().getColor(R.color.colorConnected));
                    distanceTextView.setText(recievedJSON.getString("distance") + " cm");
                    if(!isRobotConnected) Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    isRobotConnected = true;
                    robotStatus_TextView.setText("Connected.");
                    robotStatus_TextView.setTextColor(getResources().getColor(R.color.colorConnected));
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
                        subscribeTopicHandler();
                        isSubscribed = true;
                    }
                } else {
                    msg_new = "Disconnected.";
                    brokerStatus_TextView.setTextColor(0xFFFF0000); //Red if not connected
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

    private void subscribeTopicHandler() {
        if (!pahoMqttClient.mqttAndroidClient.isConnected()) {
            msg_new = "Currently not connected to MQTT broker: Must be connected to subscribe to a topic\r\n";
            Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            //tvMessage.append(msg_new);
            return;
        }
        String topic = subscribeTopic.trim();
        if (!topic.isEmpty()) {
            try {
                pahoMqttClient.subscribe(client, topic, 1);
                msg_new = "Added subscription topic: " + subscribeTopic + "\r\n";
                //tvMessage.append(msg_new);
                Snackbar.make(view, msg_new, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void publishTopicHandler(String publishMessage) {
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
                pahoMqttClient.publishMessage(client, publishMessage, 1, publishTopic);
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

        if (angle >= 70 && angle <= 110) movement = "forward";
        else if (angle >= 250 && angle <= 290) movement = "backward";
        else if (angle > 0 && angle < 70 || angle > 290 && angle < 359) movement = "right";
        else if (angle > 110 && angle <= 180 || angle > 195 && angle < 250) movement = "left";
        else if (angle == 0 || angle > 359) movement = "neutral";
        speedMotorA = strength * 1023 * maxSpeedMOTOR_A /10000;
        speedMotorB = strength * 1023 * maxSpeedMOTOR_B /10000;
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

                        if(pressedUp == false){
                            pressedUp = true;
                            accelerateButton.setImageDrawable(getResources().getDrawable(R.mipmap.accelerateclickedbutton));
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
                        brakeButton.setImageDrawable(getResources().getDrawable(R.mipmap.brakeclikedbutton));
                        if(pressedUp_brake == false){
                            pressedUp_brake = true;
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
    public JSONObject movementJSONCreate(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("move",movement );
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
            while(pressedUp) {
                int compare = Math.abs(speedMotorA-pastspeedMotorA);
                if( movement != pastMovement  || compare > 30 ) {

                    publishTopicHandler(movementJSONCreate().toString());
                    pastspeedMotorA = speedMotorA;
                    pastMovement = movement;

                }
                //Snackbar.make(view, "move=" + movement + " speedA=" + speedMotorA + " speedB=" + speedMotorB, Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();

            }
            while (pressedUp_brake) {
                movement = "brake";
                speedMotorA = 255;
                speedMotorB = 255;
                publishTopicHandler(movementJSONCreate().toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}