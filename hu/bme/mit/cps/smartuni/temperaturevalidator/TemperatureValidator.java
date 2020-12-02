package hu.bme.mit.cps.smartuni.temperaturevalidator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

import hu.bme.mit.cps.smartuni.Temperature;
import hu.bme.mit.cps.smartuni.TemperatureDataReader;
import hu.bme.mit.cps.smartuni.TemperatureDataWriter;
import hu.bme.mit.cps.smartuni.TemperatureSeq;
import hu.bme.mit.cps.smartuni.TemperatureTypeSupport;

// ===========================================================================

public class TemperatureValidator {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

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

    private static ArrayList<Temperature> data;
    
    private static ArrayList<Temperature> initData() {
    	Temperature instance0 = new Temperature();
		instance0.TimeStamp = 0;
		instance0.SensorID = -1;
		instance0.Temperature = 0;
		
		Temperature instance1 = new Temperature();
		instance1.TimeStamp = 0;
		instance1.SensorID = -1;
		instance1.Temperature = 0;
		
		ArrayList<Temperature> list = new ArrayList<Temperature>(2);
		list.add(instance0);
		list.add(instance1);
    	
    	return list;
	}
    
    private static void printData() {
    	
    	System.out.println("-----------------------------------");
    	
		for(int i = 0; i < data.size(); i++) {
			System.out.println(data.get(i).toString());
		}
	}
        
    private static Temperature validate() {
    	int n = 0;
    	Temperature instance = null;
    	
    	
		for (int i = 0; i < data.size(); i++) {
			if(data.get(i).SensorID == i) {
				n++;
			}
		}
		
		if(n > 1) {
			// TODO: How to do this?
			if (data.get(0).valueEquals(data.get(1))) {
				instance = new Temperature();
				instance.SensorID = 1000;
				instance.Temperature = Math.max(data.get(0).Temperature, data.get(1).Temperature);
				instance.TimeStamp = Math.max(data.get(0).TimeStamp, data.get(1).TimeStamp);
				data = initData();
				
			}
		}
    	
    	return instance;
	}
    
    // --- Constructors: -----------------------------------------------------

    private TemperatureValidator() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Publisher publisher = null;
        Topic topic = null;
        DataReaderListener listener = null;
        TemperatureDataReader reader = null;
        TemperatureDataWriter writer = null;

        data = initData();
        
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

            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = TemperatureTypeSupport.get_type_name(); 
            TemperatureTypeSupport.register_type(participant, typeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            topic = participant.create_topic(
                "TemperatureTopic",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }    
            
            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */
            
            writer = (TemperatureDataWriter)
            publisher.create_datawriter(
                topic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }

            // --- Create reader --- //

            listener = new TemperatureListener();

            /* To customize data reader QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            reader = (TemperatureDataReader)
            subscriber.create_datareader(
                topic, Subscriber.DATAREADER_QOS_DEFAULT, listener,
                StatusKind.STATUS_MASK_ALL);
            if (reader == null) {
                System.err.println("create_datareader error\n");
                return;
            }                         

            // --- Wait for data --- //
            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            final long receivePeriodSec = 4;

            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {
                //System.out.println("Power subscriber sleeping for "
                //+ receivePeriodSec + " sec...");

                Temperature valid = validate();
                
                if(valid != null) {
                	System.out.println("Valid Temperature" + valid.toString());
                	writer.write(valid, instance_handle);
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
            TemperatureDataReader currentReader =
            (TemperatureDataReader)reader;

            try {
                currentReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        Temperature instance = new Temperature(_dataSeq.get(i));
                        
                        if(instance.SensorID < 999) {
                            data.set(instance.SensorID, instance);
                        }
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                currentReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
}

