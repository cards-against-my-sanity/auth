package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.model.Session;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.repo.SessionRepository;
import dev.jacobandersen.cams.auth.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(User user, boolean remember) {
        return createSession(user, remember ? Duration.ofDays(30) : Duration.ofDays(3));
    }

    public Session createSession(final User user, final Duration duration) {
        if (user == null) return null;

        final Session session = new Session(user, duration);
        sessionRepository.save(session);

        user.getSessions().add(session);
        userRepository.save(user);

        return session;
    }

    public boolean endSession(final UUID userId, final UUID sessionId) {
        if (userId == null || sessionId == null) return false;

        final Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || !session.getUser().getId().equals(userId)) return false;

        sessionRepository.delete(session);
        return true;
    }
}
