package com.example.ThesisBackend.repository;

import com.example.ThesisBackend.Model.StudentModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<StudentModel, String> {
    Optional<StudentModel> findByStudentNumber(String studentNumber);
}
