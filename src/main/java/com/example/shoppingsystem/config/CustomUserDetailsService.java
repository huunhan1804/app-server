package com.example.shoppingsystem.config;

import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.repositories.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Account account;
        if (Regex.isValidEmail(loginId)) {
            account = accountRepository.findByEmail(loginId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return account;
        } else if (Regex.isValidPhoneNumber(loginId)) {
            account = accountRepository.findByPhone(loginId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return account;
        }
        return accountRepository.findByUsername(loginId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
