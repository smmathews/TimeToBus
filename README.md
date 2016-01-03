# TimeToBus
Code for taking a particle.io photon, plus an Adafruit SSD1306 LED, and programming it to display the time till the bus outside my apartment arrives

video of the finished project:
https://goo.gl/photos/DzGQDtLWrke85AsE7
The moment, 25 seconds in, when it jumps about a minute forward is when a new estimate arrived.

Notes:
1) In retrospect, using pre_away would have been a lot better than pre_dt as it's in seconds for determing the time to bus rather than epoch time for departure time of the bus.
2) I wonder how difficult/impossible it would be to skip the server and just parse the mbta data from the chip itself.
3) Would be nice if an app was used to set the bus stop, rather than hardcoding it.
