import React, { useState, useEffect, useMemo } from 'react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  LineChart,
  Line
} from 'recharts';
import { 
  Search, 
  ShoppingCart, 
  TrendingDown, 
  Package, 
  Store, 
  AlertCircle,
  LayoutDashboard,
  LineChart as LineChartIcon,
  List,
  Settings,
  LogOut,
  Menu,
  X,
  Filter,
  RefreshCcw
} from 'lucide-react';
import './Dashboard.css';
import '@fontsource/inter/400.css';
import '@fontsource/inter/500.css';
import '@fontsource/inter/600.css';
import '@fontsource/inter/700.css';
const Dashboard = () => {
  // State management
  const [products, setProducts] = useState([]);
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [customerLoading, setCustomerLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStore, setSelectedStore] = useState('All');
  const [isScraping, setIsScraping] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [priceHistory, setPriceHistory] = useState([]);
  const [showFilters, setShowFilters] = useState(false);
  const [priceRange, setPriceRange] = useState({ min: 0, max: 10000 });
  const [sortBy, setSortBy] = useState('price-asc');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [stores, setStores] = useState(['All']);
  const [productStats, setProductStats] = useState({
    totalProducts: 0,
    averagePrice: 0,
    minPrice: 0,
    maxPrice: 0,
    totalStores: 0
  });
  // Fetch customer data
  const fetchCustomerData = async () => {
    try {
      setCustomerLoading(true);
      const token = localStorage.getItem('token');
      if (!token) throw new Error('No authentication token found');
  
      const response = await fetch('http://localhost:8081/api/dashboard/customer', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });
  
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      
      const customerData = await response.json();
      setCustomer(customerData);
    } catch (error) {
      console.error('Error fetching customer:', error);
      setError(error.message);
    } finally {
      setCustomerLoading(false);
    }
  };
  

  // Fetch all products with pagination
  const fetchProducts = async (page = currentPage, size = pageSize) => { 
    try {
      setLoading(true);
      setError(null);
      const token = localStorage.getItem('token');
      const headers = { 'Content-Type': 'application/json' };
  
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      } else {
        throw new Error('No authentication token found');
      }
  
      const response = await fetch(
        `http://localhost:8081/api/dashboard/products?page=${page}&size=${size}`,
        {
          method: 'GET',
          headers,
          credentials: 'include' 
        }
      );
  
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      
      const data = await response.json();
      setProducts(data.data);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
      setCurrentPage(data.currentPage);
      generateMockPriceHistory(data.data);
    } catch (err) {
      console.error('Error fetching products:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  

  // Fetch stores
  const fetchStores = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) throw new Error('No authentication token found');
  
      const response = await fetch(
        'http://localhost:8081/api/dashboard/products/stores',
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          credentials: 'include' 
        }
      );
  
      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('Authentication failed - please login again');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }
  
      const storesData = await response.json();
      setStores(storesData.length > 0 ? ['All', ...storesData] : ['No stores found']);
    } catch (error) {
      console.error('Error fetching stores:', error);
      setStores(['Error loading stores']);
    }
  };
  

  // Pagination controls
  const PaginationControls = () => (
    <div className="pagination">
      <button
        onClick={() => handlePageChange(currentPage - 1)}
        disabled={currentPage === 0}
      >
        Previous
      </button>
      
      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i}
          onClick={() => handlePageChange(i)}
          className={currentPage === i ? 'active' : ''}
        >
          {i + 1}
        </button>
      ))}
      
      <button
        onClick={() => handlePageChange(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
      >
        Next
      </button>
    </div>
  );

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    fetchProducts(newPage);
  };

  // Generate mock price history data for visualization
  const generateMockPriceHistory = (products) => {
    if (!products || products.length === 0) return;
    
    const mockHistory = [];
    const now = new Date();
    
    // Create 7 days of history
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      const entry = {
        date: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      };
      
      // Add price data for each store
      const storeSet = new Set();
      products.forEach(product => {
        if (product.storeName) {
          storeSet.add(product.storeName);
        }
      });
      
      Array.from(storeSet).forEach(store => {
        // Random fluctuation between 0.9 and 1.1 of current price
        const fluctuation = 0.9 + Math.random() * 0.2;
        // Update generateMockPriceHistory to handle missing prices
        // In generateMockPriceHistory
      const avgPrice = products
      .filter(p => p.storeName === store)
      .reduce((acc, p) => {
        const price = parseFloat(p.price?.replace(/[^0-9.]/g, '') || 0);
        return acc + price;
      }, 0) / (products.filter(p => p.storeName === store).length || 1); // Prevent division by zero
        
        entry[store] = (avgPrice * fluctuation).toFixed(2);
      });
      
      mockHistory.push(entry);
    }
    
    setPriceHistory(mockHistory);
  };

  // Search products
  const handleSearch = async () => {
    try {
      setIsScraping(true);
      setCurrentPage(0);
      const token = localStorage.getItem('token');
      const headers = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;
  
      const response = await fetch(
        `http://localhost:8081/api/dashboard/search?term=${encodeURIComponent(searchTerm.trim())}&page=${currentPage}&size=${pageSize}`,
        {
          method: 'GET',
          headers,
          credentials: 'include', // Ensure cookies and sessions are sent
        }
      );
  
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
  
      const data = await response.json();
      setProducts(data.data);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
    } catch (err) {
      console.error('Search request failed:', err);
    } finally {
      setIsScraping(false);
    }
  };
  

  // Handle product filtering by store
  const handleFilterByStore = async (store) => {
    try {
      setSelectedStore(store);
      setLoading(true);
      setError(null);
      const token = localStorage.getItem('token');
      const headers = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;

      const url = store === 'All' 
        ? `http://localhost:8081/api/dashboard/products?page=0&size=${pageSize}`
        : `http://localhost:8081/api/dashboard/products/filter?store=${encodeURIComponent(store)}&page=0&size=${pageSize}`;

      const response = await fetch(url, { headers });
      
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      
      const data = await response.json();
      setProducts(data.data);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
      setCurrentPage(0);
    } catch (err) {
      console.error('Filter error:', err);
      setError(err.message || "Filtering failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Logout handler
  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/login';
  };

  // Load data on component mount
  useEffect(() => {
    fetchCustomerData();
    fetchProducts();
    fetchStores();
  }, []);

  // Memoized values for performance
  // In productsByLowestPrice useMemo
const productsByLowestPrice = useMemo(() => {
  if (!products || products.length === 0) return [];
  
  const productMap = products.reduce((acc, product) => {
    if (!product._id || !product.price) return acc;
    
    const price = parseFloat(product.price.replace(/[^0-9.]/g, '')) || 0;
    const existing = acc[product._id];
    
    if (!existing || price < existing.price) {
      acc[product._id] = {...product, price};
    }
    return acc;
  }, {});

  const result = Object.values(productMap);
  
  // Apply sorting using numeric values
  switch(sortBy) {
    case 'price-asc': return result.sort((a, b) => a.price - b.price);
    case 'price-desc': return result.sort((a, b) => b.price - a.price);
    case 'name-asc': return result.sort((a, b) => 
      (a.productDescription || '').localeCompare(b.productDescription || ''));
    case 'name-desc': return result.sort((a, b) => 
      (b.productDescription || '').localeCompare(a.productDescription || ''));
    default: return result;
  }
}, [products, sortBy]);
  
  // In filteredProducts useMemo
  const filteredProducts = useMemo(() => {
    if (!products || products.length === 0) return [];
    
    return products.filter(product => {
      const price = parseFloat(
        product.price.replace(/[^0-9.]/g, '') // Remove all non-numeric characters
      ) || 0;

      return (
        (!searchTerm || 
        product.productDescription?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.brandName?.toLowerCase().includes(searchTerm.toLowerCase())) &&
        (selectedStore === 'All' || product.storeName === selectedStore) &&
        (price >= priceRange.min && price <= priceRange.max)
      );
    });
  }, [products, searchTerm, selectedStore, priceRange]);
  
  const chartData = useMemo(() => {
    if (!filteredProducts || filteredProducts.length === 0) return [];
    
    const productPrices = {};
    
    filteredProducts.forEach(product => {
      if (!product._id) return;
      
      productPrices[product._id] = productPrices[product._id] || {
        productId: product._id,
        productName: product.productDescription?.substring(0, 20) + (product.productDescription?.length > 20 ? '...' : '') || 'Unnamed Product',
      };

      const price = parseFloat(product.price);
      if (!isNaN(price) && product.storeName) {
        productPrices[product._id][product.storeName] = price;
      }
    });
    
    return Object.values(productPrices).slice(0, 6);
  }, [filteredProducts]);
  
  // In priceStats useMemo
const priceStats = useMemo(() => {
  if (!filteredProducts || filteredProducts.length === 0) return { avg: 0, min: 0, max: 0 };
  
  const prices = filteredProducts
    .map(p => parseFloat(p.price.replace(/[^0-9.]/g, '')) || 0)
    .filter(p => !isNaN(p));
  
  if (prices.length === 0) return { avg: 0, min: 0, max: 0 };
  
  const sum = prices.reduce((a, b) => a + b, 0);
  
  return {
    avg: sum / prices.length,
    min: Math.min(...prices),
    max: Math.max(...prices)
  };
}, [filteredProducts]);

  // Set title when active tab changes
  useEffect(() => {
    document.title = `ZAPriceFinder - ${activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}`;
  }, [activeTab]);
  const fetchStats = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8081/api/dashboard/stats', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      const stats = await response.json();
      setProductStats({
        totalProducts: stats.totalProducts,
        averagePrice: stats.averagePrice,
        minPrice: stats.minPrice,
        maxPrice: stats.maxPrice,
        totalStores: stats.totalStores
      });
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };
  // Add safe price parsing
const safeParsePrice = (price) => {
  try {
    return parseFloat(price.toString().replace(/[^0-9.]/g, ''));
  } catch (e) {
    console.warn('Invalid price format:', price);
    return 0;
  }
};
// Utility function for consistent price formatting
const formatPrice = (price) => {
  const numericValue = parseFloat(price.replace(/[^0-9.]/g, '')) || 0;
  return numericValue.toLocaleString('en-ZA', {
    style: 'currency',
    currency: 'ZAR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};
  useEffect(() => {
    fetchCustomerData();
    fetchProducts();
    fetchStores();
    fetchStats(); // Add this line
  }, []);
  // Loadin g state
  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <div className="loading-text">
          {isScraping ? 'Scraping for products...' : 'Loading products...'}
        </div>
      </div>
    );
  }
  
  // Error state
  if (error) {
    return (
      <div className="error-screen">
        <AlertCircle size={48} className="error-icon" />
        <div className="error-message">{error}</div>
        <button 
          onClick={() => {
            setError(null);
            setSearchTerm('');
            setSelectedStore('All');
            fetchProducts();
          }}
          className="retry-button"
        >
          Try Again
        </button>
      </div>
    );
  }

  const ModernProductTable = ({ products }) => {
    return (
      <div className="table-wrapper">
        <table className="modern-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Brand</th>
              <th>Price</th>
              <th>Store</th>
              <th>Image</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {products.map(product => (
              <tr key={product._id}>
                <td className="product-description">
                  {product.productDescription || 'No description available'}
                </td>
                <td className="brand-name">{product.brandName || 'Unknown brand'}</td>
                <td className="price">R{parseFloat(product.price).toFixed(2)}</td>
                <td className="store-name">{product.storeName || 'Unknown store'}</td>
                <td className="product-image">
                  {product.productImageUrl ? (
                    <img 
                      src={product.productImageUrl} 
                      alt={product.productDescription}
                      className="product-thumbnail"
                    />
                  ) : (
                    <div className="image-placeholder">
                      <Package size={20} />
                    </div>
                  )}
                </td>
                <td className="creation-date">
                  {new Date(product.creationDate).toLocaleDateString('en-GB', {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric'
                  })}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div className="dashboard-container">
      {/* Sidebar */}
      <div className={`sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
        <div className="sidebar-header">
          {sidebarOpen ? (
            <div className="brand-container">
              <div className="brand-logo">
                <ShoppingCart size={16} />
              </div>
              <span className="brand-name">ZAPriceFinder</span>
              <button 
                onClick={() => setSidebarOpen(false)}
                className="close-sidebar"
              >
                <X size={20} />
              </button>
            </div>
          ) : (
            <button 
              onClick={() => setSidebarOpen(true)}
              className="open-sidebar-btn"
            >
              <Menu size={24} />
            </button>
          )}
        </div>

        {sidebarOpen && (
          <>
            <div className="sidebar-menu">
              <button
                onClick={() => setActiveTab('dashboard')}
                className={`menu-item ${activeTab === 'dashboard' ? 'active' : ''}`}
              >
                <LayoutDashboard className="menu-icon" />
                <span className="menu-text">Dashboard</span>
              </button>
              
              <button
                onClick={() => setActiveTab('analytics')}
                className={`menu-item ${activeTab === 'analytics' ? 'active' : ''}`}
              >
                <LineChartIcon className="menu-icon" />
                <span className="menu-text">Analytics</span>
              </button>
              
              <button
                onClick={() => setActiveTab('products')}
                className={`menu-item ${activeTab === 'products' ? 'active' : ''}`}
              >
                <List className="menu-icon" />
                <span className="menu-text">Products</span>
              </button>
              
              <button
                onClick={() => setActiveTab('settings')}
                className={`menu-item ${activeTab === 'settings' ? 'active' : ''}`}
              >
                <Settings className="menu-icon" />
                <span className="menu-text">Settings</span>
              </button>
            </div>

            <div className="sidebar-footer">
              <div className="user-profile">
                <div className="user-avatar">
                  {customer ? (customer.name || 'U').charAt(0).toUpperCase() : 'U'}
                </div>
                {!customerLoading && customer && (
                  <div className="user-info">
                    <div className="user-name">{customer.name || 'User'}</div>
                    <div className="user-email">{customer.email || 'No email'}</div>
                  </div>
                )}
                <button className="logout-btn" onClick={handleLogout}>
                  <LogOut size={20} />
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Main Content */}
      <div className="main-content">
        {!sidebarOpen && (
          <button 
            onClick={() => setSidebarOpen(true)}
            className="floating-menu-btn"
          >
            <Menu size={24} />
          </button>
        )}

        <div className="content-container">
          {/* Header */}
          <div className="dashboard-header">
            <h1>ZAPriceFinder Dashboard</h1>
            <p>Find the best deals across multiple stores</p>
          </div>
          
          {/* Search and Filter */}
          <div className="search-filter-container">
            <div className="search-bar">
              <Search className="search-icon" />
              <input
                type="text"
                placeholder="Search products..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
              <button 
                onClick={handleSearch}
                disabled={isScraping}
                className="search-button"
              >
                {isScraping ? (
                  <span className="search-button-content">
                    <span className="search-spinner"></span>
                    <span>Searching...</span>
                  </span>
                ) : 'Search'}
              </button>
            </div>
            
            <div className="filter-controls">
              <button 
                className="filter-toggle-btn"
                onClick={() => setShowFilters(!showFilters)}
              >
                <Filter size={18} />
                <span>Filters</span>
              </button>
              
              <button 
                className="refresh-btn"
                onClick={() => fetchProducts()}
                disabled={loading}
              >
                <RefreshCcw size={18} className={loading ? 'spin' : ''} />
              </button>
            </div>
          </div>
          
          {/* Expanded Filters */}
          {showFilters && (
            <div className="expanded-filters">
              <div className="filter-group">
                <label>Store:</label>
                <select 
                  value={selectedStore}
                  onChange={(e) => handleFilterByStore(e.target.value)}
                  className="store-selector"
                >
                  {stores.map(store => (
                    <option key={store} value={store}>{store}</option>
                  ))}
                </select>
              </div>
              
              <div className="filter-group">
                <label>Price Range:</label>
                <div className="price-range-inputs">
                  <input
                    type="number"
                    min="0"
                    placeholder="Min"
                    value={priceRange.min}
                    onChange={(e) => setPriceRange({...priceRange, min: parseFloat(e.target.value) || 0})}
                  />
                  <span>to</span>
                  <input
                    type="number"
                    min="0"
                    placeholder="Max"
                    value={priceRange.max}
                    onChange={(e) => setPriceRange({...priceRange, max: parseFloat(e.target.value) || 10000})}
                  />
                </div>
              </div>
              
              <div className="filter-group">
                <label>Sort By:</label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="sort-selector"
                >
                  <option value="price-asc">Price: Low to High</option>
                  <option value="price-desc">Price: High to Low</option>
                  <option value="name-asc">Name: A to Z</option>
                  <option value="name-desc">Name: Z to A</option>
                </select>
              </div>
            </div>
          )}
          
          {/* Stats Cards */}
          <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-header">
            <h3>Products</h3>
            <Package className="stat-icon blue" />
          </div>
          <div className="stat-value">{productStats.totalProducts}</div>
          <div className="stat-label">Total Products</div>
        </div>
        
        <div className="stat-card">
          <div className="stat-header">
            <h3>Average Price</h3>
            <TrendingDown className="stat-icon green" />
          </div>
          <div className="stat-value">R{productStats.averagePrice.toFixed(2)}</div>
          <div className="stat-label">Across all stores</div>
        </div>
        
        <div className="stat-card">
          <div className="stat-header">
            <h3>Price Range</h3>
            <TrendingDown className="stat-icon green" />
          </div>
          <div className="stat-value">
            R{productStats.minPrice.toFixed(2)} - R{productStats.maxPrice.toFixed(2)}
          </div>
          <div className="stat-label">Min to Max</div>
        </div>
        
        <div className="stat-card">
          <div className="stat-header">
            <h3>Stores</h3>
            <Store className="stat-icon purple" />
          </div>
          <div className="stat-value">{productStats.totalStores}</div>
          <div className="stat-label">Comparing prices</div>
        </div>
      </div>
          
          {/* Dashboard Content based on active tab */}
          {activeTab === 'dashboard' && (
            <>
              {/* Price Comparison Chart */}
              <div className="chart-container">
                <h2>Price Comparison</h2>
                {chartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={400}>
                    <BarChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                      <XAxis dataKey="productName" stroke="#888" />
                      <YAxis stroke="#888" />
                      <Tooltip 
                        contentStyle={{
                          backgroundColor: '#2d3748',
                          borderColor: '#4a5568',
                          borderRadius: '0.5rem',
                          color: 'white'
                        }}
                      />
                      <Legend />
                      {stores.filter(store => store !== 'All').map((store, index) => (
                        <Bar 
                          key={store} 
                          dataKey={store} 
                          name={store} 
                          fill={`hsl(${index * 40 + 200}, 70%, 50%)`}
                          radius={[4, 4, 0, 0]}
                        />
                      ))}
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="empty-state">
                    {searchTerm ? 'No products found. Try a different search term.' : 'No products available'}
                  </div>
                )}
              </div>
              
              {/* Best Deals Table */}
              <div className="deals-container">
                <div className="deals-header">
                  <h2>Best Deals</h2>
                  <ShoppingCart className="cart-icon" />
                </div>
                
                {productsByLowestPrice.length > 0 ? (
                  <div className="table-container">
                    <table>
                      <thead>
                        <tr>
                        <th>Product-ID</th>
                          <th>Product</th>
                          <th>Brand</th>
                          <th>Store</th>
                          <th>Price</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productsByLowestPrice.slice(0, 5).map((product) => (
                          <tr key={product._id}>
                            <td>{product._id}</td>
                            <td>{product.productDescription}</td>
                            <td>{product.brandName}</td>
                            <td>{product.storeName}</td>
                            // In Best Deals table render
                            <td className="price-cell">
                              R{(product.price || 0).toLocaleString('en-ZA', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                              })}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) :(
                  <div className="empty-state">
                    <Package size={24} />
                    <p>No matching products found</p>
                    <small>Try adjusting your filters</small>
                  </div>
                )}
              </div>
              <PaginationControls />
            </>
          )}
          
          {activeTab === 'analytics' && (
            <div className="analytics-container">
              <h2>Price Trends</h2>
              {priceHistory.length > 0 ? (
                <ResponsiveContainer width="100%" height={400}>
                  <LineChart data={priceHistory}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="date" stroke="#888" />
                    <YAxis stroke="#888" />
                    <Tooltip 
                      contentStyle={{
                        backgroundColor: '#2d3748',
                        borderColor: '#4a5568',
                        borderRadius: '0.5rem',
                        color: 'white'
                      }}
                    />
                    <Legend />
                    {stores.filter(store => store !== 'All').map((store, index) => (
                      <Line 
                        key={store}
                        type="monotone"
                        dataKey={store}
                        name={store}
                        stroke={`hsl(${index * 40 + 200}, 70%, 50%)`}
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      />
                    ))}
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="empty-state">
                  No price history available
                </div>
              )}
              
              <div className="analytics-stats">
                <h2>Price Analysis</h2>
                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-header">
                      <h3>Price Range</h3>
                    </div>
                    <div className="stat-value">R{priceStats.min} - R{priceStats.max}</div>
                    <div className="stat-label">Min to Max</div>
                  </div>
                  
                  <div className="stat-card">
                    <div className="stat-header">
                      <h3>Potential Savings</h3>
                    </div>
                    <div className="stat-value">
                      R{(priceStats.max - priceStats.min).toFixed(2)}
                    </div>
                    <div className="stat-label">Max savings possible</div>
                  </div>
                </div>
              </div>
            </div>
          )}
          
          {activeTab === 'products' && (
            <div className="products-container">
              <h2>All Products</h2>
              
              {filteredProducts.length > 0 ? (
                <>
                  <ModernProductTable products={filteredProducts} />
                  <PaginationControls />
                </>
              ) : (
                <div className="empty-state">
                  No products available
                </div>
              )}
            </div>
          )}
          
          {activeTab === 'settings' && (
            <div className="settings-container">
              <h2>Account Settings</h2>
              
              <div className="settings-section">
                <h3>User Profile</h3>
                {!customerLoading && customer ? (
                  <div className="settings-form">
                    <div className="form-group">
                      <label>Name</label>
                      <input type="text" value={customer.name || ''} disabled />
                    </div>
                    <div className="form-group">
                      <label>Email</label>
                      <input type="email" value={customer.email || ''} disabled />
                    </div>
                  </div>
                ) : (
                  <div className="empty-state">
                    Loading user information...
                  </div>
                )}
              </div>
              
              <div className="settings-section">
                <h3>Preferences</h3>
                <div className="settings-form">
                  <div className="form-group">
                    <label>Default Store</label>
                    <select 
                      value={selectedStore}
                      onChange={(e) => setSelectedStore(e.target.value)}
                    >
                      {stores.map(store => (
                        <option key={store} value={store}>{store}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Default Sort</label>
                    <select
                      value={sortBy}
                      onChange={(e) => setSortBy(e.target.value)}
                    >
                      <option value="price-asc">Price: Low to High</option>
                      <option value="price-desc">Price: High to Low</option>
                      <option value="name-asc">Name: A to Z</option>
                      <option value="name-desc">Name: Z to A</option>
                    </select>
                  </div>
                </div>
              </div>
              
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;