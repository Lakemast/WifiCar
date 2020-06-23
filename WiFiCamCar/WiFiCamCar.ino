#include "esp_camera.h"
#include <WiFi.h>
#include <WiFiManager.h> // https://github.com/tzapu/WiFiManager
//for LED status
#include <Ticker.h>
#include <PubSubClient.h>
#include <WiFiClient.h>
#include <WebServer.h>
#include <ESPmDNS.h>
#include <Update.h>
//
// WARNING!!! Make sure that you have either selected ESP32 Wrover Module,
//            or another board which has PSRAM enabled
//

// Select camera model
//#define CAMERA_MODEL_WROVER_KIT
//#define CAMERA_MODEL_ESP_EYE
//#define CAMERA_MODEL_M5STACK_PSRAM
//#define CAMERA_MODEL_M5STACK_WIDE
#define CAMERA_MODEL_AI_THINKER
#ifndef LED_BUILTIN
#define LED_BUILTIN 4 // ESP32 DOES NOT DEFINE LED_BUILTIN
#endif

#include "camera_pins.h"


#define ADDR_MQTT_URL 603
#define ADDR_PUBLISH_TOPIC 201
#define ADDR_SUBSCRIBE_TOPIC 402

// Configuração do acesso ao Broker MQTT
#define MQTT_AUTH false
#define MQTT_USERNAME "pacote"
#define MQTT_PASSWORD "l4kem4st"
#define MSG_DELAY 5771



Ticker ticker;
WiFiManager wm;
WebServer otaServer(9999);
WiFiClient wclient;
PubSubClient client;


const String HOSTNAME  = "WifiCarCam";
const char * MQTT_LOG = "wificarcam/log"; // Topico onde o Device Publica informações relacionadas com o sistema
const char * MQTT_SYSTEM_CONTROL_TOPIC = "wificarcam/set"; // Topico onde o Device subscreve para aceitar instruções de sistema
const char * MQTT_PUBLISHER_TOPIC = "wificar/cam/status";
const char * MQTT_TEST_TOPIC = "wificar/cam/control"; //Topico de exemplo onde o Device subscreve (por exemplo controlar uma lâmpada)
//MQTT BROKERS GRATUITOS PARA TESTES https://github.com/mqtt/mqtt.github.io/wiki/public_brokers
const char* MQTT_SERVER = "broker.hivemq.com"; //IP ou DNS do Broker MQTT

const char* host = "WifiCarCam";

//const char* ssid = "ARES";
//const char* password = "a552538lich";

//Variáveis da mensagem do publisher.
#define MSG_BUFFER_SIZE  (100)
char msg[MSG_BUFFER_SIZE];
unsigned long lastMsg = 0;
long lastReconnectAttempt = 0;

int LED = LED_BUILTIN;
//FLAGS de Controlo
bool OTA = false; //O Serviço OTA é muito pesado para estar sempre ativo por isso é ligado via MQTT e fica disponivel até ser desligado ou até o device ser reiniciado
bool OTABegin = false;


WiFiManagerParameter custom_mqtt_server("server", "MQTT Broker", "broker.hivemq.com", 40, " readonly");
WiFiManagerParameter custom_led_subscribe_topic("subscribeTopic", "Subscribe Topic", "wificar/cam/control", 40, " readonly");
WiFiManagerParameter custom_publish_topic("publishTopic", "Publish Topic", "wificar/cam/status", 40, " readonly");


void startCameraServer();

bool checkMqttConnection();

void tick();

void configModeCallback (WiFiManager *myWiFiManager);

void setupOTA();

void callback(char *topic, byte *payload, unsigned int length);

void verifyLED();

void setup() {
  WiFi.mode(WIFI_STA); // explicitly set mode, esp defaults to STA+AP
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println();

  pinMode(LED, OUTPUT);
  // start ticker with 0.5 because we start in AP mode and try to connect
  ticker.attach(0.6, tick);

  wm.setCustomHeadElement("<style> body { padding: 35px; background-color: #333; font-family: Arial, Helvetica, Sans-Serif; Color: #fff; }\
  a{Color:#fff}</style>");

  //reset settings - for testing
  //wm.resetSettings();



  wm.addParameter(&custom_mqtt_server);
  wm.addParameter(&custom_led_subscribe_topic);
  wm.addParameter(&custom_publish_topic);

  //set callback that gets called when connecting to previous WiFi fails, and enters Access Point mode
  wm.setAPCallback(configModeCallback);

  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  //init with high specs to pre-allocate larger buffers
  if (psramFound()) {
    config.frame_size = FRAMESIZE_UXGA;
    config.jpeg_quality = 10;
    config.fb_count = 2;
  } else {
    config.frame_size = FRAMESIZE_SVGA;
    config.jpeg_quality = 12;
    config.fb_count = 1;
  }

#if defined(CAMERA_MODEL_ESP_EYE)
  pinMode(13, INPUT_PULLUP);
  pinMode(14, INPUT_PULLUP);
#endif

  // camera init
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  sensor_t * s = esp_camera_sensor_get();
  //initial sensors are flipped vertically and colors are a bit saturated
  if (s->id.PID == OV3660_PID) {
    s->set_vflip(s, 1);//flip it back
    s->set_brightness(s, 1);//up the blightness just a bit
    s->set_saturation(s, -2);//lower the saturation
  }
  //drop down frame size for higher initial frame rate
  s->set_framesize(s, FRAMESIZE_VGA);

#if defined(CAMERA_MODEL_M5STACK_WIDE)
  s->set_vflip(s, 1);
  s->set_hmirror(s, 1);
#endif

  //WiFi.begin(ssid, password);

  //while (WiFi.status() != WL_CONNECTED) {
  //delay(500);
  //Serial.print(".");
  //}
  //Serial.println("");
  //Serial.println("WiFi connected");

  if (!wm.autoConnect("WifiCamCar")) {
    Serial.println("failed to connect and hit timeout");
    //reset and try again, or maybe put it to deep sleep
    ESP.restart();
    delay(1000);
  }

  startCameraServer();

  MQTT_SERVER =  custom_mqtt_server.getValue();
  MQTT_PUBLISHER_TOPIC = custom_publish_topic.getValue();
  MQTT_TEST_TOPIC = custom_led_subscribe_topic.getValue();

  Serial.println(MQTT_SERVER);
  Serial.println(MQTT_PUBLISHER_TOPIC);
  Serial.println(MQTT_TEST_TOPIC);
  
  Serial.print("Camera Ready! Use 'http://");
  Serial.print(WiFi.localIP());
  Serial.println("' to connect");
  ticker.detach();
  digitalWrite(LED, LOW);
  client.setServer(MQTT_SERVER, 1883);
  client.setClient(wclient);
  Serial.println("HTTP server started");
  client.setCallback(callback); //Registo da função que vai responder ás mensagens vindos do MQTT
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    if (checkMqttConnection()) {
      //Serial.println("Conectado");
      client.loop();
      //server.handleClient();
      if (OTA) {
        if (OTABegin) {
          setupOTA();
          OTABegin = false;
        }
        //ArduinoOTA.handle();
        otaServer.handleClient();
      }
    }
    unsigned long now = millis();
    if (now - lastMsg > MSG_DELAY) {
      lastMsg = now;
      //++value;
      verifyLED();
      //snprintf (msg, MSG_BUFFER_SIZE, "hello world #%ld", value);
      //Serial.print("Publish message: ");
      //Serial.println(msg);
      client.publish(MQTT_PUBLISHER_TOPIC, msg);
    }
  }
}

  
