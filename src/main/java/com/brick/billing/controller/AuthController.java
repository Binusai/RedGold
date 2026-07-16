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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("registerRequest", new RegisterRequest());
        return "login";
    }
    
    @PostMapping("/perform_login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, 
                                              HttpSession session) {
        try {
            // For demo purposes, check if user exists with matching credentials
            // In production, use proper authentication with password encoding
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            
            if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
                session.setAttribute("user", user);
                return ResponseEntity.ok(new AuthResponse(true, "Login successful", "/dashboard"));
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
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
                return ResponseEntity.ok(new AuthResponse(false, errorMessage, null));
            }
            
            // Check if passwords match
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return ResponseEntity.ok(new AuthResponse(false, "Passwords do not match", null));
            }
            
            // Check if username exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.ok(new AuthResponse(false, "Username already exists", null));
            }
            
            // Check if mobile number exists
            if (userRepository.existsByMobileNumber(registerRequest.getMobileNumber())) {
                return ResponseEntity.ok(new AuthResponse(false, "Mobile number already registered", null));
            }
            
            // Create new user
            User newUser = new User(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getMobileNumber()
            );
            
            userRepository.save(newUser);
            
            // Auto-login after registration
            session.setAttribute("user", newUser);
            
            return ResponseEntity.ok(new AuthResponse(true, "Registration successful", "/dashboard"));
        } catch (Exception e) {
            return ResponseEntity.ok(new AuthResponse(false, "Registration failed: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        // Check if user is logged in
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "dashboard"; // Your dashboard page
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
