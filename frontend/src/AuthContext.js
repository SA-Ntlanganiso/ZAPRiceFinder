import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState([]);
  const [cartLoading, setCartLoading] = useState(false);
  const [cartError, setCartError] = useState(null);

  const loadCartItems = useCallback(async (token, email) => {
    if (!token || !email) return;
    
    setCartLoading(true);
    setCartError(null);
    try {
      const response = await axios.get(
        `http://localhost:8081/api/customers/${email}/cart`,
        { 
          headers: { Authorization: `Bearer ${token}` },
          timeout: 5000
        }
      );
      setCart(response.data?.items || []);
    } catch (error) {
      setCartError(error.response?.data?.message || error.message || 'Failed to load cart');
      console.error('Cart load error:', error);
    } finally {
      setCartLoading(false);
    }
  }, []);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      const userData = localStorage.getItem('user');
      
      if (token && userData) {
        try {
          const response = await axios.get('http://localhost:8081/auth/verify', {
            headers: { Authorization: `Bearer ${token}` },
            timeout: 5000
          });
          
          if (response.data.valid) {
            const parsedUser = JSON.parse(userData);
            setUser(parsedUser);
            setIsAuthenticated(true);
            await loadCartItems(token, parsedUser.email);
          }
        } catch (error) {
          console.error('Auth verification failed:', error);
        }
      }
      setLoading(false);
    };
    
    initializeAuth();
  }, [loadCartItems]);

  const login = async (token, userData) => {
    if (!userData || typeof userData !== 'object' || !userData.email) {
      throw new Error('Invalid user data from server');
    }
  
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setIsAuthenticated(true);
    setUser(userData);
    await loadCartItems(token, userData.email);
  };

  const addToCart = async (item) => {
    if (cartLoading) return false;
    
    setCartLoading(true);
    try {
      const token = localStorage.getItem('token');
      const email = user?.email;
      
      if (!token || !email) {
        throw new Error('Authentication required');
      }

      const newCart = [...cart, item];
      setCart(newCart);

      await axios.post(
        `http://localhost:8081/api/customers/${email}/cart`,
        item,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      return true;
    } catch (error) {
      setCart(cart);
      console.error('Add to cart error:', error);
      return false;
    } finally {
      setCartLoading(false);
    }
  };

  const removeFromCart = async (index) => {
    const token = localStorage.getItem('token');
    const userData = user || JSON.parse(localStorage.getItem('user'));
    
    if (!token || !userData?.email) {
      throw new Error('Authentication required');
    }

    setCartLoading(true);
    try {
      await axios.delete(
        `http://localhost:8081/api/customers/${userData.email}/cart/${index}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      await loadCartItems(token, userData.email);
      return true;
    } catch (error) {
      console.error('Remove from cart error:', error);
      return false;
    } finally {
      setCartLoading(false);
    }
  };

  const clearCart = async () => {
    const token = localStorage.getItem('token');
    const userData = user || JSON.parse(localStorage.getItem('user'));
    
    if (!token || !userData?.email) {
      throw new Error('Authentication required');
    }

    setCartLoading(true);
    try {
      await axios.delete(
        `http://localhost:8081/api/customers/${userData.email}/cart`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setCart([]);
      return true;
    } catch (error) {
      console.error('Clear cart error:', error);
      return false;
    } finally {
      setCartLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setUser(null);
    setCart([]);
  };

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      user,
      loading,
      cart,
      cartLoading,
      loadCartItems,
      addToCart,
      removeFromCart,
      clearCart,
      login,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);