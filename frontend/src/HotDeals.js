import { useState } from 'react';
import { toast } from 'react-toastify';
import './HotDeals.css';
// In HotDeals.js, add this import at the top
import { useAuth } from './AuthContext'; // Adjust path as needed

const HotDeals = () => {
  const { isAuthenticated, addToCart } = useAuth();
  const [chatMessage, setChatMessage] = useState('');
  const [chatHistory, setChatHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [suggestedQueries] = useState([
    "Do you have Dell G15 Gaming Laptop - RTX 3060, Core i7",
    "Find ASUS TUF Gaming BE3600 Dual Band WiFi 7 Router",
    "Display Apple iPhone 16 Pro Max 512GB",
    "Show kitchen appliances on sale"
  ]);

  const handleAddToCart = async (product) => {
    if (!isAuthenticated) {
      toast.error('Please login to add items to cart');
      return;
    }
    
    try {
      const cartItem = {
        productId: product._id,
        productDescription: product.productDescription,
        price: parseFloat(product.price.replace(/[^0-9.]/g, '')),
        productImageUrl: product.productImageUrl,
        storeName: product.storeName
      };

      const success = await addToCart(cartItem);
      if (success) {
        toast.success(`${product.productDescription} added to cart`);
      }
    } catch (error) {
      toast.error('Failed to add to cart');
    }
  };

  const sendChat = async () => {
    if (!chatMessage.trim() || isLoading) return;

    try {
      setIsLoading(true);
      const userMessage = chatMessage;
      setChatMessage('');
      
      // Add user message
      setChatHistory(prev => [...prev, {
        type: 'user',
        content: userMessage,
        timestamp: new Date().toISOString()
      }]);

      // Add loading indicator
      setChatHistory(prev => [...prev, {
        type: 'loader',
        timestamp: new Date().toISOString()
      }]);

      const response = await fetch('http://localhost:8082/api/chatbot/ask', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
      body: JSON.stringify({ message: userMessage }),
    });

      const data = await response.json();
      
      
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    if (!data.success) {
      throw new Error(data.error || 'Request failed');
    }

      // Remove loading indicator
      setChatHistory(prev => prev.filter(msg => msg.type !== 'loader'));

      setChatHistory(prev => [...prev, {
        type: 'bot',
        content: data.responseText || `Found ${data.products.length} matching products:`,
        products: data.products,
        searchParams: data.searchParams,
        timestamp: new Date().toISOString()
      }]);

    } catch (err) {
       setChatHistory(prev => [...prev, {
      type: 'bot',
      content: `Error: ${err.message}`,
      timestamp: new Date().toISOString()
    }]);
      toast.error(err.message || 'Chat service unavailable');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="hot-deals-container">
      <div className="chat-header">
        <h2>Smart Shopping Assistant</h2>
        <p>Ask for products using natural language</p>
        
        <div className="suggested-queries">
          {suggestedQueries.map((query, index) => (
            <button
              key={index}
              onClick={() => setChatMessage(query)}
              className="query-chip"
            >
              {query}
            </button>
          ))}
        </div>
      </div>

      <div className="chat-container">
        <div className="chat-messages">
          {chatHistory.map((msg, index) => {
            if (msg.type === 'loader') {
              return (
                <div key={`loader-${index}`} className="message bot">
                  <div className="message-content">
                    <div className="message-bubble">
                      <div className="typing-indicator">
                        <div className="dot"></div>
                        <div className="dot"></div>
                        <div className="dot"></div>
                      </div>
                    </div>
                  </div>
                </div>
              );
            }

            return (
              <div key={`msg-${index}`} className={`message ${msg.type}`}>
                <div className="message-content">
                  <div className="message-bubble">
                    <p>{msg.content}</p>
                    
                    {msg.products && (
                      <div className="products-grid">
                        {msg.products.map(product => (
                          <div key={product._id} className="product-card">
                            <div className="image-container">
                              <img
                                src={product.productImageUrl}
                                alt={product.productDescription}
                                onError={(e) => {
                                  e.target.src = '/placeholder-product.png';
                                }}
                              />
                              {product.storeName && (
                                <span className="store-badge">
                                  {product.storeName}
                                </span>
                              )}
                            </div>
                            
                            <div className="product-details">
                              <h4>{product.brandName}</h4>
                              <p className="description">
                                {product.productDescription}
                              </p>
                              
                              <div className="price-section">
                                <span className="price">
                                  R{parseFloat(product.price).toFixed(2)}
                                </span>
                                <button
                                  className={`add-to-cart-btn ${!isAuthenticated ? 'disabled' : ''}`}
                                  onClick={() => handleAddToCart(product)}
                                  disabled={!isAuthenticated}
                                >
                                  {isAuthenticated ? 'Add to Cart' : 'Login to Buy'}
                                </button>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                  <span className="message-time">
                    {new Date(msg.timestamp).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </span>
                </div>
              </div>
            );
          })}
        </div>

        <div className="chat-input-container">
          <input
            type="text"
            value={chatMessage}
            onChange={(e) => setChatMessage(e.target.value)}
            placeholder="Ask about products (e.g. 'Show me wireless headphones under R2000')"
            onKeyPress={(e) => e.key === 'Enter' && sendChat()}
            disabled={isLoading}
          />
          <button
            onClick={sendChat}
            disabled={isLoading}
            className="send-button"
          >
            {isLoading ? (
              <div className="spinner"></div>
            ) : (
              'Send'
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
export default HotDeals;