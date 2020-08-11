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
This is the PCB schematic, you can assemble it on breadboard too.

**Notice that you must connect H Brigde pins accordingly to IN1,IN2,IN3,IN4,ENA,ENB in the schematic** 
<img src="/README/wificar-schematic.PNG" height="75%" width="75%">

### Uploading the code to NodeMCU

(Explicar como fazer upload do código e quais dependências instalar!)

### Uploading the code to ESP32CAM

(Explicar como fazer upload do código e quais dependências instalar!)


## Android App

You can install the android app downloading this [WiFiCar Android App](WiFiCarMQTT/WiFiCar.apk) or do it from Android Studio. 

### Configuring WiFiCar APP

(Explicar como configurar)

<img src="/README/WiFiCar-settings.jpg" height="75%" width="75%">

## License

(IMPORTANTE Creative Commons - Ver qual a melhor!)
