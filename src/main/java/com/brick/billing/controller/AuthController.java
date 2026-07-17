package com.brick.billing.controller;

import com.brick.billing.controller.dto.LoginRequest;
import com.brick.billing.controller.dto.RegisterRequest;
import com.brick.billing.controller.dto.AuthResponse;
import com.brick.billing.model.User;
import com.brick.billing.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        // Forward to the static HTML file
        return "forward:/login.html";
    }
    
    @PostMapping("/perform_login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, 
                                              HttpSession session) {
        try {
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            
            if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                session.setAttribute("user", user);
                return ResponseEntity.ok(new AuthResponse(true, "Login successful", "/home.html"));
            } else {
                return ResponseEntity.ok(new AuthResponse(false, "Invalid username or password", null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new AuthResponse(false, "Login failed: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                 BindingResult bindingResult,
                                                 HttpSession session) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
                return ResponseEntity.ok(new AuthResponse(false, errorMessage, null));
            }
            
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return ResponseEntity.ok(new AuthResponse(false, "Passwords do not match", null));
            }
            
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.ok(new AuthResponse(false, "Username already exists", null));
            }
            
            if (userRepository.existsByMobileNumber(registerRequest.getMobileNumber())) {
                return ResponseEntity.ok(new AuthResponse(false, "Mobile number already registered", null));
            }
            
            User newUser = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getMobileNumber()
            );
            
            userRepository.save(newUser);
            
            session.setAttribute("user", newUser);
            
            return ResponseEntity.ok(new AuthResponse(true, "Registration successful", "/home.html"));
        } catch (Exception e) {
            return ResponseEntity.ok(new AuthResponse(false, "Registration failed: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "forward:/home.html";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
