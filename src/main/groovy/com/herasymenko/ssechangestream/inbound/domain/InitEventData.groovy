package com.herasymenko.ssechangestream.inbound.domain

import groovy.transform.Canonical

/**
 * Contains the data for init event.
 */
@Canonical
class InitEventData {

    /**
     * The collection of initial train details.
     */
    List<TrainDetail> trainDetails
}
