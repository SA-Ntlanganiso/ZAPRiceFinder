package sa_ntlanganiso.SmartPrices_SA;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sa_ntlanganiso.SmartPrices_SA.db.MongoDbInterface;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import sa_ntlanganiso.SmartPrices_SA.security.JwtUtil;
import sa_ntlanganiso.SmartPrices_SA.service.DashboardService;
import sa_ntlanganiso.SmartPrices_SA.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
     private final MongoDbInterface mongoDbInterface;
    public DashboardController(DashboardService dashboardService, UserService userService, JwtUtil jwtUtil,MongoDbInterface mongoDbInterface) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.mongoDbInterface = mongoDbInterface;
    }
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<Product> allResults = mongoDbInterface.findByDescriptionContaining(term);
            
            // Create proper pagination
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allResults.size());
            
            Page<Product> paginatedResults = new PageImpl<>(
                allResults.subList(start, end),
                pageable,
                allResults.size()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("data", paginatedResults.getContent());
            response.put("currentPage", paginatedResults.getNumber());
            response.put("totalItems", paginatedResults.getTotalElements());
            response.put("totalPages", paginatedResults.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            var productsPage = dashboardService.getPaginatedProducts(page, size);
            return ResponseEntity.ok(createPagedResponse(productsPage));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/products/stores")
    public ResponseEntity<?> getAllUniqueStores() {
        try {
            List<String> stores = dashboardService.getAllUniqueStores();
            if (stores.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonList("No stores found"));
            }
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Stores unavailable: " + e.getMessage()));
        }
    }
    @GetMapping("/products/filter")
    public ResponseEntity<Map<String, Object>> filterProductsByStore(
            @RequestParam String store,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            var filteredPage = dashboardService.filterProductsByStore(store, page, size);
            return ResponseEntity.ok(createPagedResponse(filteredPage));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getProductStats() {
        try {
            return ResponseEntity.ok(dashboardService.getProductStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getCustomer(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            return ResponseEntity.ok(userService.getCustomerByEmail(email));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(errorResponse("Unauthorized"));
        }
    }

    private Map<String, Object> createPagedResponse(Page<Product> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }

    private Map<String, Object> errorResponse(String message) {
        return Map.of("error", message, "success", false);
    }
}