package com.demo.service;

import com.demo.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public UserService() {
        save(new User(null, "admin",   "admin@demo.com",   "hashed_pw_1", "ADMIN"));
        save(new User(null, "alice",   "alice@demo.com",   "hashed_pw_2", "USER"));
        save(new User(null, "bob",     "bob@demo.com",     "hashed_pw_3", "USER"));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idSeq.getAndIncrement());
        }
        store.put(user.getId(), user);
        logger.info("Saved user id={}", user.getId());
        return user;
    }
}
