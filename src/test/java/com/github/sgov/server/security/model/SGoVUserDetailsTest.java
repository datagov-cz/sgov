package com.github.sgov.server.security.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.util.Vocabulary;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class SGoVUserDetailsTest {

    @Test
    void constructorInitializesDefaultUserAuthority() {
        final UserAccount user = Generator.generateUserAccount();
        final SGoVUserDetails result = new SGoVUserDetails(user);
        assertEquals(1, result.getAuthorities().size());
        assertTrue(
            result.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.USER.getName())));
    }

    @Test
    void authorityBasedConstructorAddsDefaultAuthority() {
        final Set<GrantedAuthority> authorities =
            Collections.singleton(new SimpleGrantedAuthority("ROLE_MANAGER"));
        final SGoVUserDetails result =
            new SGoVUserDetails(Generator.generateUserAccount(), authorities);
        assertTrue(
            result.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.USER.getName())));
    }

    @Test
    void constructorResolvesAuthoritiesFromUserTypes() {
        final UserAccount user = Generator.generateUserAccount();
        user.addType(Vocabulary.s_c_administrator);
        final SGoVUserDetails result = new SGoVUserDetails(user);
        assertEquals(2, result.getAuthorities().size());
        assertTrue(
            result.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.USER.getName())));
        assertTrue(
            result.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ADMIN.getName())));
    }

    @Test
    void authorityBasedConstructorResolvesAuthoritiesFromUserTypes() {
        final Set<GrantedAuthority> authorities =
            Collections.singleton(new SimpleGrantedAuthority("ROLE_MANAGER"));
        final UserAccount user = Generator.generateUserAccount();
        user.addType(Vocabulary.s_c_administrator);
        final SGoVUserDetails result = new SGoVUserDetails(user, authorities);
        assertEquals(3, result.getAuthorities().size());
        assertTrue(
            result.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.USER.getName())));
        assertTrue(
            result.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ADMIN.getName())));
        assertTrue(result.getAuthorities().containsAll(authorities));
    }

    @Test
    void getUserReturnsCopyOfUser() {
        final UserAccount user = Generator.generateUserAccount();
        final SGoVUserDetails sut = new SGoVUserDetails(user);
        final UserAccount result = sut.getUser();
        assertEquals(user, result);
        assertNotSame(user, result);
    }
}