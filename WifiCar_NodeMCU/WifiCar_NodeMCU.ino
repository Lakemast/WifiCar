#include <Ultrasonic.h> // Declaração de biblioteca
 
Ultrasonic ultrasonic(D4, D5); // Instância chamada ultrasonic com parâmetros (trig,echo)
 
void setup() { 
  Serial.begin(9600); // Inicio da comunicação serial
}
 
void loop() {
  Serial.print("Distancia: "); // Escreve texto na tela
  Serial.print(ultrasonic.distanceRead());// distância medida em cm
  Serial.println("cm"); // escreve texto na tela e pula uma linha
  delay(1000); // aguarda 1s 
}
