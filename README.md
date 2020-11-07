# android-open-face-tracking

App destined to emulate what Smoothtrack is doing, using APIs that are available for most devices.

Currently not feature-finished : rotation works, position doesn't.

# Setting up

1. Run Opentrack on the computer where you play.
2. Set up tracker "UDP-over-network". Default port is 4242, but you can change it.
3. Set up filter Accela, with 2.5° smoothing on rotation and position (position does not work yet).
3.5. Set up "YAW" as inverted in Opentrack options/ Output.
4. Find out your IPv4 LAN adress, through the use of "ipconfig" command in windows prompt.
4. Open the app on your phone.
5. Set up the app with the correct IP and correct port. Verify that you are connected to your WiFi, same LAN as your gaming computer.
6. Put the phone in the position where it will track your face.
7. Press the button under the camera to start tracking.


# Known issues

Yaw is actually inverted

If the camera doesn't work on the first run after you gave the app the correct authorisations, just launch the app again.

## Acknowledgements

- [AIRLegend](https://github.com/AIRLegend), for his AITrack code which helped me understand what I was doing.
