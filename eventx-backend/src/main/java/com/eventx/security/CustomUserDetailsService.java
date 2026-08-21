package com.eventx.security;

import com.eventx.entity.User;
import com.eventx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return buildUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        return new CustomUserDetails(user);
    }

    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final Long id;

        public CustomUserDetails(User user) {
            super(user.getEmail(), user.getPassword(), user.isActive(), true, true, true,
                  Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())));
            this.id = user.getId();
        }

        public Long getId() {
            return id;
        }
    }
}

