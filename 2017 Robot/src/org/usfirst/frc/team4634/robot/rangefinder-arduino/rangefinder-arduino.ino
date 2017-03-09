/***************************************************************************/
// Function: Measure the distance to obstacles in front and display the
//                         result on seeedstudio serialLcd. Make sure you installed the
//                         serialLCD, SoftwareSerial and Ultrasonic library.
//        Hardware: Grove - Ultrasonic Ranger, Grove - Serial LCD
//        Arduino IDE: Arduino-1.0
//        Author:         LG
//        Date:          Jan 17,2013
//        Version: v1.0 modified by FrankieChu
//        by www.seeedstudio.com

/*****************************************************************************/
#include <SoftwareSerial.h>
#include "Ultrasonic.h"

Ultrasonic ultrasonic(7);
#define trigPin 13
#define echoPin 10

void setup() {
  Serial.begin (9600);
}

void loop()
{
    long RangeInCentimeters;
    RangeInCentimeters = ultrasonic.MeasureInCentimeters();
    delay(150);
    Serial.println("The distance:" + RangeInCentimeters,DEC + "cm");
    
}
