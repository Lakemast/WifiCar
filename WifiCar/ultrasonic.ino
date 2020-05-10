void detectObstacles () {

  //Serial.print("Distancia: "); // Escreve texto na tela
  //Serial.print(ultrasonic.distanceRead());// distância medida em cm
  //Serial.println("cm"); // escreve texto na tela e pula uma linha
  
  distance = ultrasonic.distanceRead();

  //Serial.print("pos=");
  //Serial.print(pos);
  //Serial.print(" distance=");
  //Serial.println(distance);
  
  obstacles_scan++;
  if (distance <= MIN_DISTANCE) {
    if (pos < 90) obstacle_right++;
    else obstacle_left++;
  }

  //Serial.print("obstacleRight=");
  //Serial.print(obstacle_right);
  //Serial.print(" obstacleLeft=");
  //Serial.println(obstacle_left);

  if (obstacle_right > obstacle_left) obstacleDetected = "right";
  else if (obstacle_left > obstacle_right) obstacleDetected = "left";
  //else if (obstacle_left!=0 && obstacle_right!=0 && obstacle_left == obstacle_right) obstacleDetected = "front";
  else  obstacleDetected = "none";
  
  // Serial.print(" obstacles_scan:");
  // Serial.println(obstacles_scan);
  avoidObstacles();
  
  if ( obstacles_scan > 36 ) {
    obstacle_right = 0;
    obstacle_left = 0;
    obstacles_scan = 0;
    Serial.print("obstacle:");
    Serial.println(obstacleDetected);
  }

}

void moveServo () {
  if (pos == 0) right = true;
  if (pos == 180) right = false;
  if (right) pos += 10;
  else pos -= 10;
  //Serial.print("pos=");
  //Serial.println(pos);
  ultrasonicServo.write(pos);
}

void avoidObstacles(){
  
  if( obstacleDetected != "none" && moveStr_Pub == "forward" ) moveBreak ();
  
}
