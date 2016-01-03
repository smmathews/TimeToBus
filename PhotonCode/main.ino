#include "Adafruit_SSD1306.h"

// use hardware SPI
#define OLED_DC     D3
#define OLED_CS     D4
#define OLED_RESET  D5
Adafruit_SSD1306 display(OLED_DC, OLED_RESET, OLED_CS);

int curMinutes = -1;
int curSeconds = -1;
char message[64];
char displayedMessage[64];

void refresh_every_second()
{
  if(curSeconds > 0 || curMinutes > 0)
  {
    curSeconds -= 1;
    if(curSeconds < 0)
    {
      curMinutes -= 1;
      curSeconds = 59;
    }
    refresh();
  }
}

Timer timer(1000, refresh_every_second);

void setup()   {

  //init the display settings
  display.begin(SSD1306_SWITCHCAPVCC);
  display.clearDisplay();
  display.display();
  sprintf(displayedMessage, "");

  refresh();
  Particle.function("TimeToBus", TimeToBus);
  timer.start();
}

int TimeToBus(String input)
{
  int newMin;
  int newSec;
  sscanf(input,"%d:%d", &newMin, &newSec);
  Particle.publish("TimeToBusUpdated", input);
  curMinutes = newMin;
  curSeconds = newSec;
  if(curMinutes < 0 || curMinutes > 1440 || curSeconds < 0 || curSeconds > 59)
  {
    curMinutes = -1;
    curSeconds = -1;
    refresh();
    return -1;
  }
  refresh();
  return 0;
}

void refresh() {
  if(curMinutes >= 0 && curSeconds >= 0)
  {
      sprintf(message,"TTB:\n%.2d:%.2d", curMinutes, curSeconds);
  }
  else
  {
      sprintf(message,"TTB:\nUNKNOWN");
  }
  if(strcmp(message, displayedMessage) != 0)
  {
      display.setTextSize(3);
      display.setTextColor(WHITE);
      display.setCursor(0,0);
      display.setTextWrap(false);
      display.clearDisplay();
      display.println(message);
      display.display();
      sprintf(displayedMessage,message);
  }
}
