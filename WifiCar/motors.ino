void moveForward (int pwm){ 
  analogWrite(ENA,pwm);
  analogWrite(ENB,pwm);
  
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,LOW);
  
}

void moveBackward (int pwm){
  analogWrite(ENA,pwm);
  analogWrite(ENB,pwm);
  
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,HIGH);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);
}

void moveRight (int pwm){
  analogWrite(ENA,pwm);
  analogWrite(ENB,pwm);
  
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);
}

void moveLeft (int pwm){
  analogWrite(ENA,pwm);
  analogWrite(ENB,pwm);
  
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,HIGH);
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,LOW);
}

void moveBreak (){
  analogWrite(ENA,255);
  analogWrite(ENB,255);
  
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,HIGH);
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,HIGH);
}

void moveNeutral (){
  analogWrite(ENA,0);
  analogWrite(ENB,0);
  
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,LOW);
}
