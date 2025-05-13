package sa_ntlanganiso.SmartPrices_SA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa_ntlanganiso.SmartPrices_SA.model.Customer;
import sa_ntlanganiso.SmartPrices_SA.service.UserService;
import sa_ntlanganiso.SmartPrices_SA.security.JwtUtil;
import sa_ntlanganiso.SmartPrices_SA.dto.CartResponse;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "https://accounts.google.com"})
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody Customer customer) {
        try {
            // First check if email exists
            boolean exists = userService.customerExists(customer.getEmail());
            if (exists) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            // Register the new customer
            Customer registeredCustomer = userService.registerCustomer(customer);
            
            // Generate token for the new user
            String token = jwtUtil.generateToken(registeredCustomer);
            
            // Create response
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", registeredCustomer.getEmail());
            response.put("message", "Registration successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Customer loginRequest) {
        try {
            boolean isValid = userService.loginCustomer(
                loginRequest.getEmail(), 
                loginRequest.getPassword()
            );
            
            if (isValid) {
                Customer customer = userService.getCustomerByEmail(loginRequest.getEmail());
                String token = jwtUtil.generateToken(customer);
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", Map.of(
                    "email", customer.getEmail(),
                    "name", customer.getName(),
                    "id", customer.getId().toString()
                ));
                
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authorization header");
            }
            
            String token = authHeader.substring(7);
            String email = jwtUtil.getEmailFromToken(token);
            
            if (email != null && jwtUtil.validateToken(token)) {
                return ResponseEntity.ok(Map.of("valid", true, "email", email));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token verification failed: " + e.getMessage());
        }
    }
}