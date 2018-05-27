package com.herasymenko.ssechangestream.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

/**
 * Contains information about train state.
 */
@Document
@Canonical
@EqualsAndHashCode( excludes = ['id', 'version'] )
class Train {

    /**
     * The required mongo document id.
     */
    @Id
    @Field( FieldNames.ID )
    String id

    /**
     * A version field used to implement optimistic locking on entities.
     */
    @Version
    @Field( FieldNames.VERSION )
    Long version

    /**
     * The train unique identifier - its number.
     */
    @Indexed
    @Field( FieldNames.TRAIN_NUMBER )
    String trainNumber

    /**
     * The train current station.
     */
    @Field( FieldNames.CURRENT_STATION )
    String currentStation

    /**
     * The time when train arrived at current station.
     */
    @Field( FieldNames.DATE_CODE )
    long dateCode

    /**
     * The train start station.
     */
    @Field( FieldNames.START_STATION )
    String startStation

    /**
     * The train end station.
     */
    @Field( FieldNames.END_STATION )
    String endStation

    @Immutable
    static final class FieldNames {
        public static final String ID = '_id'
        public static final String VERSION = 'version'
        public static final String TRAIN_NUMBER = 'number'
        public static final String CURRENT_STATION = 'current-station'
        public static final String START_STATION = 'start-station'
        public static final String END_STATION = 'end-station'
        public static final String DATE_CODE = 'date-code'
    }
}
