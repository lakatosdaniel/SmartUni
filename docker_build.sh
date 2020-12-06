docker build -t cps-smartuni-temperaturesensor0 --build-arg sensor_value=0 -f ./hu/bme/mit/cps/smartuni/temperaturesensor/Dockerfile .
docker build -t cps-smartuni-temperaturesensor1 --build-arg sensor_value=1 -f ./hu/bme/mit/cps/smartuni/temperaturesensor/Dockerfile .
docker build -t cps-smartuni-timetablesource -f ./hu/bme/mit/cps/smartuni/timetable/Dockerfile .
docker build -t cps-smartuni-windowsensor -f ./hu/bme/mit/cps/smartuni/windowsensor/Dockerfile .
docker build -t cps-smartuni-temperaturevalidator -f ./hu/bme/mit/cps/smartuni/temperaturevalidator/Dockerfile .
docker build -t cps-smartuni-firealarm -f ./hu/bme/mit/cps/smartuni/firealarm/Dockerfile .
docker build -t cps-smartuni-controller -f ./hu/bme/mit/cps/smartuni/controller/Dockerfile .
docker build -t cps-smartuni-decisionlogic -f ./hu/bme/mit/cps/smartuni/decisionlogic/Dockerfile .
