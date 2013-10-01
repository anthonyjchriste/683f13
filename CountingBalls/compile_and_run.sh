#!/bin/bash

# Compiles and runs the counting balls program
# ICS 683
# Anthony Christe

# Make sure the output directory exists
mkdir -p out/

# Compiles the program and stores the class files in the directory out/
javac -Xlint -d out/ src/edu/achriste/processing/*.java src/edu/achriste/ui/*.java src/edu/achriste/utils/*.java

# Runs the program
java -classpath out/ edu.achriste.ui.CountingBallsUI
