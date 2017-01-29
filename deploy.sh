#!/bin/bash
./gradlew desktop:dist
mv desktop/build/libs/desktop-1.0.jar ./bin/game.jar
cp play.sh ./bin/
cp play.bat ./bin/
cd bin
rm are_you_running.zip
zip -r are_you_running.zip .
rm game.jar
rm play.bat
rm play.sh
nautilus . &
cd ..
echo "Done"