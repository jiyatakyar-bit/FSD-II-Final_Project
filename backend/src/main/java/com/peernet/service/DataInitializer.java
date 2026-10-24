package com.peernet.service;

import com.peernet.model.Answer;
import com.peernet.model.AppUser;
import com.peernet.model.Doubt;
import com.peernet.model.DoubtStatus;
import com.peernet.model.HelperExpertise;
import com.peernet.model.Role;
import com.peernet.model.Subject;
import com.peernet.repository.AnswerRepository;
import com.peernet.repository.DoubtRepository;
import com.peernet.repository.HelperExpertiseRepository;
import com.peernet.repository.SubjectRepository;
import com.peernet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Creates demo data on first start: subjects, an admin, a mentor, a few students and two solved doubts. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final HelperExpertiseRepository expertiseRepository;
    private final DoubtRepository doubtRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        List<Subject> subjects = new ArrayList<>();
        for (String name : List.of("Data Structures & Algorithms", "DBMS", "Operating Systems", "Computer Networks",
                "Java", "Web Development", "Python", "Mathematics")) {
            Subject s = new Subject();
            s.setName(name);
            subjects.add(subjectRepository.save(s));
        }

        createUser("Admin", "admin@peernet.com", adminPassword, Role.ADMIN, 0);
        createUser("Prof. Mehta (Mentor)", "mentor@peernet.com", "Mentor@123", Role.MENTOR, 300);
        AppUser student = createUser("Student", "student@peernet.com", "Student@123", Role.STUDENT, 140);
        AppUser diya = createUser("Diya Verma", "diya@peernet.com", "Student@123", Role.STUDENT, 90);
        AppUser rohan = createUser("Rohan Singh", "rohan@peernet.com", "Student@123", Role.STUDENT, 40);
        AppUser meera = createUser("Meera Nair", "meera@peernet.com", "Student@123", Role.STUDENT, 15);

        declare(student, subjects.get(0));
        declare(student, subjects.get(4));
        declare(diya, subjects.get(1));
        declare(diya, subjects.get(5));
        declare(rohan, subjects.get(2));
        declare(rohan, subjects.get(3));
        declare(meera, subjects.get(6));

        solved(rohan, student, subjects.get(0), "How does a HashMap handle collisions?",
                "I know hashing maps keys to buckets, but what happens when two keys land in the same bucket?",
                "hashmap,hashing,collision",
                "Each bucket stores a chain (linked list, or a balanced tree in Java 8+ when the chain grows long). "
                        + "On a collision the new entry is added to the chain, and get() walks the chain using equals() to find the key.");
        solved(meera, diya, subjects.get(1), "Difference between primary key and unique key?",
                "Both seem to stop duplicate values. When should I use which?",
                "sql,keys",
                "A table can have only one primary key and it cannot be NULL. A table can have many unique keys and "
                        + "(in most databases) they may contain NULL. Use the primary key to identify a row and unique keys for other columns that must not repeat, like email.");
    }

    private AppUser createUser(String name, String email, String password, Role role, int reputation) {
        AppUser u = new AppUser();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        u.setReputation(reputation);
        return userRepository.save(u);
    }

    private void declare(AppUser user, Subject subject) {
        HelperExpertise e = new HelperExpertise();
        e.setUser(user);
        e.setSubject(subject);
        e.setDeclared(true);
        expertiseRepository.save(e);
    }

    private void solved(AppUser asker, AppUser helper, Subject subject, String title, String description,
                        String tags, String answerText) {
        Doubt d = new Doubt();
        d.setAsker(asker);
        d.setSubject(subject);
        d.setTitle(title);
        d.setDescription(description);
        d.setTags(tags);
        d.setAnonymous(true);
        d.setStatus(DoubtStatus.RESOLVED);
        d.setFirstAnsweredAt(LocalDateTime.now());
        d.setResolvedAt(LocalDateTime.now());
        d = doubtRepository.save(d);
        Answer a = new Answer();
        a.setDoubt(d);
        a.setHelper(helper);
        a.setContent(answerText);
        a.setAccepted(true);
        a.setVotes(3);
        answerRepository.save(a);
    }
}
