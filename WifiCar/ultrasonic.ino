void detectObstacles (){
  
 // Serial.print("Distancia: "); // Escreve texto na tela
 // Serial.print(ultrasonic.distanceRead());// distância medida em cm
 // Serial.println("cm"); // escreve texto na tela e pula uma linha
 distance = ultrasonic.distanceRead();
  
}

void moveServo (){
  if(pos > 179 ) pos=0;
  else pos+=5;
  Serial.print("pos=");
  Serial.println(pos);
  ultrasonicServo.write(pos);
}
