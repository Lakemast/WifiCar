const String postForms = "<html>\
  <head>\
    <title> WifiCar Over MQTT</title>\
    <style>\
      body { padding: 35px; background-color: #333; font-family: Arial, Helvetica, Sans-Serif; Color: #fff; }\
      .inputtext1{ margin-left: 13px; background-color: #b8b7b6; Color: #333; height:2.3em }\
      .inputtext2{ margin-left: 17px; background-color: #b8b7b6; Color: #333; height:2.3em }\
      .inputtext3{ margin-left: 35px; background-color: #b8b7b6; Color: #333; height:2.3em }\
      input[type=button], input[type=submit], input[type=reset] {\
          background-color: #11a67b;\
          border: none;\
          color: white;\
          padding: 16px 32px;\
          text-decoration: none;\
          margin: 2px 196px;\
          cursor: pointer;\
      }\
    </style>\
  </head>\
  <body>\ <h1> WifiCar Over MQTT </h1><br>\
    <form method=\"post\" enctype=\"application/x-www-form-urlencoded\" action=\"/postform/\">\
      MQTT Broker Url:<input type=\"text\" class=\"inputtext1\" name=\"mqtturl\" value=\"broker.hivemq.com\"><br><br>\
      Subscribe Topic: <input type=\"text\" class=\"inputtext2\" name=\"subscribetopic\" value=\"wificar/status\"><br><br>\
      Publish Topic:  <input type=\"text\" class=\"inputtext3\" name=\"publishtopic\" value=\"wificar/control\"><br><br>\
      <input type=\"submit\" value=\"Submit\">\
    </form>\
  </body>\
</html>";

const String responseForms1 = "<html>\
  <head>\
    <title> WifiCar Over MQTT</title>\
    <style>\
      body { padding: 35px; background-color: #333; font-family: Arial, Helvetica, Sans-Serif; Color: #fff; }\
      .inputtext1{ margin-left: 13px; background-color: #b8b7b6; Color: #333; height:2.3em }\
      .inputtext2{ margin-left: 17px; background-color: #b8b7b6; Color: #333; height:2.3em }\
      .inputtext3{ margin-left: 35px; background-color: #b8b7b6; Color: #333; height:2.3em }\
      input[type=button], input[type=submit], input[type=reset] {\
          background-color: #11a67b;\
          border: none;\
          color: white;\
          padding: 16px 32px;\
          text-decoration: none;\
          margin: 2px 196px;\
          cursor: pointer;\
      }\
      a:link, a:visited {\
        background-color: #11a67b;\
        color: white;\
        padding: 14px 25px;\
        text-align: center;\
        text-decoration: none;\
        display: inline-block;\
      }\
      a:hover, a:active {\
        background-color: red;\
      }\
    </style>\
  </head>\
  <body>\ 
      <h1> WifiCar Over MQTT </h1><br>\
      <p> Configuration Updated Sucessful! </p>\
      <p>";

      

const String responseForms2 ="</p> <a href=\"/\">Return</a>\          
   </form>\
  </body>\
</html>";

void handleRoot() {
  server.send(200, "text/html", postForms);
}

void handlePlain() {
  if (server.method() != HTTP_POST) {
    server.send(405, "text/plain", "Method Not Allowed");
  } else {
    server.send(200, "text/plain", "POST body was:\n" + server.arg("plain"));
  }
}

void handleForm() {
  if (server.method() != HTTP_POST) {
    server.send(405, "text/plain", "Method Not Allowed");
  } else {
    String message = "POST form was:<br>";
    for (uint8_t i = 0; i < server.args(); i++) {
      message += " " + server.argName(i) + ": " + server.arg(i) + "<br>";
    }
    char buf [200];
    char buf1 [200];
    char buf2 [200];
    server.arg(0).toCharArray(buf,200);
    MQTT_SERVER = buf;
    server.arg(1).toCharArray(buf1,200);
    MQTT_TEST_TOPIC = buf1;
    server.arg(2).toCharArray(buf2,200);
    MQTT_PUBLISHER_TOPIC = buf2;

    Serial.println(MQTT_SERVER);
    Serial.println(MQTT_TEST_TOPIC);
    Serial.println(MQTT_PUBLISHER_TOPIC);
    String mQTTBrokerURL = server.arg(0);
    String testTopic = server.arg(1);
    String publishTopic = server.arg(2);
    
    writeStringEeprom (ADDR_MQTT_URL,mQTTBrokerURL);
    writeStringEeprom (ADDR_SUBSCRIBE_TOPIC,testTopic );
    writeStringEeprom (ADDR_PUBLISH_TOPIC,publishTopic);
      
    client.disconnect();
    client.setServer(MQTT_SERVER,1883);
    
    //char * MQTT_TEST_TOPIC;
    //char * MQTT_PUBLISHER_TOPIC;
    
    server.send(200, "text/html", responseForms1 + message + responseForms2);
    
  }
}

void handleNotFound() {
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET) ? "GET" : "POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i = 0; i < server.args(); i++) {
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
}
