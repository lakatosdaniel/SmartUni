package hu.bme.mit.cps.smartuni.decisionlogic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

import hu.bme.mit.cps.smartuni.Action;
import hu.bme.mit.cps.smartuni.ActionDataWriter;
import hu.bme.mit.cps.smartuni.ActionKind;
import hu.bme.mit.cps.smartuni.ActionTypeSupport;
import hu.bme.mit.cps.smartuni.Temperature;
import hu.bme.mit.cps.smartuni.TemperatureDataReader;
import hu.bme.mit.cps.smartuni.TemperatureSeq;
import hu.bme.mit.cps.smartuni.TemperatureTypeSupport;
import hu.bme.mit.cps.smartuni.TimeTable;
import hu.bme.mit.cps.smartuni.TimeTableDataReader;
import hu.bme.mit.cps.smartuni.TimeTableSeq;
import hu.bme.mit.cps.smartuni.TimeTableTypeSupport;
import hu.bme.mit.cps.smartuni.WindowState;
import hu.bme.mit.cps.smartuni.WindowStateDataReader;
import hu.bme.mit.cps.smartuni.WindowStateDataWriter;
import hu.bme.mit.cps.smartuni.WindowStateSeq;
import hu.bme.mit.cps.smartuni.WindowStateTypeSupport;

// ===========================================================================

public class DecisionLogic {
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
    private static TimeTable timetable = null;
    private static WindowState windowstate = null;
    
    
    private static void initData() {
    	temperature = null;
    	timetable = null;
    	windowstate = null;
    }
    
    private static void printData() {
		System.out.print("Temperature " + temperature.toString() + "\nTimeTable " + timetable.toString() + "\nWindowState " + windowstate.toString());
	}
    
    private static Action decide() {
    	Action action = null;
    	
    	if (temperature != null && timetable != null && windowstate != null) {
    		
    		// TODO: Not sure if this should be like this...
    		if (Math.abs((temperature.TimeStamp/1000)-(timetable.TimeStamp/1000)) <= 30 &&
    				Math.abs((temperature.TimeStamp/1000)-(windowstate.TimeStamp/1000)) <= 30 &&
    				Math.abs((timetable.TimeStamp/1000)-(windowstate.TimeStamp/1000)) <= 30) {
    			action = new Action();
    		}
    		
    		if (windowstate.IsOpen) {
    			action.Action = ActionKind.STOP;
    		}
    		else {
    			if (timetable.Lecture) {
    				if (temperature.Temperature > 22) {
    					action.Action = ActionKind.COOL;
    				}
    				else if (temperature.Temperature < 16) {
    					action.Action = ActionKind.HEAT;
    				}
    				else {
    					action.Action = ActionKind.STOP;
    				}
    			}
    			else {
    				if (temperature.Temperature > 26) {
    					action.Action = ActionKind.COOL;
    				}
    				else if (temperature.Temperature < 14) {
    					action.Action = ActionKind.HEAT;
    				}
    				else {
    					action.Action = ActionKind.STOP;
    				}
    			}
    		}
    		action.TimeStamp = Math.max(Math.max(temperature.TimeStamp, timetable.TimeStamp), windowstate.TimeStamp);
    		
		}
    	
    	return action;
    }
    
    // --- Constructors: -----------------------------------------------------

    private DecisionLogic() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Publisher publisher = null;
        Topic temperatureTopic = null;
        Topic timetableTopic = null;
        Topic windowstateTopic = null;
        Topic actionTopic = null;
        TemperatureListener temperatureListener = null;
        TimeTableListener timetableListener = null;
        WindowStateListener windowstateListener = null;
        TemperatureDataReader temperatureReader = null;
        TimeTableDataReader timetableReader = null;
        WindowStateDataReader windowstateReader = null;
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
            String timetableTypeName = TimeTableTypeSupport.get_type_name(); 
            TimeTableTypeSupport.register_type(participant, timetableTypeName);
            String windowstateTypeName = WindowStateTypeSupport.get_type_name(); 
            WindowStateTypeSupport.register_type(participant, windowstateTypeName);
            String actionTypeName = ActionTypeSupport.get_type_name(); 
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
            
            timetableTopic = participant.create_topic(
                "TimeTableTopic",
                timetableTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (timetableTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }
            
            windowstateTopic = participant.create_topic(
                "WindowStateTopic",
                windowstateTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (windowstateTopic == null) {
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
            
            timetableListener = new TimeTableListener();
            
            timetableReader = (TimeTableDataReader)
            subscriber.create_datareader(
                timetableTopic, Subscriber.DATAREADER_QOS_DEFAULT, timetableListener,
                StatusKind.STATUS_MASK_ALL);
            if (timetableReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            
            windowstateListener = new WindowStateListener();
            
            windowstateReader = (WindowStateDataReader)
            subscriber.create_datareader(
                windowstateTopic, Subscriber.DATAREADER_QOS_DEFAULT, windowstateListener,
                StatusKind.STATUS_MASK_ALL);
            if (windowstateReader == null) {
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

            final long receivePeriodSec = 4;
            Action instance = null;
            
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

            	instance = decide(); 
            	if (instance != null) {
					System.out.println("Decided Action" + instance.toString());
					/* Write data */
	                actionWriter.write(instance, instance_handle);
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
    
    private static class TimeTableListener extends DataReaderAdapter {

        TimeTableSeq _dataSeq = new TimeTableSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            TimeTableDataReader timeTableReader =
            (TimeTableDataReader)reader;

            try {
            	timeTableReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        TimeTable instance = _dataSeq.get(i);
                        if (instance.SourceID > 999) {
							timetable = instance;
						}
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	timeTableReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
    
    private static class WindowStateListener extends DataReaderAdapter {

        WindowStateSeq _dataSeq = new WindowStateSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            WindowStateDataReader windowStateReader =
            (WindowStateDataReader)reader;

            try {
            	windowStateReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        WindowState instance = _dataSeq.get(i);
                        if (instance.SensorID > 999) {
							windowstate = instance;
						}
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	windowStateReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
    
}

