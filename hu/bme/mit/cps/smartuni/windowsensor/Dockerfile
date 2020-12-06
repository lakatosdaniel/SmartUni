FROM java:8
WORKDIR /smartuni
ADD ./hu/bme/mit/cps/smartuni/*.java /smartuni/hu/bme/mit/cps/smartuni/
ADD ./hu/bme/mit/cps/smartuni/windowsensor/*.java /smartuni/hu/bme/mit/cps/smartuni/windowsensor/
ADD rti_license.dat /smartuni/rti_license.dat
ADD lib/* /smartuni/libs/
ADD data/*.csv /smartuni/data/
ADD ./hu/bme/mit/cps/smartuni/windowsensor/MANIFEST.MF /smartuni/META-INF/MANIFEST.MF
ENV LD_LIBRARY_PATH="/smartuni/libs:${LD_LIBRARY_PATH}"
RUN javac -classpath /smartuni/libs/nddsjava.jar ./hu/bme/mit/cps/smartuni/WindowState.java ./hu/bme/mit/cps/smartuni/WindowStateSeq.java ./hu/bme/mit/cps/smartuni/WindowStateTypeCode.java ./hu/bme/mit/cps/smartuni/WindowStateTypeSupport.java ./hu/bme/mit/cps/smartuni/WindowStateDataReader.java ./hu/bme/mit/cps/smartuni/WindowStateDataWriter.java ./hu/bme/mit/cps/smartuni/windowsensor/WindowSensor.java
RUN jar cmvf ./META-INF/MANIFEST.MF cps-smartuni-windowsensor.jar hu/bme/mit/cps/smartuni/windowsensor/*.class hu/bme/mit/cps/smartuni/*.class
CMD java -jar cps-smartuni-windowsensor.jar
