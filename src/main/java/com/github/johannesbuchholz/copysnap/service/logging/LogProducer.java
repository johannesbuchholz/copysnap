package com.github.johannesbuchholz.copysnap.service.logging;

/**
 * Implementing classes are able to produce messages to one or more accepted {@link LogConsumer} instances.
 */
public interface LogProducer {

    void addConsumer(LogConsumer logConsumer);

}
