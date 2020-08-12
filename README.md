# WiFiCar

This is a full implementation of a RC Car over MQTT using:
- ESP8266 (NodeMCU v1.0)
- ESP32CAM
- Android App

Take a look how it works.

<img src="/README/WifiCar.png" width="70%" height="45%">

(VIDEO DO PROJETO!)

## Materials
* 1x NodeMCU v1.0 ESP 12 E
* 1x ESP32CAM
* 1x LM7805
* 1x Micro Servo 9g SG90
* 1x Ultrasonic Sensor HC-SR04
* 1x H Brigde L298N
* 1x 7.4V Battery (*I used a LiPo Battery, but you can also use 2x 18650 3.7V*)
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
This is the PCB schematic, you can assemble it on breadboard too. I designed the PCB layout on Altium and all the files are in the [WiFiCar-PCB folder](WifiCar-PCB). Here are the [PCB bottom layer and top overlay](WifiCar-PCB/WifiCar-PCB-TRANSFER) to make the your PCB (*Check out if the hardware dimensions before making the PCB because pdf files may cause some distortions*).

<img src="/README/wificar-schematic_revised.PNG" height="75%" width="75%"> (PCB Schematic)

**Notice that you must connect H Brigde pins accordingly to IN1,IN2,IN3,IN4,ENA,ENB in the schematic.**

<img src="https://i.pinimg.com/564x/ae/a5/00/aea500ba31f99047ac2ef4dfd375453d.jpg" height="40%" width="40%"> (L298N H Bridge driver pinout)

**VU must be connected to 5V output of H Brigde. Connect the GND of the H Brigde and PCB to battery GND. Now connect the positive pole of your battery simultaneously to 12V of H Bridge and to VBAT+ (*See Diagram below*)**.

<img src="/README/ConnectionSchemt.png" height="40%" width="40%">

### Uploading the code to NodeMCU

(Explicar como fazer upload do código e quais dependências instalar!)

### Uploading the code to ESP32CAM

(Explicar como fazer upload do código e quais dependências instalar!)


## Android App

You can install the android app downloading this [WiFiCar Android App](WiFiCarMQTT/WiFiCar.apk) or do it from Android Studio. 

**Remember you need to activate developer mode in your android smartphone!**

### Configuring WiFiCar APP and WiFiCar Robot

<img src="/README/WiFiCar-settings.jpg" height="75%" width="75%">

- **MQTT Broker:** put here your Broker's URL (*[See some Public Brokers here if you dont have your own](https://github.com/mqtt/mqtt.github.io/wiki/public_brokers)*)

- **Username and Password:** fill it if your broker has authentication, if you are using a public broker leave it blank.

- **Publish Topic:**

- **Subscribe Topic:**

- **Speed Left/Right Motor**: used to calibrate straight forward robot movement.

- **Camera URL**: put here your ESP32CAM URL (*remember to add ":81/stream" after the IP address*)

- **Camera Publish Topic:**

- **Camera Subscribe Topic:**


## License
WiFiCar by Lakemast is licensed under the [Creative Commons - Attribution - Non-Commercial - Share Alikelicense](https://creativecommons.org/licenses/by-nc-sa/4.0/).
