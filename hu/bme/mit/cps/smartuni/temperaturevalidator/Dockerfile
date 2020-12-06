FROM java:8
WORKDIR /smartuni
ADD ./hu/bme/mit/cps/smartuni/*.java /smartuni/hu/bme/mit/cps/smartuni/
ADD ./hu/bme/mit/cps/smartuni/temperaturevalidator/*.java /smartuni/hu/bme/mit/cps/smartuni/temperaturevalidator/
ADD rti_license.dat /smartuni/rti_license.dat
ADD lib/* /smartuni/libs/
ADD data/*.csv /smartuni/data/
ADD ./hu/bme/mit/cps/smartuni/temperaturevalidator/MANIFEST.MF /smartuni/META-INF/MANIFEST.MF
ENV LD_LIBRARY_PATH="/smartuni/libs:${LD_LIBRARY_PATH}"
RUN javac -classpath /smartuni/libs/nddsjava.jar ./hu/bme/mit/cps/smartuni/Temperature.java ./hu/bme/mit/cps/smartuni/TemperatureSeq.java ./hu/bme/mit/cps/smartuni/TemperatureTypeCode.java ./hu/bme/mit/cps/smartuni/TemperatureTypeSupport.java ./hu/bme/mit/cps/smartuni/TemperatureDataReader.java ./hu/bme/mit/cps/smartuni/TemperatureDataWriter.java ./hu/bme/mit/cps/smartuni/temperaturevalidator/TemperatureValidator.java
RUN jar cmvf ./META-INF/MANIFEST.MF cps-smartuni-temperaturevalidator.jar hu/bme/mit/cps/smartuni/temperaturevalidator/*.class hu/bme/mit/cps/smartuni/*.class
CMD java -jar cps-smartuni-temperaturevalidator.jar
