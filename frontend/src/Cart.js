import { useState, useEffect } from 'react';
import { useAuth } from './AuthContext';
import { toast } from 'react-toastify';
import './Cart.css';

const Cart = () => {
  const { 
    isAuthenticated,
    user,
    cart = [],
    cartLoading,
    cartError,
    loadCartItems,
    removeFromCart,
    clearCart
  } = useAuth();
  
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    if (isAuthenticated && user?.email) {
      loadCartItems();
    }
  }, [isAuthenticated, user?.email, loadCartItems]);

  useEffect(() => {
    if (cartError) {
      toast.error(cartError);
    }
  }, [cartError]);

  const handleRemoveItem = async (index) => {
    if (cartLoading || isProcessing) return;
    
    setIsProcessing(true);
    try {
      const success = await removeFromCart(index);
      if (success) {
        toast.success('Item removed from cart');
      } else {
        toast.error('Failed to remove item');
      }
    } catch (error) {
      toast.error('Error removing item');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleClearCart = async () => {
    if (cartLoading || isProcessing) return;
    
    setIsProcessing(true);
    try {
      const success = await clearCart();
      if (success) {
        toast.success('Cart cleared successfully');
      } else {
        toast.error('Failed to clear cart');
      }
    } catch (error) {
      toast.error('Error clearing cart');
    } finally {
      setIsProcessing(false);
    }
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price || 0), 0).toFixed(2);
  };

  if (!isAuthenticated) {
    return (
      <div className="cart-container">
        <h2>Your Cart</h2>
        <p>Please login to view your cart</p>
      </div>
    );
  }

  if (cartLoading && cart.length === 0) {
    return (
      <div className="cart-container">
        <h2>Your Cart</h2>
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  return (
    <div className="cart-container">
      <h2>Your Cart</h2>
      
      {cart.length === 0 ? (
        <p>Your cart is empty</p>
      ) : (
        <>
          <div className="cart-items">
            {cart.map((item, index) => (
              <div className={`cart-item ${isProcessing ? 'updating' : ''}`} 
                   key={`${item.productId}-${index}`}>
                <div className="cart-item-image">
                  <img 
                    src={item.productImageUrl} 
                    alt={item.productDescription}
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = '/placeholder-image.png';
                    }}
                  />
                </div>
                <div className="cart-item-details">
                  <h3>{item.productDescription}</h3>
                  <p><strong>Brand:</strong> {item.brandName}</p>
                  <p className="price">
                    <strong>Price:</strong> R{item.price?.toFixed(2) || '0.00'}
                  </p>
                  <p><strong>Store:</strong> <strong className="store">{item.storeName}</strong></p>
                  <div className="cart-item-actions">
                    <button 
                      className='remove-button'
                      onClick={() => handleRemoveItem(index)}
                      disabled={isProcessing}
                    >
                      {isProcessing ? 'Removing...' : 'Remove'}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
          
          <div className="cart-summary">
            <h3>Order Summary</h3>
            <div className="summary-row">
              <span>Subtotal ({cart.length} items)</span>
              <span>R{calculateTotal()}</span>
            </div>
            <div className="summary-row total">
              <span>Total</span>
              <span>R{calculateTotal()}</span>
            </div>
            <button 
              className="remove-button"
              onClick={handleClearCart}
              disabled={isProcessing}
            >
              {isProcessing ? 'Clearing...' : 'Clear Cart'}
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default Cart;