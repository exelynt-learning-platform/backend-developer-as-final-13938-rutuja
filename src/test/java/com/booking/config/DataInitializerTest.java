package com.booking.config;

import com.booking.model.User;
import com.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void testRunWhenUsersDoNotExist() {
        when(userRepository.findByEmail("admin@booking.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@booking.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        dataInitializer.run();

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void testRunWhenUsersAlreadyExist() {
        when(userRepository.findByEmail("admin@booking.com")).thenReturn(Optional.of(new User()));
        when(userRepository.findByEmail("user@booking.com")).thenReturn(Optional.of(new User()));

        dataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
