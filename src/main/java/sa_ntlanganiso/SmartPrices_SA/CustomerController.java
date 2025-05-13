package sa_ntlanganiso.SmartPrices_SA;

import org.apache.hc.core5.http.HttpStatus;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa_ntlanganiso.SmartPrices_SA.dto.CartResponse;
import sa_ntlanganiso.SmartPrices_SA.model.CartItem;
import sa_ntlanganiso.SmartPrices_SA.model.Customer;
import sa_ntlanganiso.SmartPrices_SA.security.JwtUtil;
import sa_ntlanganiso.SmartPrices_SA.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3000") 
public class CustomerController {
    
        private final UserService userService;
        private final JwtUtil jwtUtil;
    
        @Autowired
        public CustomerController(UserService userService, JwtUtil jwtUtil) {
            this.userService = userService;
            this.jwtUtil = jwtUtil;
        }
    
        private boolean validateRequest(String authHeader, String email) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return false;
            }
            String token = authHeader.substring(7);
            return jwtUtil.validateToken(token) && 
                   email.equals(jwtUtil.getEmailFromToken(token));
        }
    
        @GetMapping("/{email}/cart")
        public ResponseEntity<CartResponse> getCartItems(
                @PathVariable String email,
                @RequestHeader("Authorization") String authHeader) {
            try {
                if (!validateRequest(authHeader, email)) {
                    return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
                }
                
                List<CartItem> cartItems = userService.getCartItems(email);
                return ResponseEntity.ok(new CartResponse(cartItems));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .body(new CartResponse(Collections.emptyList()));
            }
        }

    @PostMapping("/{email}/cart")
    public ResponseEntity<?> addToCart(
            @PathVariable String email,
            @RequestBody CartItem item,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!validateRequest(authHeader, email)) {
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
            }
            
            userService.addToCart(email, item);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{email}/cart/{index}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable String email,
            @PathVariable int index,
            @RequestBody CartItem updatedItem,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!validateRequest(authHeader, email)) {
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
            }
            
            userService.updateCartItem(email, index, updatedItem);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{email}/cart/{index}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable String email,
            @PathVariable int index,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!validateRequest(authHeader, email)) {
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
            }
            
            userService.removeFromCart(email, index);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // In your Java controller
        @PostMapping("/{email}/cart/sync")
    public ResponseEntity<Map<String, Object>> syncCart(
            @PathVariable String email,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // 1. Validate authorization
            if (!validateRequest(authHeader, email)) {
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "Unauthorized"));
            }

            // 2. Extract and validate items
            List<Map<String, Object>> items = (List<Map<String, Object>>) requestBody.get("items");
            if (items == null || items.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cart cleared",
                    "items", Collections.emptyList()
                ));
            }

            // 3. Process each item
            List<CartItem> cartItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                CartItem cartItem = new CartItem();
                
                // Required fields
                cartItem.setProductId(item.get("productId").toString());
                cartItem.setProductDescription(item.get("productDescription").toString());
                
                // Optional fields with defaults
                cartItem.setBrandName(item.getOrDefault("brandName", "Unknown").toString());
                
                // Handle price (string or number)
                Object price = item.get("price");
                if (price instanceof String) {
                    cartItem.setPrice(Double.parseDouble(((String) price).replaceAll("[^0-9.]", "")));
                } else {
                    cartItem.setPrice(((Number) price).doubleValue());
                }
                
                cartItem.setStoreName(item.getOrDefault("storeName", "Unknown").toString());
                cartItem.setProductImageUrl(item.getOrDefault("productImageUrl", "").toString());
                cartItem.setAddedAt(new Date());
                
                cartItems.add(cartItem);
            }

            // 4. Update database
            userService.syncCart(email, cartItems);

            // 5. Return success response
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart updated successfully",
                "items", cartItems.stream().map(this::convertToMap).collect(Collectors.toList())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage(),
                        "timestamp", new Date()
                    ));
        }
    }

    private Map<String, Object> convertToMap(CartItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", item.getProductId());
        map.put("productDescription", item.getProductDescription());
        map.put("brandName", item.getBrandName());
        map.put("price", item.getPrice());
        map.put("storeName", item.getStoreName());
        map.put("productImageUrl", item.getProductImageUrl());
        map.put("addedAt", item.getAddedAt());
        return map;
    }
    @DeleteMapping("/{email}/cart")
    public ResponseEntity<?> clearCart(
            @PathVariable String email,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!validateRequest(authHeader, email)) {
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
            }
            
            userService.clearCart(email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{email}")
    public ResponseEntity<Customer> getCustomer(
            @PathVariable String email,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!validateRequest(authHeader, email)) {
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build();
            }
            
            Customer customer = userService.getCustomerByEmail(email);
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        return userService.getCustomerById(new ObjectId(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable String id,
            @RequestBody Customer customerDetails) {
        Customer updatedCustomer = userService.updateCustomer(customerDetails);
        return ResponseEntity.ok(updatedCustomer);
    }
}