FROM java:8
WORKDIR /smartuni
ARG sensor_value=0
ENV sensor=${sensor_value}
ADD ./hu/bme/mit/cps/smartuni/*.java /smartuni/hu/bme/mit/cps/smartuni/
ADD ./hu/bme/mit/cps/smartuni/temperaturesensor/*.java /smartuni/hu/bme/mit/cps/smartuni/temperaturesensor/
ADD rti_license.dat /smartuni/rti_license.dat
ADD lib/* /smartuni/libs/
ADD data/*.csv /smartuni/data/
ADD ./hu/bme/mit/cps/smartuni/temperaturesensor/MANIFEST.MF /smartuni/META-INF/MANIFEST.MF
ENV LD_LIBRARY_PATH="/smartuni/libs:${LD_LIBRARY_PATH}"
RUN javac -classpath /smartuni/libs/nddsjava.jar ./hu/bme/mit/cps/smartuni/Temperature.java ./hu/bme/mit/cps/smartuni/TemperatureSeq.java ./hu/bme/mit/cps/smartuni/TemperatureTypeCode.java ./hu/bme/mit/cps/smartuni/TemperatureTypeSupport.java ./hu/bme/mit/cps/smartuni/TemperatureDataReader.java ./hu/bme/mit/cps/smartuni/TemperatureDataWriter.java ./hu/bme/mit/cps/smartuni/temperaturesensor/TemperatureSensor.java
RUN jar cmvf ./META-INF/MANIFEST.MF cps-smartuni-temperaturesensor.jar hu/bme/mit/cps/smartuni/temperaturesensor/*.class hu/bme/mit/cps/smartuni/*.class
CMD java -jar cps-smartuni-temperaturesensor.jar ${sensor}
