package com.itechartgroup.telemedpoc.security.controller;

import com.itechartgroup.telemedpoc.exception.ResourceNotFoundException;
import com.itechartgroup.telemedpoc.security.CurrentUser;
import com.itechartgroup.telemedpoc.security.UserPrincipal;
import com.itechartgroup.telemedpoc.security.entity.User;
import com.itechartgroup.telemedpoc.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/me")
    // @RolesAllowed({ "ROLE_DOCTOR", "ROLE_PATIENT" })
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_PATIENT')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
