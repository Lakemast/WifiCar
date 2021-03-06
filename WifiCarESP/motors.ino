
// max pwmA = 255 max pwmB = 170

void moveForward (int pwmA, int pwmB) {
  analogWrite(ENA, pwmA);
  analogWrite(ENB, pwmB);

  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, HIGH);

}

void moveBackward (int pwmA, int pwmB) {
  analogWrite(ENA, pwmA);
  analogWrite(ENB, pwmB);

  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW);
}

void moveRight (int pwmA, int pwmB) {
  analogWrite(ENA, pwmA);
  analogWrite(ENB, pwmB);

  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, HIGH);
}

void moveLeft (int pwmA, int pwmB) {
  analogWrite(ENA, pwmA);
  analogWrite(ENB, pwmB);

  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW);
}

void moveBrake () {
  analogWrite(ENA, 0);
  analogWrite(ENB, 0);

  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, HIGH);
}

void moveNeutral () {
  analogWrite(ENA, 0);
  analogWrite(ENB, 0);

  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);
}
