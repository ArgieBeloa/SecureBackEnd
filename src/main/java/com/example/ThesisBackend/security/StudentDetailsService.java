package com.example.ThesisBackend.security;

import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.repository.StudentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StudentDetailsService implements UserDetailsService {

    private final StudentRepository repo;

    public StudentDetailsService(StudentRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String studentNumber) throws UsernameNotFoundException {
        StudentModel s = repo.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found: " + studentNumber));

        // Create a UserDetails. No roles shown here; add if needed.
        return User.builder()
                .username(s.getStudentNumber())
                .password(s.getStudentPassword()) // password must be already BCrypt-hashed in DB
                .authorities("STUDENT")
                .build();
    }
}
