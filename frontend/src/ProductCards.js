import React, { useState } from "react";
import { toast } from "react-toastify";
import { useAuth } from "./AuthContext";
import "./ProductCards.css";

function ProductCards({ products, itemsPerPage = 24 }) {
  const [currentPage, setCurrentPage] = useState(1);
  const [addingItem, setAddingItem] = useState(false);
  const { user, isAuthenticated, addToCart } = useAuth();
  const totalPages = Math.ceil(products.length / itemsPerPage);

  const handleAddToCart = async (product) => {
    if (!isAuthenticated || !user) {
      toast.error('Please login to add items to cart');
      return;
    }

    setAddingItem(true);
    try {
      const cartItem = {
        productId: product.id || product._id,
        productDescription: product.productDescription || 'Unknown Product',
        brandName: product.brandName || 'Unknown Brand',
        price: typeof product.price === 'string' ? 
              parseFloat(product.price.replace(/[^0-9.]/g, '')) : 
              product.price,
        storeName: product.storeName || 'Unknown Store',
        productImageUrl: product.productImageUrl || '',
        addedAt: new Date().toISOString()
      };

      const success = await addToCart(cartItem);
      if (success) {
        toast.success(`Added ${cartItem.productDescription} to cart`);
      } else {
        throw new Error('Failed to add to cart');
      }
    } catch (error) {
      toast.error(error.message);
    } finally {
      setAddingItem(false);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const startIndex = (currentPage - 1) * itemsPerPage;
  const selectedProducts = products.slice(startIndex, startIndex + itemsPerPage);

  return (
    <div className="product-cards-container">
      <div className="product-cards">
        {selectedProducts.map((product) => (
          <div className="card" key={product._id}>
            <div className="card-content">
              <h3>{product.productDescription}</h3>
              <p>
                <strong>Brand:</strong> <span className="brand-name">{product.brandName}</span>
              </p>
              <p className="price">
                <strong>Price:</strong> {product.price}
              </p>
              <p>
                <strong>Store:</strong> {product.storeName}
              </p>
              <p>
                <strong>Added:</strong> {product.creationDate}
              </p>
              <div className="buttons">
                <button 
                  className="add-to-cart"
                  onClick={() => handleAddToCart(product)}
                  disabled={addingItem}
                >
                  {addingItem ? 'Adding...' : 'Add to Cart'}
                </button>
                <a
                  href={`https://${product.storeName}.com`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="go-buy"
                >
                  Go Buy
                </a>
              </div>
            </div>
            <ImageWithFallback
              src={product.productImageUrl}
              alt={product.productDescription}
              fallback={<div className="loading-spinner"></div>}
            />
          </div>
        ))}
      </div>
      
      <div className="pagination">
        <button onClick={handlePrevPage} disabled={currentPage === 1}>
          Prev
        </button>
        <span>Page {currentPage} of {totalPages}</span>
        <button onClick={handleNextPage} disabled={currentPage === totalPages}>
          Next
        </button>
      </div>
    </div>
  );
}

function ImageWithFallback({ src, alt, fallback }) {
  const [isError, setIsError] = useState(false);

  return (
    <div className="image-container">
      {!isError ? (
        <img
          src={src}
          alt={alt}
          className="product-image"
          onError={() => setIsError(true)}
        />
      ) : (
        fallback
      )}
    </div>
  );
}

export default ProductCards;