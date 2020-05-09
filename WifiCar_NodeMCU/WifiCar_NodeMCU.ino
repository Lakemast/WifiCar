#include <Ultrasonic.h> // Declaração de biblioteca
#include <Thread.h>
#include <ThreadController.h>

#define IN1 D0
#define IN2 D1
#define IN3 D2
#define IN4 D3
#define TRIGGER D4
#define ECHO D5
#define ENA D6
#define ENB D7
#define SERVO D8

Ultrasonic ultrasonic(TRIGGER, ECHO); // Instância chamada ultrasonic com parâmetros (trig,echo)
ThreadController cpu;
Thread detectObstacles_Thread;

void setup() {
  Serial.begin(9600); // Inicio da comunicação serial
  
  pinMode(IN1,OUTPUT);
  pinMode(IN2,OUTPUT);
  pinMode(IN3,OUTPUT);
  pinMode(IN4,OUTPUT);
  pinMode(ENA,OUTPUT);
  pinMode(ENB,OUTPUT);
  pinMode(SERVO,OUTPUT);

  //Configuração da Thread de verificação do estado do dispositivo
  detectObstacles_Thread.setInterval(200);
  detectObstacles_Thread.onRun(detectObstacles);

  //Configuração do ThreadController
  cpu.add(&detectObstacles_Thread);
}

void loop() {
  cpu.run();
  moveForward(255);
  Serial.println("Move Forward!");
  delay(1000); // aguarda 1s
  moveBackward(255);
  Serial.println("Move Backward!");
  delay(1000); // aguarda 1s
  moveRight(255);
  Serial.println("Move Right!");
  delay(1000); // aguarda 1s
  moveLeft(255);
  Serial.println("Move Left!");
  delay(1000); // aguarda 1s
  moveBreak();
  Serial.println("Move Break!");
  delay(1000); // aguarda 1s
  moveNeutral();
  Serial.println("Move Neutral!");
  delay(1000); // aguarda 1s
  
}
