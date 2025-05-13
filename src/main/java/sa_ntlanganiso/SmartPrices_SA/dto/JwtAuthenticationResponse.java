package sa_ntlanganiso.SmartPrices_SA.dto;

import lombok.Data;
import sa_ntlanganiso.SmartPrices_SA.model.Customer;

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String userId;
    private String email;
    private String name;

    public JwtAuthenticationResponse(String accessToken, Customer customer) {
        this.accessToken = accessToken;
        this.userId = customer.getId().toString();
        this.email = customer.getEmail();
        this.name = customer.getName();
    }
}