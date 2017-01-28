#!/bin/bash
./gradlew desktop:dist
mv desktop/build/libs/desktop-1.0.jar ./bin/
cp play.sh ./bin/
cp play.bat ./bin/
cd bin
zip -r are_your_running.zip .
rm desktop-1.0.jar
rm play.bat
rm play.sh
cd ..
echo "Done"