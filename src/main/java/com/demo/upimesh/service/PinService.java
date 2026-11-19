package com.demo.upimesh.service;

import com.demo.upimesh.model.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Service
@RequiredArgsConstructor
public class PinService {

    private final PasswordEncoder passwordEncoder;

    public String hashPin(String pin){
        return passwordEncoder.encode(pin);
    }

    public boolean verifyPin(String enteredPin, String storedHash) {
        return passwordEncoder.matches(enteredPin, storedHash);
    }
}
