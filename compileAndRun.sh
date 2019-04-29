echo "compiling JenkinsToJira fat jar..."
./gradlew clean fatJar
echo $@
java -jar JenkinsToJira/build/libs/JenkinsToJira-fat.jar $@