// In a central setup file (e.g., api.js)
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
  const API_BASE = 'http://localhost:8081/api';
  export const fetchProducts = async (token) => {
    const headers = {
      'Content-Type': 'application/json'
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  
    const response = await fetch(`${API_BASE}/dashboard/products`, { headers });
    if (!response.ok) throw new Error('Failed to fetch products');
    return response.json();
  };
  
  export const searchProducts = async (term, token) => {
    const headers = {
      'Content-Type': 'application/json'
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  
    const response = await fetch(
      `${API_BASE}/dashboard/products/search?term=${encodeURIComponent(term)}`,
      { headers }
    );
    if (!response.ok) throw new Error('Search failed');
    return response.json();
  };
  
  export const fetchCustomer = async (token) => {
    if (!token) throw new Error('No token provided');
    
    const response = await fetch(`${API_BASE}/dashboard/customer`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch customer');
    return response.json();
  };