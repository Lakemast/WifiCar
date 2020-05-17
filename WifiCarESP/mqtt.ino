void setupOTA() {
  MDNS.begin(host);
  httpUpdater.setup(&httpServer, update_path, update_username, update_password);
  httpServer.begin();
  MDNS.addService("http", "tcp", 80);
  
  /*Serial.print("HTTPUpdateServer ready! Open http://");
  Serial.print(WiFi.localIP().toString());
  Serial.print(update_path);
  Serial.print(" in your browser and login with username: ");
  Serial.print(update_username);
  Serial.print("  and password: ");
  Serial.println(update_password);*/
}

//Chamada de recepção de mensagem
void callback(char *topic, byte *payload, unsigned int length) {
  String payloadStr = "";
  for (int i = 0; i < length; i++) {
    payloadStr += (char)payload[i];
  }
  String topicStr = String(topic);
  Serial.println(topicStr);
  if (topicStr.equals(MQTT_SYSTEM_CONTROL_TOPIC)) {
    if (payloadStr.equals("OTA_ON_" + String(HOSTNAME))) {
      Serial.println("OTA ON");
      OTA = true;
      OTABegin = true;
      payloadStr = payloadStr + "_" + WiFi.localIP().toString();
      client.publish(MQTT_LOG, payloadStr.c_str());
    } else if (payloadStr.equals("OTA_OFF_" + String(HOSTNAME))) {
      Serial.println("OTA OFF");
      OTA = false;
      OTABegin = false;
      client.publish(MQTT_LOG, payloadStr.c_str());
    } else if (payloadStr.equals("REBOOT_" + String(HOSTNAME))) {
      Serial.println("REBOOT");
      ESP.restart();
    }
  } else if (topicStr.equals(MQTT_TEST_TOPIC)) {
    //TOPICO DE TESTE
    payloadInterpreter(payloadStr);
    Serial.println(payloadStr);
  }
}


void payloadInterpreter ( String payloadStr ) {
  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(payloadStr);
  if (!root.success()) {
    Serial.println("parseObject() failed");
    return;
  }
  String moveStr = root["move"];
  moveStr_Pub = moveStr;
  pwmA = root["pwma"];
  pwmB = root["pwmb"];
  Serial.print("moveStr=");
  Serial.println(moveStr);

  Serial.print("pwmA=");
  Serial.println(pwmA);
  Serial.print("pwmB=");
  Serial.println(pwmB);

  if ( moveStr == "forward" ) moveForward(pwmA, pwmB);
  else if ( moveStr == "backward" ) moveBackward(pwmA, pwmB);
  else if ( moveStr == "left" ) moveLeft(pwmA, pwmB);
  else if ( moveStr == "right" ) moveRight(pwmA, pwmB);
  else if ( moveStr == "brake" ) moveBrake();
  else if ( moveStr == "neutral" ) moveNeutral();
  return;
}

void messageJSON() {
  StaticJsonBuffer<300> JSONbuffer;
  JsonObject& JSONencoder = JSONbuffer.createObject();
  JSONencoder["obstacle"] = obstacleDetected;
  JSONencoder["distance"] = distance;
  JSONencoder["voltage"] = checkBatteryVoltage ();
  JSONencoder.printTo(msg, sizeof(msg));
  //Serial.println(msg);
}


bool checkMqttConnection() {
  if (!client.connected()) {
    if (MQTT_AUTH ? client.connect(HOSTNAME.c_str(), MQTT_USERNAME, MQTT_PASSWORD) : client.connect(HOSTNAME.c_str())) {
      Serial.println("CONNECTED TO MQTT BROKER " + String(MQTT_SERVER));
      client.publish(MQTT_LOG, String("CONNECTED_" + HOSTNAME).c_str());
      //SUBSCRIÇÃO DE TOPICOS
      client.subscribe(MQTT_SYSTEM_CONTROL_TOPIC);
      client.subscribe(MQTT_TEST_TOPIC);
    }
  }
  return client.connected();
}

float checkBatteryVoltage (){
  float batteryVoltage = 0;
  batteryVoltage = analogRead(A0);
  //Serial.println(batteryVoltage);
  batteryVoltage = round((batteryVoltage/340)*7.722*100)/100;
  return batteryVoltage;
}
