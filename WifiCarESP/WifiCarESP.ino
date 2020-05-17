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

// Configuração do acesso ao Broker MQTT
#define MQTT_AUTH false
#define MQTT_USERNAME "pacote"
#define MQTT_PASSWORD "l4kem4st"

//Variáveis da mensagem do publisher.
#define MSG_BUFFER_SIZE  (50)

//Constantes
const String HOSTNAME  = "WifiCar"; //NOME DO DEVICE, este nome tambem é utilizado apra criar o Access Point para configuração
const char * MQTT_LOG = "wificar/log"; // Topico onde o Device Publica informações relacionadas com o sistema
const char * MQTT_SYSTEM_CONTROL_TOPIC = "wificar/set"; // Topico onde o Device subscreve para aceitar instruções de sistema
const char * MQTT_PUBLISHER_TOPIC = "wificar/status";
const char * MQTT_TEST_TOPIC = "wificar/control"; //Topico de exemplo onde o Device subscreve (por exemplo controlar uma lâmpada)


//MQTT BROKERS GRATUITOS PARA TESTES https://github.com/mqtt/mqtt.github.io/wiki/public_brokers
const char* MQTT_SERVER = "test.mosquitto.org"; //IP ou DNS do Broker MQTT
const char* host = "WifiCar";
const char* update_path = "/firmware";
const char* update_username = "admin";
const char* update_password = "admin";

//Variáveis da mensagem do publisher.
#define MSG_BUFFER_SIZE  (100)
char msg[MSG_BUFFER_SIZE];
unsigned long lastMsg = 0;

int pwmA = 0, pwmB = 0;
int distance = 0;
int obstacle_right = 0, obstacle_left = 0, obstacles_scan = 0;;

String moveStr_Pub, obstacleDetected;

int pos = 0;
boolean right = true;

//FLAGS de Controlo
bool OTA = false; //O Serviço OTA é muito pesado para estar sempre ativo por isso é ligado via MQTT e fica disponivel até ser desligado ou até o device ser reiniciado
bool OTABegin = false;


ESP8266WebServer httpServer(80);
ESP8266HTTPUpdateServer httpUpdater;
WiFiClient wclient;
PubSubClient client(MQTT_SERVER, 1883, wclient);
Ultrasonic ultrasonic(TRIGGER, ECHO); // Instância chamada ultrasonic com parâmetros (trig,echo)
WiFiManager wifiManager;
Servo ultrasonicServo;
ThreadController cpu;
Thread detectObstacles_Thread;
Thread moveServo_Thread;


void setup() {
  Serial.begin(9600); // Inicio da comunicação serial

  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(ENA, OUTPUT);
  pinMode(ENB, OUTPUT);
  ultrasonicServo.attach(D8);

  //Configuração da Thread de verificação do estado do dispositivo
  detectObstacles_Thread.setInterval(50);
  detectObstacles_Thread.onRun(detectObstacles);
  moveServo_Thread.setInterval(57);
  moveServo_Thread.onRun(moveServo);

  //Configuração do ThreadController
  cpu.add(&detectObstacles_Thread);
  cpu.add(&moveServo_Thread);

  //wifiManager.resetSettings(); //Limpa a configuração anterior do Wi-Fi SSID e Password, procedimento, 1º descomentar a linha, 2º Fazer Upload do código para o ESP e deixar o ESP arrancar, 3º Voltar a comentar a linha e enviar novamente o código para o ESP
  /*define o tempo limite até o portal de configuração ficar novamente inátivo,
    útil para quando alteramos a password do AP*/
  wifiManager.setTimeout(180);
  wifiManager.autoConnect(HOSTNAME.c_str());
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
    if (now - lastMsg > 250) {
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
