const char* loginIndex =
  "<form name='loginForm'>"
  "<table width='20%' bgcolor='A09F9F' align='center'>"
  "<tr>"
  "<td colspan=2>"
  "<center><font size=4><b>ESP32 Login Page</b></font></center>"
  "<br>"
  "</td>"
  "<br>"
  "<br>"
  "</tr>"
  "<td>Username:</td>"
  "<td><input type='text' size=25 name='userid'><br></td>"
  "</tr>"
  "<br>"
  "<br>"
  "<tr>"
  "<td>Password:</td>"
  "<td><input type='Password' size=25 name='pwd'><br></td>"
  "<br>"
  "<br>"
  "</tr>"
  "<tr>"
  "<td><input type='submit' onclick='check(this.form)' value='Login'></td>"
  "</tr>"
  "</table>"
  "</form>"
  "<script>"
  "function check(form)"
  "{"
  "if(form.userid.value=='admin' && form.pwd.value=='admin')"
  "{"
  "window.open('/serverIndex')"
  "}"
  "else"
  "{"
  " alert('Error Password or Username')/*displays error message*/"
  "}"
  "}"
  "</script>";

/*
   Server Index Page
*/

const char* serverIndex =
  "<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js'></script>"
  "<form method='POST' action='#' enctype='multipart/form-data' id='upload_form'>"
  "<input type='file' name='update'>"
  "<input type='submit' value='Update'>"
  "</form>"
  "<div id='prg'>progress: 0%</div>"
  "<script>"
  "$('form').submit(function(e){"
  "e.preventDefault();"
  "var form = $('#upload_form')[0];"
  "var data = new FormData(form);"
  " $.ajax({"
  "url: '/update',"
  "type: 'POST',"
  "data: data,"
  "contentType: false,"
  "processData:false,"
  "xhr: function() {"
  "var xhr = new window.XMLHttpRequest();"
  "xhr.upload.addEventListener('progress', function(evt) {"
  "if (evt.lengthComputable) {"
  "var per = evt.loaded / evt.total;"
  "$('#prg').html('progress: ' + Math.round(per*100) + '%');"
  "}"
  "}, false);"
  "return xhr;"
  "},"
  "success:function(d, s) {"
  "console.log('success!')"
  "},"
  "error: function (a, b, c) {"
  "}"
  "});"
  "});"
  "</script>";


void setupOTA() { 
  int cont = 0;
  /*use mdns for host name resolution*/
  if (!MDNS.begin(host)) { //http://esp32.local
    Serial.println("Error setting up MDNS responder!");
    while (cont < 4) {
      delay(1000);
      cont++;
    }
  }
  cont = 0;
  //Serial.println("mDNS responder started");
  /*return index page which is stored in serverIndex */
  otaServer.on("/", HTTP_GET, []() {
    otaServer.sendHeader("Connection", "close");
    otaServer.send(200, "text/html", loginIndex);
  });
  otaServer.on("/serverIndex", HTTP_GET, []() {
    otaServer.sendHeader("Connection", "close");
    otaServer.send(200, "text/html", serverIndex);
  });
  /*handling uploading firmware file */
  otaServer.on("/update", HTTP_POST, []() {
    otaServer.sendHeader("Connection", "close");
    otaServer.send(200, "text/plain", (Update.hasError()) ? "FAIL" : "OK");
    ESP.restart();
  }, []() {
    HTTPUpload& upload = otaServer.upload();
    if (upload.status == UPLOAD_FILE_START) {
      Serial.printf("Update: %s\n", upload.filename.c_str());
      if (!Update.begin(UPDATE_SIZE_UNKNOWN)) { //start with max available size
        Update.printError(Serial);
      }
    } else if (upload.status == UPLOAD_FILE_WRITE) {
      /* flashing firmware to ESP*/
      if (Update.write(upload.buf, upload.currentSize) != upload.currentSize) {
        Update.printError(Serial);
      }
    } else if (upload.status == UPLOAD_FILE_END) {
      if (Update.end(true)) { //true to set the size to the current progress
        Serial.printf("Update Success: %u\nRebooting...\n", upload.totalSize);
      } else {
        Update.printError(Serial);
      }
    }
  });
  otaServer.begin();
  return;
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
    }
    else if (payloadStr.equals("OTA_OFF_" + String(HOSTNAME))) {
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
    if(payloadStr.equals("ON"))digitalWrite(LED,HIGH);
    else if(payloadStr.equals("OFF"))digitalWrite(LED,LOW);
    else if(payloadStr.equals("TOGGLE"))digitalWrite(LED,!digitalRead(LED));
    Serial.println(payloadStr);
  }
}

bool checkMqttConnection() {
  if (!client.connected()) {
    if (MQTT_AUTH ? client.connect(host, MQTT_USERNAME, MQTT_PASSWORD) : client.connect(host)) {
      Serial.println("CONNECTED TO MQTT BROKER " + String(MQTT_SERVER));
      client.publish(MQTT_LOG, String("CONNECTED_" + HOSTNAME).c_str());
      //SUBSCRIÇÃO DE TOPICOS
      client.subscribe(MQTT_SYSTEM_CONTROL_TOPIC);
      client.subscribe(MQTT_TEST_TOPIC);
    }
  }
  return client.connected();
}


void verifyLED() {
  if (digitalRead(LED)) snprintf (msg, MSG_BUFFER_SIZE, "ON");
  else  snprintf (msg, MSG_BUFFER_SIZE, "OFF");
}

void tick() {
    //toggle state
    digitalWrite(LED, !digitalRead(LED));     // set pin to the opposite state
  }

  //gets called when WiFiManager enters configuration mode
  void configModeCallback (WiFiManager * myWiFiManager) {
    Serial.println("Entered config mode");
    Serial.println(WiFi.softAPIP());
    //if you used auto generated SSID, print it
    Serial.println(myWiFiManager->getConfigPortalSSID());
    //entered config mode, make led toggle faster
    ticker.attach(0.2, tick);
  }
