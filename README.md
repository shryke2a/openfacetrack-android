# android-open-face-tracking

App destined to emulate IRTrack on an android phone with the camera, using APIs that are available for most devices. It can then broadcast the face's rotation to a computer, allowing to look around in computer games such as Arma or flight sims.

Works only for rotation, position has not been implemented and probably won't be.

It requires the phone and PC to be connected to the same LAN, and the PC to run OpenTrack https://github.com/opentrack/opentrack/

# Version  

You can find the releases here : https://github.com/shryke2a/android-open-face-tracking/releases

# Setting up

1. Run Opentrack on the computer where you play.
2. Set up tracker "UDP-over-network". Default port is 4242, but you can change it.
3. Set up filter Accela, with 2.5° smoothing on rotation and position (position does not work yet). Hamilton is said to be working great too but I haven't tried it.
4. Set up "YAW" as inverted in Opentrack options/ Output.
5. Find out your computer's IPv4 LAN adress, through the use of "ipconfig" command in windows prompt.
6. Install and open the app on your phone.
7. Set up the app with the correct IP and correct port. Verify that you are connected to your WiFi, same LAN as your gaming computer.
8. Put the phone in the position where it will track your face.
9. Press the button under the camera to start tracking.


# Known issues

Yaw is actually inverted.

If the camera doesn't work on the first run after you gave the app the correct authorisations, just launch the app again.



## Acknowledgements

- [AIRLegend](https://github.com/AIRLegend), for his AITrack code which helped me understand what I was doing.
