package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.nexfit.repository.TrainerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("trainerDetailsService")
@RequiredArgsConstructor
public class TrainerDetailsServiceImpl implements UserDetailsService {

    private final TrainerRepository trainerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return trainerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Trainer not found with email: " + username));
    }
}
