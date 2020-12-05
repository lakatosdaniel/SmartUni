/*
* (c) Copyright, Real-Time Innovations, 2012.  All rights reserved.
* RTI grants Licensee a license to use, modify, compile, and create derivative
* works of the software solely for use with RTI Connext DDS. Licensee may
* redistribute copies of the software provided that all such copies are subject
* to this license. The software is provided "as is", with no warranty of any
* type, including any warranty for fitness for any purpose. RTI is under no
* obligation to maintain or support the software. RTI shall not be liable for
* any incidental or consequential damages arising out of the use or inability
* to use the software.
*/

package hu.bme.mit.cps.smartuni.windowsensor;

import java.util.Calendar;
import java.util.Random;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;

import hu.bme.mit.cps.smartuni.WindowState;
import hu.bme.mit.cps.smartuni.WindowStateDataWriter;
import hu.bme.mit.cps.smartuni.WindowStateTypeSupport;

// ===========================================================================

public class WindowSensor {
	private static int number = 0;
	
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
    	if (args.length >= 1) {
            number = Integer.valueOf(args[0]).intValue();
        }
    	
        // --- Get domain ID --- //
        int domainId = 0;
        /*if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }*/

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        /*if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }-/

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */

        // --- Run --- //
        publisherMain(domainId, sampleCount);
    }

    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    // --- Constructors: -----------------------------------------------------

    private WindowSensor() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        WindowStateDataWriter writer = null;
        
        System.out.println("Starting WindowStatePublisher...");
        
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

            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = WindowStateTypeSupport.get_type_name();
            WindowStateTypeSupport.register_type(participant, typeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            topic = participant.create_topic(
                "WindowStateTopic",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           

            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            writer = (WindowStateDataWriter)
            publisher.create_datawriter(
                topic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            // --- Write --- //

            /* Create data sample for writing */
            WindowState instance = new WindowState();
            
            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            /* For a data type that has a key, if the same instance is going to be
            written multiple times, initialize the key here
            and register the keyed instance prior to writing */
            //instance_handle = writer.register_instance(instance);
            
            Random random = new Random();
            Boolean isOpen = random.nextBoolean();
            
            final long sendPeriodMillis = 5 * 1000; // 5 seconds
            
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {

                /* Modify the instance to be written here */
            	
            	if (count % 10 == 0) {
            		isOpen = random.nextBoolean();
            	}

                instance.SensorID = number;
                instance.IsOpen = isOpen;
                instance.TimeStamp = Calendar.getInstance().getTimeInMillis();                
                
                System.out.println("Writing WindowState" + instance.toString());
                
                /* Write data */
                writer.write(instance, instance_handle);
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }

            //writer.unregister_instance(instance, instance_handle);

        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Data Distribution Service provides finalize_instance()
            method for people who want to release memory used by the
            participant factory singleton. Uncomment the following block of
            code for clean destruction of the participant factory
            singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
}