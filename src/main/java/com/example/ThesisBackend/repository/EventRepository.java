package com.example.ThesisBackend.repository;

import com.example.ThesisBackend.Model.EventModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<EventModel, String> {
}
