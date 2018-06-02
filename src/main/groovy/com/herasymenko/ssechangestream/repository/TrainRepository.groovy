package com.herasymenko.ssechangestream.repository

import com.herasymenko.ssechangestream.domain.Train
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * Talks to the {@link Train} collection in the database.
 */
interface TrainRepository extends MongoRepository<Train, String> { }
