package hu.bme.mit.cps.smartuni.visualization;


import java.util.ArrayList;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;

import hu.bme.mit.cps.smartuni.Action;
import hu.bme.mit.cps.smartuni.ActionDataReader;
import hu.bme.mit.cps.smartuni.ActionSeq;
import hu.bme.mit.cps.smartuni.ActionTypeSupport;
import hu.bme.mit.cps.smartuni.PredictedTemperature;
import hu.bme.mit.cps.smartuni.PredictedTemperatureDataReader;
import hu.bme.mit.cps.smartuni.PredictedTemperatureSeq;
import hu.bme.mit.cps.smartuni.PredictedTemperatureTypeSupport;
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
import hu.bme.mit.cps.smartuni.WindowStateSeq;
import hu.bme.mit.cps.smartuni.WindowStateTypeSupport;
import hu.bme.mit.cps.smartuni.prediction.Prediction;

// ===========================================================================

public class Visualization {
	private static String API_KEY = "ac433992054cfd71b3a1cd5e753a6ba5";
    private static String LOCATION = "Lágymányos"; //Location of BME
	private static DatabaseHandler dbHandler = new DatabaseHandler();
	
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
    private static PredictedTemperature predictedTemperature = null;
    private static Action recommendedAction = null;
    private static Action action = null;
    private static TimeTable timetable = null;
    private static WindowState windowstate = null;
    
    
    /*private static void initData() {
    	temperature = null;
    	predictedTemperature = null;
    	recommendedAction = null;
    	action = null;
    	timetable = null;
    	windowstate = null;
    }
    
    private static void printData() {
		System.out.print("Temperature " + temperature.toString() + "\nTimeTable " + timetable.toString() + "\nWindowState " + windowstate.toString());
	}*/
    
    private static boolean writeToDB() {
    	if (temperature != null && predictedTemperature != null && recommendedAction != null 
    			&& action != null && timetable != null && windowstate != null) {
    		
    		ArrayList<Long> timestamps = new ArrayList<Long>();
    		timestamps.add(temperature.TimeStamp);
    		timestamps.add(predictedTemperature.TimeStamp);
    		timestamps.add(recommendedAction.TimeStamp);
    		timestamps.add(action.TimeStamp);
    		timestamps.add(timetable.TimeStamp);
    		timestamps.add(windowstate.TimeStamp);
    		
    		for (long timestamp : timestamps) {
    			for (long timestamp_other : timestamps) {
    				if (Math.abs((timestamp/1000)-(timestamp_other/1000)) > 10) {
    					System.out.println("Warning, mismatch in received data timestamps.");
    					return false;
    				}
    			}
    		}
    			
			String windowState = "Closed";
			if (windowstate.IsOpen) {
				windowState = "Open";
			}
			
			String lectureState = "No";
			if (timetable.Lecture) {
				lectureState = "Yes";
			}
			
			float outsideTemperature = (float)Prediction.getOutsideTemperature(API_KEY, LOCATION);
			
			dbHandler.addData(temperature.Temperature, outsideTemperature, 
					predictedTemperature.Prediction.toString(), windowState, lectureState, 
					recommendedAction.Action.toString(), action.Action.toString());
			
    		return true;
    	}
    	
    	return false;
    }
    
    // --- Constructors: -----------------------------------------------------

    private Visualization() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Topic temperatureTopic = null;
        Topic timetableTopic = null;
        Topic windowstateTopic = null;
        Topic recommendedActionTopic = null;
        Topic actionTopic = null;
        Topic predictedTemperatureTopic = null;
        
        TemperatureListener temperatureListener = null;
        TimeTableListener timetableListener = null;
        WindowStateListener windowstateListener = null;
        RecommendedActionListener recommendedActionListener = null;
        ActionListener actionListener = null;
        PredictedTemperatureListener predictedTemperatureListener = null;
        
        TemperatureDataReader temperatureReader = null;
        TimeTableDataReader timetableReader = null;
        WindowStateDataReader windowstateReader = null;
        ActionDataReader recommendedActionReader = null;
        ActionDataReader actionReader = null;
        PredictedTemperatureDataReader predictedTemperatureReader = null;
        
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

            // --- Create topic --- //

            /* Register type before creating topic */
            String temperatureTypeName = TemperatureTypeSupport.get_type_name(); 
            TemperatureTypeSupport.register_type(participant, temperatureTypeName);
            String timetableTypeName = TimeTableTypeSupport.get_type_name(); 
            TimeTableTypeSupport.register_type(participant, timetableTypeName);
            String windowstateTypeName = WindowStateTypeSupport.get_type_name(); 
            WindowStateTypeSupport.register_type(participant, windowstateTypeName);
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
            
            recommendedActionListener = new RecommendedActionListener();
            
            recommendedActionReader = (ActionDataReader)
            subscriber.create_datareader(
                recommendedActionTopic, Subscriber.DATAREADER_QOS_DEFAULT, recommendedActionListener,
                StatusKind.STATUS_MASK_ALL);
            if (recommendedActionReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            
            actionListener = new ActionListener();
            
            actionReader = (ActionDataReader)
            subscriber.create_datareader(
                actionTopic, Subscriber.DATAREADER_QOS_DEFAULT, actionListener,
                StatusKind.STATUS_MASK_ALL);
            if (actionReader == null) {
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

            // --- Wait for data --- //
            final long receivePeriodSec = 5;
            
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

            	if (writeToDB()) {
					System.out.println("Data written to DataBase.");
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
						timetable = instance;
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
						windowstate = instance;
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	windowStateReader.return_loan(_dataSeq, _infoSeq);
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
    
    private static class ActionListener extends DataReaderAdapter {

        ActionSeq _dataSeq = new ActionSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            ActionDataReader actionReader =
            (ActionDataReader)reader;

            try {
            	actionReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        Action instance = _dataSeq.get(i);
						action = instance;
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	actionReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
    
}

