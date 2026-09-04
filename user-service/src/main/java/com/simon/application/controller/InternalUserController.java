package com.simon.application.controller;

import com.simon.application.dto.response.UserSummaryResponse;
import com.simon.application.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserSummaryResponse getUserSummary(@PathVariable Long id) {
        return userService.getUserSummary(id);
    }
}
