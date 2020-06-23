#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ESP8266HTTPUpdateServer.h>
#include <PubSubClient.h>
#include <WiFiManager.h>
#include <ArduinoJson.h>
#include <Ultrasonic.h> // Declaração de biblioteca
#include <Thread.h>
#include <ThreadController.h>
#include <Servo.h>
#include <EEPROM.h>
#include <Ticker.h>



#define IN1 D0
#define IN2 D1
#define IN3 D2
#define IN4 D3
#define TRIGGER D4
#define ECHO D5
#define ENA D6
#define ENB D7
#define SERVO D8
#define MIN_DISTANCE 15

#define ADDR_MQTT_URL 603
#define ADDR_PUBLISH_TOPIC 201
#define ADDR_SUBSCRIBE_TOPIC 402

// Configuração do acesso ao Broker MQTT
#define MQTT_AUTH false
#define MQTT_USERNAME "pacote"
#define MQTT_PASSWORD "l4kem4st"

//Variáveis da mensagem do publisher.
#define MSG_BUFFER_SIZE  (50)
#define MSG_DELAY 2500

//Constantes
const String HOSTNAME  = "WifiCar"; //NOME DO DEVICE, este nome tambem é utilizado apra criar o Access Point para configuração
const char * MQTT_LOG = "wificar/log"; // Topico onde o Device Publica informações relacionadas com o sistema
const char * MQTT_SYSTEM_CONTROL_TOPIC = "wificar/set"; // Topico onde o Device subscreve para aceitar instruções de sistema
const char * MQTT_PUBLISHER_TOPIC = "wificar/status";
const char * MQTT_TEST_TOPIC = "wificar/control"; //Topico de exemplo onde o Device subscreve (por exemplo controlar uma lâmpada)
const char * MQTT_CONTROL_CAMERA_SERVO = "wificar/cam/control";

//char * MQTT_PUBLISHER_TOPIC;
//char * MQTT_TEST_TOPIC;
//char* MQTT_SERVER;

//MQTT BROKERS GRATUITOS PARA TESTES https://github.com/mqtt/mqtt.github.io/wiki/public_brokers
const char* MQTT_SERVER = "broker.hivemq.com"; //IP ou DNS do Broker MQTT
const char* host = "WifiCar";
const char* update_path = "/firmware";
const char* update_username = "admin";
const char* update_password = "admin";


//Variáveis da mensagem do publisher.
#define MSG_BUFFER_SIZE  (100)
char msg[MSG_BUFFER_SIZE];
unsigned long lastMsg = 0;
long lastReconnectAttempt = 0;

int pwmA = 0, pwmB = 0;
int distance = 0;
int obstacle_right = 0, obstacle_left = 0, obstacles_scan = 0;;

String moveStr_Pub, obstacleDetected;

int pos = 0;
boolean right = true;

//FLAGS de Controlo
bool OTA = false; //O Serviço OTA é muito pesado para estar sempre ativo por isso é ligado via MQTT e fica disponivel até ser desligado ou até o device ser reiniciado
bool OTABegin = false;


ESP8266WebServer httpServer(81); // Servidor do OTA.
//ESP8266WebServer server(80); // Servidor para mudar url do broker e os topicos.
ESP8266HTTPUpdateServer httpUpdater;
WiFiClient wclient;
//PubSubClient client(MQTT_SERVER,1883,wclient);
PubSubClient client;
Ultrasonic ultrasonic(TRIGGER, ECHO); // Instância chamada ultrasonic com parâmetros (trig,echo)
WiFiManager wifiManager;
Servo ultrasonicServo;
ThreadController cpu;
Thread detectObstacles_Thread;


//custom parameters
WiFiManagerParameter custom_mqtt_server("server", "MQTT Broker", "broker.hivemq.com", 40, " readonly");
WiFiManagerParameter custom_subscribe_topic("subscribeTopic", "Control Topic", "wificar/control", 40, " readonly");
WiFiManagerParameter custom_publish_topic("publishTopic", "Status Topic", "wificar/status", 40, " readonly");
WiFiManagerParameter custom_subscribe_topic_cam("subscribeTopicCam", " Control Camera Topic", "wificar/cam/control", 40, " readonly");



