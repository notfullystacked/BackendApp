package org.example.smartbiobackend.config;


import org.example.smartbiobackend.model.User;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitData implements Runnable {
    @Override
    public void run() {

        // Steps:
        // Create x Users
        // Pick a seat in a booking
        // Verify that the seat is reserved by user
        User user = new User();

    }
}
