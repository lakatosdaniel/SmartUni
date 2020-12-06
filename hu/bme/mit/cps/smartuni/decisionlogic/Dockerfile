FROM java:8
WORKDIR /smartuni
ADD ./hu/bme/mit/cps/smartuni/*.java /smartuni/hu/bme/mit/cps/smartuni/
ADD ./hu/bme/mit/cps/smartuni/decisionlogic/*.java /smartuni/hu/bme/mit/cps/smartuni/decisionlogic/
ADD rti_license.dat /smartuni/rti_license.dat
ADD lib/* /smartuni/libs/
ADD data/*.csv /smartuni/data/
ADD ./hu/bme/mit/cps/smartuni/decisionlogic/MANIFEST.MF /smartuni/META-INF/MANIFEST.MF
ENV LD_LIBRARY_PATH="/smartuni/libs:${LD_LIBRARY_PATH}"
RUN javac -classpath /smartuni/libs/nddsjava.jar ./hu/bme/mit/cps/smartuni/Temperature.java ./hu/bme/mit/cps/smartuni/TemperatureSeq.java ./hu/bme/mit/cps/smartuni/TemperatureTypeCode.java ./hu/bme/mit/cps/smartuni/TemperatureTypeSupport.java ./hu/bme/mit/cps/smartuni/TemperatureDataReader.java ./hu/bme/mit/cps/smartuni/TemperatureDataWriter.java ./hu/bme/mit/cps/smartuni/TimeTable.java ./hu/bme/mit/cps/smartuni/TimeTableSeq.java ./hu/bme/mit/cps/smartuni/TimeTableTypeCode.java ./hu/bme/mit/cps/smartuni/TimeTableTypeSupport.java ./hu/bme/mit/cps/smartuni/TimeTableDataReader.java ./hu/bme/mit/cps/smartuni/TimeTableDataWriter.java ./hu/bme/mit/cps/smartuni/WindowState.java ./hu/bme/mit/cps/smartuni/WindowStateSeq.java ./hu/bme/mit/cps/smartuni/WindowStateTypeCode.java ./hu/bme/mit/cps/smartuni/WindowStateTypeSupport.java ./hu/bme/mit/cps/smartuni/WindowStateDataReader.java ./hu/bme/mit/cps/smartuni/WindowStateDataWriter.java ./hu/bme/mit/cps/smartuni/Action.java ./hu/bme/mit/cps/smartuni/ActionSeq.java ./hu/bme/mit/cps/smartuni/ActionTypeCode.java ./hu/bme/mit/cps/smartuni/ActionTypeSupport.java ./hu/bme/mit/cps/smartuni/ActionDataReader.java ./hu/bme/mit/cps/smartuni/ActionDataWriter.java ./hu/bme/mit/cps/smartuni/ActionKind.java ./hu/bme/mit/cps/smartuni/ActionKindSeq.java ./hu/bme/mit/cps/smartuni/ActionKindTypeCode.java ./hu/bme/mit/cps/smartuni/ActionKindTypeSupport.java ./hu/bme/mit/cps/smartuni/decisionlogic/DecisionLogic.java
RUN jar cmvf ./META-INF/MANIFEST.MF cps-smartuni-decisionlogic.jar hu/bme/mit/cps/smartuni/decisionlogic/*.class hu/bme/mit/cps/smartuni/*.class
CMD java -jar cps-smartuni-decisionlogic.jar
