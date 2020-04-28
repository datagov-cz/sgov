package com.github.sgov.server.service.security;

import com.github.sgov.server.event.LoginAttemptsThresholdExceeded;
import com.github.sgov.server.event.LoginFailureEvent;
import com.github.sgov.server.event.LoginSuccessEvent;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.SecurityConstants;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Tracks unsuccessful login attempts, but does not store the information in any persistent
 * storage.
 *
 * <p>This means that when the application is shut down, the data about login attempts are lost.
 *
 * <p>When a threshold ({@link SecurityConstants#MAX_LOGIN_ATTEMPTS}) is reached, an event is
 * published for the application. A successful login resets the counter of unsuccessful attempts for
 * the user.
 */
public class RuntimeBasedLoginTracker implements LoginTracker, ApplicationEventPublisherAware {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeBasedLoginTracker.class);
    private final Map<URI, AtomicInteger> counter = new ConcurrentHashMap<>();
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void onLoginFailure(LoginFailureEvent event) {
        final UserAccount user = event.getUser();
        if (!counter.containsKey(user.getUri())) {
            counter.putIfAbsent(user.getUri(), new AtomicInteger());
        }
        final AtomicInteger cnt = counter.get(user.getUri());
        final int attempts = cnt.incrementAndGet();
        if (attempts > SecurityConstants.MAX_LOGIN_ATTEMPTS) {
            emitThresholdExceeded(user);
        }
    }

    private void emitThresholdExceeded(UserAccount user) {
        if (counter.get(user.getUri()).get() > SecurityConstants.MAX_LOGIN_ATTEMPTS + 1) {
            // Do not emit multiple times
            return;
        }
        LOG.warn("Unsuccessful login attempts limit exceeded by user {}. Locking the account.",
            user);
        eventPublisher.publishEvent(new LoginAttemptsThresholdExceeded(user));
    }

    @Override
    public void onLoginSuccess(LoginSuccessEvent event) {
        final UserAccount user = event.getUser();
        Objects.requireNonNull(user);
        counter.computeIfPresent(user.getUri(), (uri, attempts) -> {
            attempts.set(0);
            return attempts;
        });
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
