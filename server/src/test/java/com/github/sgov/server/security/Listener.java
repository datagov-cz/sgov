package com.github.sgov.server.security;

import com.github.sgov.server.event.LoginFailureEvent;
import com.github.sgov.server.event.LoginSuccessEvent;
import com.github.sgov.server.model.UserAccount;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    private UserAccount user;

    public UserAccount getUser() {
        return user;
    }

    @EventListener
    public void onSuccess(LoginSuccessEvent event) {
        this.user = event.getUser();
    }

    @EventListener
    public void onFailure(LoginFailureEvent event) {
        this.user = event.getUser();
    }
}