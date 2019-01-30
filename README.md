# BusTalk

# About
An Android application that lets users that are connected to a particular electric bus (Västtrafik) join a common chatroom.
Created for a course in software engineering at Chalmers University of Technology, and also for participation in a competition held by ElectriCity in 2015.

# Installation tips

1.The class ConnectionsHandler has a boolean "isTest". When the boolean is true, the application will ignore to check if the wifi is correct. When false the program will work as intended.

2. You will need an android phone to test the app since normal emulators don't support wifimanagers.

# Conduction of survey and given feedback
The concept of this project was created during the Concept Workshop held by ElectriCity at Lindholmen. There was a wish for statistics to show that people really wanted this app and which organization/company could actually want to invest in it. To help us answer these questions a survey was created. The link to it can be seen below. The survey was shared on our personal Facebook walls.

Pure data:
https://docs.google.com/spreadsheets/d/1rwEL9TgjVIR__5OHLZQOlrLK3iBE1DTXMMqGTMQ-Ic8/edit?usp=sharing

Diagram:
https://docs.google.com/forms/d/1-cKwGODiy2N8YEI3UPDMFc3HCd829ScWu3Al_iVM6-k/viewanalytics

# Notes
Might be worth noting that the program will not fetch the correct "next busstop" if the Innovation Platform is down.

The pure app source code is located at the Client branch, the source dode for the server is located at the Server branch.
