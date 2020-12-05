package hu.bme.mit.cps.smartuni.optimalization;

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

import hu.bme.mit.cps.smartuni.Action;
import hu.bme.mit.cps.smartuni.ActionDataReader;
import hu.bme.mit.cps.smartuni.ActionDataWriter;
import hu.bme.mit.cps.smartuni.ActionKind;
import hu.bme.mit.cps.smartuni.ActionSeq;
import hu.bme.mit.cps.smartuni.ActionTypeSupport;
import hu.bme.mit.cps.smartuni.PredictedTemperature;
import hu.bme.mit.cps.smartuni.PredictedTemperatureDataReader;
import hu.bme.mit.cps.smartuni.PredictedTemperatureSeq;
import hu.bme.mit.cps.smartuni.PredictedTemperatureTypeSupport;
import hu.bme.mit.cps.smartuni.PredictionKind;
import hu.bme.mit.cps.smartuni.Temperature;
import hu.bme.mit.cps.smartuni.TemperatureDataReader;
import hu.bme.mit.cps.smartuni.TemperatureSeq;
import hu.bme.mit.cps.smartuni.TemperatureTypeSupport;

public class Optimalization {
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

    private static Temperature temperature = null;
    private static Temperature lastTemperature = null;
    private static Action recommendedAction = null;
    private static PredictedTemperature predictedTemperature = null;
    
    /*private static void printData() {
		System.out.print("Temperature " + temperature.toString() + "\nRecommendedAction " + recommendedAction.toString() + "\nPredictedTemperature " + predictedTemperature.toString());
	}*/
    
    private static Action optimize() {
    	Action action = null;
    	if (temperature != null && recommendedAction != null && predictedTemperature != null) {
    		action = new Action();
    		action.TimeStamp = Math.max(temperature.TimeStamp, Math.max(recommendedAction.TimeStamp, predictedTemperature.TimeStamp));
    		if(recommendedAction.Action == ActionKind.STOP) {
    			action.Action = ActionKind.STOP;
    		} 
    		else if(recommendedAction.Action == ActionKind.COOL) {
    			if(predictedTemperature.Prediction == PredictionKind.DECREASE) {
    				if(lastTemperature == null || temperature.Temperature <= lastTemperature.Temperature) {
    					action.Action = ActionKind.STOP;
    				} 
    				else {
    					action.Action = ActionKind.COOL;
    				}
    			} 
    			else if(predictedTemperature.Prediction == PredictionKind.STAGNATE || predictedTemperature.Prediction == PredictionKind.INCREASE) {
    				action.Action = ActionKind.COOL;
    			} 
    			else {
    				System.out.println("Error, unable to optimize data.");
    			}
    		} 
    		else if(recommendedAction.Action == ActionKind.HEAT) {
    			if(predictedTemperature.Prediction == PredictionKind.INCREASE) {
    				if(lastTemperature == null || temperature.Temperature >= lastTemperature.Temperature) {
    					action.Action = ActionKind.STOP;
    				} 
    				else {
    					action.Action = ActionKind.HEAT;
    				}
    			} 
    			else if(predictedTemperature.Prediction == PredictionKind.STAGNATE || predictedTemperature.Prediction == PredictionKind.DECREASE) {
    				action.Action = ActionKind.HEAT;
    			} 
    			else {
    				System.out.println("Error, unable to optimize data.");
    			}
    		} 
    		else {
    			System.out.println("Error, unable to optimize data.");
    		}
		}
    	lastTemperature = temperature;
    	return action;
    }
    
    // --- Constructors: -----------------------------------------------------

    private Optimalization() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Publisher publisher = null;
        Topic temperatureTopic = null;
        Topic recommendedActionTopic = null;
        Topic predictedTemperatureTopic = null;
        Topic actionTopic = null;
        TemperatureListener temperatureListener = null;
        RecommendedActionListener recommendedActionListener = null;
        PredictedTemperatureListener predictedTemperatureListener = null;
        TemperatureDataReader temperatureReader = null;
        ActionDataReader recommendedActionReader = null;
        PredictedTemperatureDataReader predictedTemperatureReader = null;
        ActionDataWriter actionWriter = null;
        
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
            String recommendedActionTypeName = ActionTypeSupport.get_type_name(); 
            ActionTypeSupport.register_type(participant, recommendedActionTypeName);
            String predictedTemperatureTypeName = PredictedTemperatureTypeSupport.get_type_name(); 
            PredictedTemperatureTypeSupport.register_type(participant, predictedTemperatureTypeName);
            String actionTypeName = ActionTypeSupport.get_type_name(); //TODO: Kell ez 2x?
            ActionTypeSupport.register_type(participant, actionTypeName);

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
            
            recommendedActionTopic = participant.create_topic(
                "RecommendedActionTopic",
                recommendedActionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (recommendedActionTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }
            
            predictedTemperatureTopic = participant.create_topic(
                "PredictedTemperatureTopic",
                predictedTemperatureTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (predictedTemperatureTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }
            
            actionTopic = participant.create_topic(
                "ActionTopic",
                actionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (actionTopic == null) {
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
            
            recommendedActionListener = new RecommendedActionListener();
            
            recommendedActionReader = (ActionDataReader)
            subscriber.create_datareader(
                recommendedActionTopic, Subscriber.DATAREADER_QOS_DEFAULT, recommendedActionListener,
                StatusKind.STATUS_MASK_ALL);
            if (recommendedActionReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            
            predictedTemperatureListener = new PredictedTemperatureListener();
            
            predictedTemperatureReader = (PredictedTemperatureDataReader)
            subscriber.create_datareader(
                predictedTemperatureTopic, Subscriber.DATAREADER_QOS_DEFAULT, predictedTemperatureListener,
                StatusKind.STATUS_MASK_ALL);
            if (predictedTemperatureReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            
            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            actionWriter = (ActionDataWriter)
            publisher.create_datawriter(
                actionTopic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (actionWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }

            // --- Wait for data --- //
            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;

            final long receivePeriodSec = 5;
            Action instance = null;
            
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

            	instance = optimize(); 
            	if (instance != null) {
					System.out.println("Final Action" + instance.toString());
					/* Write data */
	                actionWriter.write(instance, instance_handle);
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
							temperature = instance;
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
    
    private static class RecommendedActionListener extends DataReaderAdapter {

        ActionSeq _dataSeq = new ActionSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            ActionDataReader recommendedActionReader =
            (ActionDataReader)reader;

            try {
            	recommendedActionReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        Action instance = _dataSeq.get(i);
						recommendedAction = instance;
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	recommendedActionReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
    
    private static class PredictedTemperatureListener extends DataReaderAdapter {

        PredictedTemperatureSeq _dataSeq = new PredictedTemperatureSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            PredictedTemperatureDataReader predictedTemperatureReader =
            (PredictedTemperatureDataReader)reader;

            try {
            	predictedTemperatureReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        PredictedTemperature instance = _dataSeq.get(i);
						predictedTemperature = instance;
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	predictedTemperatureReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
}
