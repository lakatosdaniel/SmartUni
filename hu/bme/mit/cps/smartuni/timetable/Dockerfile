FROM java:8
WORKDIR /smartuni
ADD ./hu/bme/mit/cps/smartuni/*.java /smartuni/hu/bme/mit/cps/smartuni/
ADD ./hu/bme/mit/cps/smartuni/timetable/*.java /smartuni/hu/bme/mit/cps/smartuni/timetable/
ADD rti_license.dat /smartuni/rti_license.dat
ADD lib/* /smartuni/libs/
ADD data/*.csv /smartuni/data/
ADD ./hu/bme/mit/cps/smartuni/timetable/MANIFEST.MF /smartuni/META-INF/MANIFEST.MF
ENV LD_LIBRARY_PATH="/smartuni/libs:${LD_LIBRARY_PATH}"
RUN javac -classpath /smartuni/libs/nddsjava.jar ./hu/bme/mit/cps/smartuni/TimeTable.java ./hu/bme/mit/cps/smartuni/TimeTableSeq.java ./hu/bme/mit/cps/smartuni/TimeTableTypeCode.java ./hu/bme/mit/cps/smartuni/TimeTableTypeSupport.java ./hu/bme/mit/cps/smartuni/TimeTableDataReader.java ./hu/bme/mit/cps/smartuni/TimeTableDataWriter.java ./hu/bme/mit/cps/smartuni/timetable/TimeTableSource.java
RUN jar cmvf ./META-INF/MANIFEST.MF cps-smartuni-timetable.jar hu/bme/mit/cps/smartuni/timetable/*.class hu/bme/mit/cps/smartuni/*.class
CMD java -jar cps-smartuni-timetablesource.jar
