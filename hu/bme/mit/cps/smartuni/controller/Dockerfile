FROM java:8
WORKDIR /smartuni
ADD ./hu/bme/mit/cps/smartuni/*.java /smartuni/hu/bme/mit/cps/smartuni/
ADD ./hu/bme/mit/cps/smartuni/controller/*.java /smartuni/hu/bme/mit/cps/smartuni/controller/
ADD rti_license.dat /smartuni/rti_license.dat
ADD lib/* /smartuni/libs/
ADD data/*.csv /smartuni/data/
ADD ./hu/bme/mit/cps/smartuni/controller/MANIFEST.MF /smartuni/META-INF/MANIFEST.MF
ENV LD_LIBRARY_PATH="/smartuni/libs:${LD_LIBRARY_PATH}"
RUN javac -classpath /smartuni/libs/nddsjava.jar ./hu/bme/mit/cps/smartuni/Action.java ./hu/bme/mit/cps/smartuni/ActionSeq.java ./hu/bme/mit/cps/smartuni/ActionTypeCode.java ./hu/bme/mit/cps/smartuni/ActionTypeSupport.java ./hu/bme/mit/cps/smartuni/ActionDataReader.java ./hu/bme/mit/cps/smartuni/ActionDataWriter.java ./hu/bme/mit/cps/smartuni/ActionKind.java ./hu/bme/mit/cps/smartuni/ActionKindSeq.java ./hu/bme/mit/cps/smartuni/ActionKindTypeCode.java ./hu/bme/mit/cps/smartuni/ActionKindTypeSupport.java ./hu/bme/mit/cps/smartuni/controller/Controller.java
RUN jar cmvf ./META-INF/MANIFEST.MF cps-smartuni-controller.jar hu/bme/mit/cps/smartuni/controller/*.class hu/bme/mit/cps/smartuni/*.class
CMD java -jar cps-smartuni-controller.jar
