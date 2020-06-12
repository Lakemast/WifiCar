// MQTT URL - addr 0 byte -> 200 byte 
// PUBLISH TOPIC - addr 201 byte -> 401 byte
// SUBSCRIBE TOPIC - addr 402 byte -> 602 byte

void writeStringEeprom (int addrOffset, String &url ) {
  byte len = url.length();
  
  EEPROM.write(addrOffset, len);

  //Serial.println(addrOffset);
  //Serial.println(len);

  for (int i = 0 ; i < len; i++) {
    EEPROM.write(addrOffset + i + 1, url[i]);
    Serial.print(url[i]);
  }
  //EEPROM.write(len+1, '\0');
  
  EEPROM.commit();

  
  //if (EEPROM.commit()) Serial.println("EEPROM successfully committed");
  //else                 Serial.println("ERROR! EEPROM commit failed");

  //EEPROM.end();
  return;
}

char* readCharArrayEeprom (int addrOffset) {
  int len = EEPROM.read(addrOffset);
  //Serial.println(len);
  char url [200];
  for ( int i = 0; i < len+1; i++) {
    url[i] = EEPROM.read(addrOffset + i + 1);
    Serial.println(url[i]);
  }
  url [len]='\0';
  Serial.println(url);
  return url;
}

void clearEeprom () {
  //EEPROM.begin(512);
  // write a 0 to all 512 bytes of the EEPROM
  for (int i = 0; i < 4096; i++) {
    EEPROM.write(i, 0);
  }
  EEPROM.commit();
  //EEPROM.end();
}
