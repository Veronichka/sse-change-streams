package com.herasymenko.ssechangestream.inbound.domain

import groovy.transform.Canonical

/**
 * Contains train detailed information.
 */
@Canonical
class TrainDetail {

    /**
     * The train unique identifier - it's number.
     */
    String trainNumber

    /**
     * The train current station.
     */
    String currentStation

    /**
     * The time when train arrived at current station.
     */
    long dateCode
}
