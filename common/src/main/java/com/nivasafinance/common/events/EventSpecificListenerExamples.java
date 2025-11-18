package com.nivasafinance.common.events;



import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;

import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Component;



/**

 * Examples of how to create event-specific listeners.

 * 

 * <p>This file contains examples only - delete or move these examples to your actual listener classes.</p>

 * 

 * <h3>How to Create Event-Specific Listeners:</h3>

 * 

 * <p>Use Spring's conditional event listening with SpEL expressions to filter events.

 * The condition is evaluated BEFORE the listener method is invoked, so it's very efficient.</p>

 */

@Slf4j

@Component

public class EventSpecificListenerExamples {



    /**

     * Example 1: Listen to ONLY LEAD_CREATED events

     * 

     * <p>Use the condition to check the event type string value.</p>

     */

    @EventListener(condition = "#event.eventType == 'LEAD_CREATED'")

    @Async("eventTaskExecutor")

    public void handleOnlyLeadCreated(SystemEvent<?> event) {

        log.info("Processing LEAD_CREATED event only: {}", event);

        // Your business logic here

    }



    /**

     * Example 2: Listen to multiple specific event types

     * 

     * <p>Use OR conditions to listen to multiple event types.</p>

     */

    @EventListener(condition = "#event.eventType == 'LEAD_CREATED' or #event.eventType == 'LOAN_APPROVED'")

    @Async("eventTaskExecutor")

    public void handleMultipleSpecificEvents(SystemEvent<?> event) {

        log.info("Processing LEAD_CREATED or LOAN_APPROVED event: {}", event);

        // Your business logic here

    }



    /**

     * Example 3: Listen to events based on payload type

     * 

     * <p>Check if the payload is an instance of a specific class.</p>

     */

    @EventListener(condition = "#event.payload instanceof T(com.nivasafinance.common.events.payload.LeadCreationEventPayload)")

    @Async("eventTaskExecutor")

    public void handleLeadPayloadEvents(SystemEvent<?> event) {

        log.info("Processing event with LeadCreationEventPayload: {}", event);

        // Your business logic here

    }



    /**

     * Example 4: Listen to events using BusinessEvent enum directly

     * 

     * <p>Compare against the enum's string representation (eventType is stored as String).</p>

     */

    @EventListener(condition = "#event.eventType == T(com.nivasafinance.common.events.BusinessEvent).LEAD_CREATED.toString()")

    @Async("eventTaskExecutor")

    public void handleLeadCreatedWithEnum(SystemEvent<?> event) {

        log.info("Processing LEAD_CREATED event using enum comparison: {}", event);

        // Your business logic here

    }



    /**

     * Example 5: Listen to all events EXCEPT a specific one

     * 

     * <p>Use NOT condition to exclude specific events.</p>

     */

    @EventListener(condition = "#event.eventType != 'LEAD_CREATED'")

    @Async("eventTaskExecutor")

    public void handleAllExceptLeadCreated(SystemEvent<?> event) {

        log.info("Processing all events except LEAD_CREATED: {}", event);

        // Your business logic here

    }



    /**

     * Example 6: Listen to events with custom condition logic

     * 

     * <p>You can use more complex SpEL expressions for custom filtering.</p>

     */

    @EventListener(condition = "#event.eventType.startsWith('LEAD_')")

    @Async("eventTaskExecutor")

    public void handleAllLeadEvents(SystemEvent<?> event) {

        log.info("Processing all LEAD_* events: {}", event);

        // Your business logic here

    }

}

