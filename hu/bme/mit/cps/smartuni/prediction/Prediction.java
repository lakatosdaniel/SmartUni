package hu.bme.mit.cps.smartuni.prediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;

import hu.bme.mit.cps.smartuni.PredictedTemperature;
import hu.bme.mit.cps.smartuni.PredictedTemperatureDataWriter;
import hu.bme.mit.cps.smartuni.PredictedTemperatureTypeSupport;
import hu.bme.mit.cps.smartuni.PredictionKind;
import hu.bme.mit.cps.smartuni.Temperature;
import hu.bme.mit.cps.smartuni.TemperatureDataReader;
import hu.bme.mit.cps.smartuni.TemperatureSeq;
import hu.bme.mit.cps.smartuni.TemperatureTypeSupport;

public class Prediction {
	private static String API_KEY = "ac433992054cfd71b3a1cd5e753a6ba5";
    private static String LOCATION = "Lágymányos"; //Location of BME
	// -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

	public static Map<String,Object> jsonToMap(String str){
	    Map<String,Object> map = new Gson().fromJson(str,new TypeToken<HashMap<String,Object>> () {}.getType());
	    return map;
	}
	
	public static double getOutsideTemperature(String api_key, String location) {
		String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + api_key + "&units=metric";
        double temp = 200;
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader (conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null){
                result.append(line);
            }
            rd.close();
            Map<String, Object > respMap = jsonToMap(result.toString());
            Map<String, Object > mainMap = jsonToMap(respMap.get("main").toString());
            temp = (double) mainMap.get("temp");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        
        return temp;
	}
	
    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */

        // --- Run --- //
        subscriberMain(domainId, sampleCount);
    }

    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    private static Temperature insideTemperature = null;
    
/*    private static void printData() {
		System.out.print("Temperature " + insideTemperature.toString());
	}*/
    
    private static PredictedTemperature predict() {
    	PredictedTemperature prediction = new PredictedTemperature();
    	double outsideTemp = getOutsideTemperature(API_KEY, LOCATION);
    	
    	if (insideTemperature != null) {
    		if(outsideTemp == 200) {
    			System.out.println("Error, invalid API data.");
    		}
    		else {
    			if(Math.abs(insideTemperature.Temperature - outsideTemp) < 5.0) {
        			prediction.Prediction = PredictionKind.STAGNATE;
        		} else if(outsideTemp > insideTemperature.Temperature) {
        			prediction.Prediction = PredictionKind.INCREASE;
        		} else {
        			prediction.Prediction = PredictionKind.DECREASE;
        		}
        		prediction.TimeStamp = insideTemperature.TimeStamp;
    		}
    	}
    	else {
    		System.out.println("Warning, inside temperature data is not available.");
    		if (outsideTemp > 30) {
    			prediction.Prediction = PredictionKind.INCREASE;
    		} 
    		else if(outsideTemp < 10) {
    			prediction.Prediction = PredictionKind.DECREASE;
    		}
    		else {
    			prediction.Prediction = PredictionKind.STAGNATE;
    		}
    	}
    	return prediction;
    }
    
    // --- Constructors: -----------------------------------------------------

    private Prediction() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Publisher publisher = null;
        Topic temperatureTopic = null;
        Topic predictionTopic = null;
        TemperatureListener temperatureListener = null;
        TemperatureDataReader temperatureReader = null;
        PredictedTemperatureDataWriter predictionWriter = null;
        
        try {

            // --- Create participant --- //

            /* To customize participant QoS, use
            the configuration file
            USER_QOS_PROFILES.xml */

            participant = DomainParticipantFactory.TheParticipantFactory.
            create_participant(
                domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            } 
            
            // --- Create publisher --- //

            /* To customize publisher QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }  
            

            // --- Create subscriber --- //

            /* To customize subscriber QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (subscriber == null) {
                System.err.println("create_subscriber error\n");
                return;
            }     

            // --- Create topic --- //

            /* Register type before creating topic */
            String temperatureTypeName = TemperatureTypeSupport.get_type_name(); 
            TemperatureTypeSupport.register_type(participant, temperatureTypeName);
            String predictionTypeName = PredictedTemperatureTypeSupport.get_type_name(); 
            PredictedTemperatureTypeSupport.register_type(participant, predictionTypeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            temperatureTopic = participant.create_topic(
                "TemperatureTopic",
                temperatureTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (temperatureTypeName == null) {
                System.err.println("create_topic error\n");
                return;
            }
            
            predictionTopic = participant.create_topic(
                "PredictedTemperatureTopic",
                predictionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (predictionTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }

            // --- Create reader --- //

            temperatureListener = new TemperatureListener();

            /* To customize data reader QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            temperatureReader = (TemperatureDataReader)
            subscriber.create_datareader(
                temperatureTopic, Subscriber.DATAREADER_QOS_DEFAULT, temperatureListener,
                StatusKind.STATUS_MASK_ALL);
            if (temperatureReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            
            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            predictionWriter = (PredictedTemperatureDataWriter)
            publisher.create_datawriter(
                predictionTopic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (predictionWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }

            // --- Wait for data --- //
            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;

            final long receivePeriodSec = 5;
            PredictedTemperature instance = null;
            
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

            	instance = predict(); 
            	if (instance != null) {
					System.out.println("Predicted Temperature Change" + instance.toString());
					/* Write data */
	                predictionWriter.write(instance, instance_handle);
				}
            	else {
            		System.out.println("No data received, sleeping for " + receivePeriodSec + " sec...");
            	}
            	
                try {
                    Thread.sleep(receivePeriodSec * 1000);  // in millisec
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }
            
        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Data Distribution Service provides the finalize_instance()
            method for users who want to release memory used by the
            participant factory singleton. Uncomment the following block of
            code for clean destruction of the participant factory
            singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }

    // -----------------------------------------------------------------------
    // Private Types
    // -----------------------------------------------------------------------

    // =======================================================================

    private static class TemperatureListener extends DataReaderAdapter {

        TemperatureSeq _dataSeq = new TemperatureSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            TemperatureDataReader temperatureReader =
            (TemperatureDataReader)reader;

            try {
                temperatureReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        Temperature instance = _dataSeq.get(i);
                        if (instance.SensorID > 999) {
							insideTemperature = instance;
						}
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                temperatureReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
}
