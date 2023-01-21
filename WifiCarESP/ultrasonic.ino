void moveServo (int pos) {
  ultrasonicServo.attach(D8);
  Serial.print("pos=");
  Serial.println(pos);
  ultrasonicServo.write(pos);
  delay(100);
  ultrasonicServo.detach();
  return;
}

void detectObstacles () {
  distance = ultrasonic.distanceRead();
  if (distance <= MIN_DISTANCE && moveStr_Pub == "forward") moveBrake();
  return;
}