Ticker ticker;
int LED = LED_BUILTIN;

void tick(){
  //toggle state
  digitalWrite(LED, !digitalRead(LED));     // set pin to the opposite state
}

//gets called when WiFiManager enters configuration mode
void configModeCallback (WiFiManager *myWiFiManager) {
  Serial.println("Entered config mode");
  Serial.println(WiFi.softAPIP());
  //if you used auto generated SSID, print it
  Serial.println(myWiFiManager->getConfigPortalSSID());
  //entered config mode, make led toggle faster
  ticker.attach(0.2, tick);
}


void setup() {
  //clearEeprom ();
  Serial.begin(9600); // Inicio da comunicação serial
  EEPROM.begin(4096);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(ENA, OUTPUT);
  pinMode(ENB, OUTPUT);
  ultrasonicServo.attach(D8);

  //set led pin as output
  pinMode(LED, OUTPUT);
  // start ticker with 0.5 because we start in AP mode and try to connect
  ticker.attach(0.6, tick);
  wifiManager.setCustomHeadElement("<style> body { padding: 35px; background-color: #333; font-family: Arial, Helvetica, Sans-Serif; Color: #fff; }\
  a{Color:#fff}</style>");


  //Serial.println(String(MQTT_SERVER));
  //Configuração da Thread de verificação do estado do dispositivo
  detectObstacles_Thread.setInterval(50);
  detectObstacles_Thread.onRun(detectObstacles);

  //Configuração do ThreadController
  cpu.add(&detectObstacles_Thread);


  //wifiManager.resetSettings(); //Limpa a configuração anterior do Wi-Fi SSID e Password, procedimento, 1º descomentar a linha, 2º Fazer Upload do código para o ESP e deixar o ESP arrancar, 3º Voltar a comentar a linha e enviar novamente o código para o ESP
  /*define o tempo limite até o portal de configuração ficar novamente inátivo,
    útil para quando alteramos a password do AP*/

  wifiManager.addParameter(&custom_mqtt_server);
  wifiManager.addParameter(&custom_subscribe_topic);
  wifiManager.addParameter(&custom_subscribe_topic_cam);
  wifiManager.addParameter(&custom_publish_topic);

  MQTT_SERVER =  custom_mqtt_server.getValue();
  MQTT_PUBLISHER_TOPIC = custom_publish_topic.getValue();
  MQTT_TEST_TOPIC = custom_subscribe_topic.getValue();
  MQTT_CONTROL_CAMERA_SERVO = custom_subscribe_topic_cam.getValue();
  
  //wifiManager.setTimeout(180);
  wifiManager.autoConnect(HOSTNAME.c_str());
  //reset settings - for testing
  //wifiManager.resetSettings();

  client.setServer(MQTT_SERVER, 1883);
  client.setClient(wclient);
  //Serial.println("HTTP server started");
  client.setCallback(callback); //Registo da função que vai responder ás mensagens vindos do MQTT

}

void loop() {

  cpu.run();
  if (WiFi.status() == WL_CONNECTED) {
    if (checkMqttConnection()) {
      client.loop();
      if (OTA) {
        if (OTABegin) {
          setupOTA();
          OTABegin = false;
        }
        //ArduinoOTA.handle();
        httpServer.handleClient();
      }
    }
    unsigned long now = millis();
    if (now - lastMsg > MSG_DELAY) {
      lastMsg = now;
      //++value;
      messageJSON();
      //snprintf (msg, MSG_BUFFER_SIZE, "hello world #%ld", value);
      //Serial.print("Publish message: ");
      //Serial.println(msg);
      client.publish(MQTT_PUBLISHER_TOPIC, msg);
    }
  }
}
