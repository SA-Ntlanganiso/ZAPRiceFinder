// models/Product.js
const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
  brandName: String,
  price: String, // Keep as string to preserve formatting
  productDescription: String,
  productImageUrl: String,
  storeName: String,
  creationDate: Date,
  // Add numeric price for filtering
  numericPrice: Number
});

// Add pre-save hook to automatically calculate numericPrice
productSchema.pre('save', function(next) {
  if (this.isModified('price')) {
    // Handle different price formats
    const numericPrice = parseFloat(
      this.price
        .replace(/[^0-9.]/g, '') // Remove non-numeric characters
        .replace(/\s+/g, '')     // Remove whitespace
    );
    
    if (!isNaN(numericPrice)) {
      this.numericPrice = numericPrice;
    } else {
      this.numericPrice = 0;
    }
  }
  next();
});

module.exports = mongoose.model('Product', productSchema);