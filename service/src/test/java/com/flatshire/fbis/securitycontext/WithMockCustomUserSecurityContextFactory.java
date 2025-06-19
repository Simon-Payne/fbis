package com.flatshire.fbis.securitycontext;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collection;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomUserDetails principal =
                new CustomUserDetails(customUser.name(), customUser.username(), customUser.credential());
        Authentication auth =
                UsernamePasswordAuthenticationToken.authenticated(principal, customUser.credential(), principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }

    class CustomUserDetails implements UserDetails {

        private final String userFriendlyName;
        private final String userName;
        private final String password;

        public CustomUserDetails(String userFriendlyName, String userName, String password) {
            this.userFriendlyName = userFriendlyName;
            this.userName = userName;
            this.password = password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return userName;
        }

        public String getUserFriendlyName() {
            return userFriendlyName;
        }
    }
}
