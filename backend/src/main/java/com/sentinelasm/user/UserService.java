package com.sentinelasm.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public AppUser currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return loadUserByUsername(email);
    }
}
