# WiFiCar

Now you will be able to build and control your own RC Car over the Internet using the Message Queue Telemetry Transport Protocol (MQTT) with an easy and intuitive control interface on WAN (Wide Area Network) and LAN (Local Area Network) (*but the camera stream may require some workaround*) using any two motors robot chassis.

[Click here to see WiFiCar Video on YouTube](https://youtu.be/1mkALsdsVI4)


<img src="/README/wificar.gif" width="70%" height="45%">

<img src="/README/WifiCar.png" width="70%" height="35%">

## Materials

* 1x NodeMCU v1.0 ESP 12 E
* 1x ESP32CAM
* 1x LM7805
* 1x Micro Servo 9g SG90
* 1x Ultrasonic Sensor HC-SR04
* 1x H Brigde L298N
* 1x 7.4V Battery (*I used a LiPo Battery, but you can also use 2x 18650 3.7V or a maximum battery voltage of 25V, see the section **Using a battery over 7.4V and under 25V** before doing your PCB*)
* 1x Robot chassis with 2 motors
* 3x 0.100" (2.54 mm) Breakaway Male Header: 1×40-Pin
* 3x  0.100" (2.54 mm) Breakaway Female Header: 1×40-Pin
* 1x Screw terminal block 3 pins (KF301-3P)
* 1x LED 5mm (*any color*)
* Some Female x Male Jumpers
* Some AWG 22 Cables (*Optional*)

* Resistors:
  * 1x 550 Ohms
  * 1x 4K7 Ohms
  * 1x 10K Ohms
  * 1x 22K Ohms
  * 1X 3K3 Ohms
* Capacitors:
  * 1x 330 nF 25V
  * 1x 100 nF 25V
  
## Assembly
This is the PCB schematic, you can assemble it on breadboard too. I designed the PCB layout and all the files are in the [WiFiCar-PCB folder](WifiCar-PCB). Here are the [PCB bottom layer and top overlay](WifiCar-PCB/WifiCar-PCB-TRANSFER) to make your PCB (*Check out if the hardware dimensions before making the PCB because pdf files may cause some distortions*).

<img src="/README/wificar-schematic_revised.PNG" height="75%" width="75%"> (PCB Schematic)

**Notice that you must connect H Brigde pins accordingly to IN1,IN2,IN3,IN4,ENA,ENB in the schematic.**

<img src="https://i.pinimg.com/564x/ae/a5/00/aea500ba31f99047ac2ef4dfd375453d.jpg" height="40%" width="40%"> [(L298N H Bridge driver pinout *source: Eletronics Hub*)](https://www.electronicshub.org/arduino-dc-motor-control-using-l298n/)

VU must be connected to 5V output of H Brigde. Connect the GND of the H Brigde and PCB to battery GND. Now connect the positive pole of your battery simultaneously to 12V of H Bridge and to VBAT+ (*See Diagram below*).

<img src="/README/ConnectionSchemt.png" height="40%" width="40%">

### Using a battery over 7.4V and under 25V

If you will use a battery over 7.4V, you will need to change the resistors R3 and R4. [Look the PCB Schematic](/README/wificar-schematic_revised.PNG)
To calculate their values you will need to use the voltage divider formula to provide the analago input A0 of NodeMCU 1V when the batterry is at full charge. 
[Take a look at this voltage divider calculator](https://ohmslawcalculator.com/voltage-divider-calculator) or you can calcultate yourself, here it is the formula: 

<img src="/README/voltagedivider.PNG" height="35%" width="35%">

where:

* VA0 is the voltage at A0 analog input of NodeMCU. This voltage must be 1V.
* VBAT+ is the voltage of your battery at full charge. Must be under 25V to not damage 7805 voltage regulator at PCB.
* R3 and R4 are the resistors in the PCB Schematic. Choose a value to R4 then calculate R3 value. Remember there is no problem using a resistor with a approximate resistance value.

### Uploading the code to NodeMCU

[Follow this to upload the code to your NodeMCU](https://randomnerdtutorials.com/how-to-install-esp8266-board-arduino-ide/). Remember you will upload the code on the [WiFiCarESP folder](/WiFiCarESP).

### Uploading the code to ESP32CAM

[Take a look at this if you dont know how to upload the code to ESP32CAM](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/). Follow the steps: 1. Install the ESP32 add-on and 3. ESP32-CAM Upload Code of this tutorial. Instead uploading the example code you will compile and upload the code on [WiFiCamCar folder](/WiFiCamCar).


## Android App

You can install the android app downloading this [WiFiCar Android App](WiFiCarMQTT/WiFiCar.apk) or do it from Android Studio. 

**Remember you need to activate developer mode in your android smartphone!**

## Configuring WiFiCar APP

First configure your WiFiCar APP. You may change MQTT topics, if you want. It's highly recommended change theses MQTT topics when using Public Broker to avoid different applications using the same topic. **When changing topic, you must change the topics in NodeMCU and ESP32CAM, Don't forget that!**

<img src="/README/WiFiCar-settings.jpg" height="75%" width="75%">

- **MQTT Broker:** put here your Broker's URL (*[See some Public Brokers here if you dont have your own](https://github.com/mqtt/mqtt.github.io/wiki/public_brokers)*).

- **Username and Password:** fill it if your broker has authentication. If you are using a public broker leave it blank.

- **Publish Topic:** MQTT topic which WiFiCar App will post commands to controll the WiFiCar Robot.

- **Subscribe Topic:** MQTT topic which App will receive data from WiFiCar Robot.

- **Speed Left/Right Motor**: used to calibrate straight forward robot movement.

- **Camera URL**: put here your ESP32CAM URL (*remember to add ":81/stream" after the IP address*).

- **Camera Publish Topic:** MQTT topic which WiFiCar App will post commands to ESP32CAM and the servor motor.

- **Camera Subscribe Topic:** MQTT topic which WiFiCar App will receive data from WiFiCar ESP32CAM.

## Configuring WiFiCar Robot

There are two ways **to change MQTT Broker URL and MQTT Topics in NodeMCU and ESP32CAM**. It can be done changing source code or you can change via HTTP web page.


Search for this code in [WiFiCarESP.ino](WifiCarESP/WifiCarESP.ino) to change ESP8266 MQTT Broker URL and Topics. (**THIS IS OPTIONAL, DO IT ONLY IF YOU NEED**)

```C

//custom parameters
WiFiManagerParameter custom_mqtt_server("server", "MQTT Broker", "broker.hivemq.com", 40, " readonly");
WiFiManagerParameter custom_subscribe_topic("subscribeTopic", "Control Topic", "wificar/control", 40, " readonly");
WiFiManagerParameter custom_publish_topic("publishTopic", "Status Topic", "wificar/status", 40, " readonly");
WiFiManagerParameter custom_subscribe_topic_cam("subscribeTopicCam", " Control Camera Topic", "wificar/cam/control", 40, " readonly");

```
Now you will change these values:

- Replace **broker.hivemq.com** with your broker URL.
- Replace **wificar/control** with the chosen topic to control the robot movement.
- Replace **wificar/status** with the chosen topic to receive robot status messages.
- Replace **wificar/cam/control** with the chosen topic to rotate camera. **This topic must be the same at ESP32CAM**


Search for this code in [WiFiCamCar.ino](WiFiCamCar/WiFiCamCar.ino) to change ESP32CAM MQTT Broker URL and Topics. (**THIS IS OPTIONAL, DO IT ONLY IF YOU NEED**)

```C

WiFiManagerParameter custom_mqtt_server("server", "MQTT Broker", "broker.hivemq.com", 40, " readonly");
WiFiManagerParameter custom_led_subscribe_topic("subscribeTopic", "Subscribe Topic", "wificar/cam/control", 40, " readonly");
WiFiManagerParameter custom_publish_topic("publishTopic", "Publish Topic", "wificar/cam/status", 40, " readonly");

```

Now you will change these values:

- Replace **broker.hivemq.com** with your broker URL.
- Replace **wificar/cam/control** with the chosen topic to control ESP32CAM LED. **This topic must be the same at ESP8266**
- Replace **wificar/cam/status** with the topic chosen to receive the ESP32CAM LED status.

## Repositories I used

Thanks to these repositories I achieved it.

- [esp8266/Arduino](https://github.com/esp8266/Arduino)
- [tzapu/WiFiManager](https://github.com/tzapu/WiFiManager)
- [perthcpe23/android-mjpeg-view](https://github.com/perthcpe23/android-mjpeg-view)
- [controlwear/virtual-joystick-android](https://github.com/controlwear/virtual-joystick-android)
- [internetofhomethings/MQTT-Android-Demo-App](https://github.com/internetofhomethings/MQTT-Android-Demo-App)
- [fatum2996/altium-library](https://github.com/fatum2996/altium-library)

## License
WiFiCar by Lakemast is licensed under the [Creative Commons - Attribution - Non-Commercial - Share Alikelicense](https://creativecommons.org/licenses/by-nc-sa/4.0/).
