void moveServo (int pos) {
  Serial.print("pos=");
  Serial.println(pos);
  ultrasonicServo.write(pos);
  return;
}

void detectObstacles () {
  distance = ultrasonic.distanceRead();
  if (distance <= MIN_DISTANCE && moveStr_Pub == "forward") moveBrake();
  return;
}
