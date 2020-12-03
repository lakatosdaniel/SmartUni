package hu.bme.mit.cps.smartuni.controller;


import java.awt.event.ActionListener;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;

import hu.bme.mit.cps.smartuni.Action;
import hu.bme.mit.cps.smartuni.ActionDataReader;
import hu.bme.mit.cps.smartuni.ActionDataWriter;
import hu.bme.mit.cps.smartuni.ActionKind;
import hu.bme.mit.cps.smartuni.ActionSeq;
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
import hu.bme.mit.cps.smartuni.WindowStateSeq;
import hu.bme.mit.cps.smartuni.WindowStateTypeSupport;

// ===========================================================================

public class Controller {
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

    private static Action finalAction = null;
    private static Action recommendedAction = null;
    
    
    /*private static void initData() {
    	finalAction = null;
    	recommendedAction = null;
    }
    
    private static void printData() {
		System.out.print("Temperature " + temperature.toString() + "\nTimeTable " + timetable.toString() + "\nWindowState " + windowstate.toString());
	}*/
    
    private static void control() {
    	Action basicAction = new Action();
    	basicAction.Action = ActionKind.STOP;
    	
    	if (finalAction != null && recommendedAction != null) {
    		System.out.println("Optimized decision available!");
    		System.out.println("Controller Action" + finalAction.toString());
    	}
    	else if (recommendedAction != null) {
    		System.out.println("Warning, optimized decision unavailable!");
    		System.out.println("Controller Action" + recommendedAction.toString());
    	}
    	else {
    		System.out.println("Error, no data available!");
    		System.out.println("Controller Action" + basicAction.toString());
    	}
    		
    }
    
    // --- Constructors: -----------------------------------------------------

    private Controller() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Topic finalActionTopic = null;
        Topic recommendedActionTopic = null;
        FinalActionListener finalActionListener = null;
        RecommendedActionListener recommendedActionListener = null;
        ActionDataReader finalActionReader = null;
        ActionDataReader recommendedActionReader = null;
        
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
            String actionTypeName = ActionTypeSupport.get_type_name(); 
            ActionTypeSupport.register_type(participant, actionTypeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            finalActionTopic = participant.create_topic(
                "ActionTopic",
                actionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (finalActionTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }
            
            recommendedActionTopic = participant.create_topic(
                "RecommendedActionTopic",
                actionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (recommendedActionTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }

            // --- Create reader --- //

            finalActionListener = new FinalActionListener();

            /* To customize data reader QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            finalActionReader = (ActionDataReader)
            subscriber.create_datareader(
                finalActionTopic, Subscriber.DATAREADER_QOS_DEFAULT, finalActionListener,
                StatusKind.STATUS_MASK_ALL);
            if (finalActionReader == null) {
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

            // --- Wait for data --- //
            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;

            final long receivePeriodSec = 5;
            
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

            	control();
            	
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

    private static class FinalActionListener extends DataReaderAdapter {

        ActionSeq _dataSeq = new ActionSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            ActionDataReader finaActionReader =
            (ActionDataReader)reader;

            try {
            	finaActionReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        Action instance = _dataSeq.get(i);
						finalAction = instance;
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
            	finaActionReader.return_loan(_dataSeq, _infoSeq);
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
    
}

