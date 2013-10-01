Counting Balls Assignment
Anthony Christe

-- Runtime Environment
Java Development Kit >= 1.6

-- Included shell script
I've included a shell script which creates an output directory "out", compiles the source code, and runs the program.
1) To invoke the shell script, first make it executable

   chmod u+x compile_and_run.sh

2) And then run the script

   ./compile_and_run.sh

Or

   bash compile_and_run.sh

If you prefer to do things by hand, follow the steps bellow.

-- Compiling
1) Navigate a terminal window to the source root of this project
2) Create a subdirectory in the source root called "out"

   mkdir out/

3) Run the following command to compile the source code

   javac -Xlint -d out/ src/edu/achriste/processing/*.java src/edu/achriste/ui/*.java src/edu/achriste/utils/*.java

-- Running
1) To run the program, make sure to set the classpath to the output directory

   java -classpath out/ edu.achriste.ui.CountingBallsUI
