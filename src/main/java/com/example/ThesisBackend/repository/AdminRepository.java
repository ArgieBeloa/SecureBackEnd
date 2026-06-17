package com.example.ThesisBackend.repository;

import com.example.ThesisBackend.Model.AdminModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<AdminModel, String> {
}
