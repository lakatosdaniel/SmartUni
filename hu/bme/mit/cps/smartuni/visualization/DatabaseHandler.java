package hu.bme.mit.cps.smartuni.visualization;

import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

public class DatabaseHandler {
	private static String InfluxURL = "http://localhost:8086"; //"http://grafana.wlap.eu:8086";
	private static String InfluxUser = "admin";
	private static String InfluxPass = "admin";
	private static String InfluxDBname = "smartuni";
	protected InfluxDB databaseConnection;
	
	public DatabaseHandler() {
		databaseConnection = InfluxDBFactory.connect(InfluxURL, InfluxUser, InfluxPass);
		databaseConnection.setDatabase(InfluxDBname);
		databaseConnection.enableBatch(BatchOptions.DEFAULTS);
	}
	
	public void addData(float insideTemperature, float outsideTemperature, String predictedChange, 
			String windowState, String lectureState, String recommendedAction, String finalAction){
		databaseConnection.write(Point.measurement("smartuni")
        	    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        	    .addField("insideTemperature", insideTemperature)
        	    .addField("outsideTemperature", outsideTemperature)
        	    .addField("predictedChange", predictedChange)
        	    .addField("window", windowState)
        	    .addField("lecture", lectureState)
        	    .addField("recommendedAction", recommendedAction)
        	    .addField("finalAction", finalAction)
        	    .build());
	}
}
