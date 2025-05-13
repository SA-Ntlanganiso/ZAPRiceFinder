package sa_ntlanganiso.SmartPrices_SA.dto;  // Must match exactly

import sa_ntlanganiso.SmartPrices_SA.model.CartItem;
import java.util.List;

public class CartResponse {
    private List<CartItem> items;
    private double total;
    private int count;

    public CartResponse() {}  // Required for JSON serialization

    public CartResponse(List<CartItem> items) {
        this.items = items;
        this.count = items != null ? items.size() : 0;
        this.total = items != null ? items.stream().mapToDouble(CartItem::getPrice).sum() : 0.0;
    }

    // Getters
    public List<CartItem> getItems() { return items; }
    public double getTotal() { return total; }
    public int getCount() { return count; }

    // Setters
    public void setItems(List<CartItem> items) { 
        this.items = items;
        // Auto-update derived fields
        if (items != null) {
            this.count = items.size();
            this.total = items.stream().mapToDouble(CartItem::getPrice).sum();
        } else {
            this.count = 0;
            this.total = 0.0;
        }
    }
    public void setTotal(double total) { this.total = total; }
    public void setCount(int count) { this.count = count; }
}