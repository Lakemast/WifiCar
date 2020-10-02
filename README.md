# WiFiCar

This is a full implementation of a RC Car over MQTT using:
- ESP8266 (NodeMCU v1.0)
- ESP32CAM
- Android App

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
This is the PCB schematic, you can assemble it on breadboard too. I designed the PCB layout on Altium and all the files are in the [WiFiCar-PCB folder](WifiCar-PCB). Here are the [PCB bottom layer and top overlay](WifiCar-PCB/WifiCar-PCB-TRANSFER) to make your PCB (*Check out if the hardware dimensions before making the PCB because pdf files may cause some distortions*).

<img src="/README/wificar-schematic_revised.PNG" height="75%" width="75%"> (PCB Schematic)

**Notice that you must connect H Brigde pins accordingly to IN1,IN2,IN3,IN4,ENA,ENB in the schematic.**

<img src="https://i.pinimg.com/564x/ae/a5/00/aea500ba31f99047ac2ef4dfd375453d.jpg" height="40%" width="40%"> (L298N H Bridge driver pinout)

VU must be connected to 5V output of H Brigde. Connect the GND of the H Brigde and PCB to battery GND. Now connect the positive pole of your battery simultaneously to 12V of H Bridge and to VBAT+ (*See Diagram below*).

<img src="/README/ConnectionSchemt.png" height="40%" width="40%">

### Using a battery over 7.4V and under 25V

If you will use a battery over 7.4V you will need to change the resistors R3 and R4. [Look the PCB Schematic](/README/wificar-schematic_revised.PNG)
To calculate their values you will need to use the voltage divider formula to provide the analago input A0 of NodeMCU 1V when the batterry is at full charge. 
[Take a look at this voltage divider calculator](https://ohmslawcalculator.com/voltage-divider-calculator) or you can calcultate yourself, here it is the formula: 

<img src="/README/voltagedivider.PNG" height="35%" width="35%">

where is:

* VA0 is the voltage at A0 analog input of NodeMCU. This voltage must be 1V.
* VBAT+ is the voltage of your battery at full charge. Must be under 25V to not damage 7805 voltage regulator at PCB.
* R3 and R4 are the resistors in the PCB Schematic. Choose a value to R4 then calculate R3 value. Remember there is no problem using a resistor with a approximate resistance value.

### Uploading the code to NodeMCU

(Explicar como fazer upload do código e quais dependências instalar!)

### Uploading the code to ESP32CAM

(Explicar como fazer upload do código e quais dependências instalar!)


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

There are two ways **to change MQTT Topics in NodeMCU and ESP32CAM**. It can be done changing source code or you can change via HTTP web page.
The only way to change topics permantly is via source code, if you change it using the HTTP web page when you reboot your robot the topics will be reset to the source code default.

## Repositories I used

Thanks to these repositories I was able to do it.

- [esp8266/Arduino](https://github.com/esp8266/Arduino)
- [tzapu/WiFiManager](https://github.com/tzapu/WiFiManager)
- [perthcpe23/android-mjpeg-view](https://github.com/perthcpe23/android-mjpeg-view)
- [controlwear/virtual-joystick-android](https://github.com/controlwear/virtual-joystick-android)
- [internetofhomethings/MQTT-Android-Demo-App](https://github.com/internetofhomethings/MQTT-Android-Demo-App)
- [fatum2996/altium-library](https://github.com/fatum2996/altium-library)

## License
WiFiCar by Lakemast is licensed under the [Creative Commons - Attribution - Non-Commercial - Share Alikelicense](https://creativecommons.org/licenses/by-nc-sa/4.0/).
