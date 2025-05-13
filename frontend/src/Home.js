import React, { useState, useEffect } from 'react';
import { FiHome, FiZap, FiShoppingCart, FiGrid, FiUserPlus, FiSearch, FiLogIn } from 'react-icons/fi';
import ProductCards from './ProductCards';
import { useAuth } from './AuthContext';
import { toast } from 'react-toastify';
import './Home.css';

function Home() {
  const [term, setTerm] = useState('');
  const [products, setProducts] = useState([]);
  const [error, setError] = useState(null);
  const [currentMessage, setCurrentMessage] = useState(0);
  const [currentWelcome, setCurrentWelcome] = useState(0);
  const [currentVideoIndex, setCurrentVideoIndex] = useState(0);
  const { user } = useAuth();
  const [cartMessage, setCartMessage] = useState('');
  const { addToCart } = useAuth();

  const messages = [
    "The Best Solution for finding the best deals on electronics, apparel, and more! Search across brands like Apple, Adidas, and Nike.",
    "Save big by comparing prices from retailers like Takealot, Bash and Superbalist!",
    "Find the lowest prices on your favorite gadgets, clothing, and accessories.",
    "Shop smarter with ZAPriceFinder and make informed decisions effortlessly.",
    "Discover exclusive deals tailored to your needs every day!"
  ];

  const welcomMessageInAllLang = [
    "Molweni!",
    "Dumelang!",
    "Sawubona!",
    "Dumela!",
    "Ndaa!",
    "Aa!",
    "Hallo!",
    "Xewani!",
    "Lotjhani"
  ];

  const videos = [
    { name: "1", path: "video/1.mp4", duration: 19 },
    { name: "2", path: "video/2.mp4", duration: 48 },
    { name: "3", path: "video/3.mp4", duration: 7 },
    { name: "4", path: "video/4.mp4", duration: 16 },
    { name: "5", path: "video/5.mp4", duration: 10 },
    { name: "6", path: "video/6.mp4", duration: 6 },
  ];

  useEffect(() => {
    const duration = videos[currentVideoIndex]?.duration * 1000;
    const timeout = setTimeout(() => {
      setCurrentVideoIndex((prevIndex) => (prevIndex + 1) % videos.length);
    }, duration);
    return () => clearTimeout(timeout);
  }, [currentVideoIndex]);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentMessage((prevMessage) => (prevMessage + 1) % messages.length);
    }, 5000);
    return () => clearInterval(interval);
  }, [messages.length]);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentWelcome((prevWelcome) => (prevWelcome + 1) % welcomMessageInAllLang.length);
    }, 3000);
    return () => clearInterval(interval);
  }, [welcomMessageInAllLang.length]);

  const handleSearch = async () => {
    try {
      const response = await fetch(`http://localhost:8081/api/dbproducts/search?term=${term}`);
      if (response.ok) {
        const data = await response.json();
        setProducts(data);
        setError(null);
      } else {
        setError("No products found for the search term. Please wait.");
        setProducts([]);
      }
    } catch (err) {
      setError("An error occurred while fetching products. Database pipeline is closed.");
       toast.error(`An error occurred while fetching products. Database pipeline is closed.`)
      console.error(err);
    }
  };
  const handleAddToCart = async (product) => {
    try {
      const cartItem = {
        productId: product._id,
        productDescription: product.productDescription,
        brandName: product.brandName,
        price: parseFloat(product.price) || 0,
        storeName: product.storeName,
        productImageUrl: product.productImageUrl
      };
      
      await addToCart(cartItem);
      // Success toast is handled inside AuthContext
    } catch (error) {
      console.error('Add to cart error:', error);
      toast.error(`Failed to add ${product.productDescription} to cart`);
    }
  };

  return (
    <div className="App">
      {/* Cart message notification */}
      {cartMessage && (
        <div className="cart-message">
          {cartMessage}
        </div>
      )}

      {/* Existing navbar */}
      <nav className="home-navbar">
        <img src="images/smileShop.png" className="navbar-logo" alt="logo" />
        <li className="website-name">ZAPriceFinder</li>
        <span>
          <input
            value={term}
            onChange={(e) => setTerm(e.target.value)}
            placeholder="Search item here"
          />
          <button className='search-btn' onClick={handleSearch}>
            <FiSearch />
          </button>
        </span>
        <ul className="nav-links">
          <li><a href="/"><FiHome /> Home</a></li>
          <li><a href="/hotdeals"><FiZap /> Hot Deals</a></li>
          <li><a href="/cart"><FiShoppingCart /> Cart</a></li>
          <li><a href="/dashboard"><FiGrid /> Dashboard</a></li>
          <li><a href="/auth"><FiLogIn /> Login</a></li>
          <li><a href="/auth"><FiUserPlus /> Signup</a></li>
        </ul>
      </nav>

      {/* Existing sections */}
      <section className="website-description">
        <video 
          autoPlay 
          muted 
          key={videos[currentVideoIndex].path}
          className="background-video"
        >
          <source src={videos[currentVideoIndex].path} type="video/mp4" />
        </video>
        <h2 className="description-text">{welcomMessageInAllLang[currentWelcome]}</h2>
        <h2>Welcome to <span>ZAPriceFinder!</span></h2>
        <p className="description-text">{messages[currentMessage]}</p>
        <p className="byBrand"><strong>Shop By brand!!</strong></p>
      </section>

      <section className="brand-icons">
        <div className="icon-grid">
          <div className="brand-circle"><img src="images/applee.jpg" alt="Apple" /></div>
          <div className="brand-circle"><img src="images/adidas.webp" alt="Adidas" /></div>
          <div className="brand-circle"><img src="images/nikeeee.png" alt="Nike" /></div>
          <div className="brand-circle"><img src="images/hu.jpg" alt="Huawei" /></div>
          <div className="brand-circle"><img src="images/nb.webp" alt="Takealot" /></div>
          <div className="brand-circle"><img src="images/bash.jpg" alt="BAsh" /></div>
        </div>
      </section>

      {/* Error message and ProductCards */}
      {error && <div className="error-message">{error}</div>}
      <div>
        <div className="website-bck">
          <ProductCards 
            products={products}
            onAddToCart={addToCart} // Pass the context function directly
          />
        </div>
        <Footer />
      </div>
    </div>
  );
}

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h4>About Us</h4>
          <p>We are your go-to online store for the best products.</p>
          <p className="footer-credit">Found by Sinalo Alizwa Ntlanganiso.</p>
        </div>
        <div className="footer-section">
          <h4>Quick Links</h4>
          <ul>
            <li><a href="#">Home</a></li>
            <li><a href="#">Shop</a></li>
            <li><a href="#">Contact</a></li>
            <li><a href="#">FAQ</a></li>
          </ul>
        </div>
        <div className="footer-section">
          <h4>Follow Us</h4>
          <div className="social-icons">
            <a href="#">
              <i className="fab fa-facebook">
                <img className='fa-logo' src='images/facebook.webp' alt="Facebook" />
              </i>
            </a>
            <a href="#">
              <i className="fab fa-twitter">
                <img className='fa-logo' src='images/twitter.jpg' alt="Twitter" />
              </i>
            </a>
            <a href="#">
              <i className="fab fa-instagram">
                <img className='fa-logo' src='images/insta.jpg' alt="Instagram" />
              </i>
            </a>
          </div>
        </div>
      </div>
      <p className="footer-bottom">&copy; 2025 ZAPriceFinder. All Rights Reserved.</p>
    </footer>
  );
}

export default Home;