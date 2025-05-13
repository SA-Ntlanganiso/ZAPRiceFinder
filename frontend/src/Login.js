import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { toast } from 'react-toastify';
import './Login.css';

function Login() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8081/auth/login', formData);
      
      // Debug the complete response
      console.log('Full login response:', response.data);
  
      // Check if response has the expected structure
      if (!response.data || !response.data.token) {
        throw new Error('Invalid server response format');
      }
  
      // Handle case where user data might be missing
      const userData = response.data.user || {
        email: formData.email, // Fallback to login email
        name: '',
        id: ''
      };
  
      if (!userData.email) {
        throw new Error('User email not found in response');
      }
  
      login(response.data.token, userData);
      
      setMessage('Login successful!');
      setError('');
      toast.success('Login successful!');
      
      const from = location.state?.from || '/home';
      navigate(from, { replace: true });
      
    } catch (err) {
      let errorMessage = 'Login failed';
      
      if (err.response) {
        // Handle structured error responses
        errorMessage = err.response.data?.error || 
                      err.response.data?.message || 
                      JSON.stringify(err.response.data);
      } else if (err.message) {
        errorMessage = err.message;
      }
  
      console.error('Login failed:', {
        error: err,
        response: err.response?.data,
        status: err.response?.status
      });
  
      setError(errorMessage);
      setMessage('');
      toast.error(errorMessage);
    }
  };
  const handleGoogleSignIn = () => {
    // Store the intended destination before redirecting to Google auth
    localStorage.setItem('preAuthPath', location.state?.from || '/home');
    window.location.href = "https://accounts.google.com/o/oauth2/auth?...";
  };

  // Debug stored auth data on render
  React.useEffect(() => {
    console.log('Current stored auth data:', {
      token: localStorage.getItem('token'),
      user: localStorage.getItem('user')
    });
  }, []);

  return (
    <section className="signup-container">
      {/* Content Section */}
      <div className="content-section">
        <div className="content-wrapper">
          <img src="images/smileShop.png" alt="ZAPriceFinder Logo" className="brand-logo" />
          <h1><span>ZAPriceFinder!!!</span> Making your online shopping easier.</h1>
          <p className="section-paragraph">Find the best prices across multiple stores in seconds.</p>
        </div>
      </div>

      {/* Login Form Section */}
      <div className="form-section">
        <div className="form-wrapper">
          <h2>Great to have you back!</h2>
          <br /><br />
          
          <form onSubmit={handleLogin}>
            <div className="input-container">
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input 
                  placeholder="Enter your email" 
                  type="email" 
                  id="email" 
                  value={formData.email}
                  onChange={handleChange}
                  aria-autocomplete="off" 
                  required
                />
              </div>
            </div>
            <div className="input-container">
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input 
                  placeholder="Enter password" 
                  type="password" 
                  id="password" 
                  value={formData.password}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>
            <div className="remember-forgot">
              <div className="remember-me">
                <input type="checkbox" id="remember-me" />
                <label htmlFor="remember-me">Remember me</label>
              </div>
              <a className='blue' href="#">Forgot password?</a>
            </div>

            <button type="submit" className="login-btn">Login</button>
          </form>

          {error && <p className="error-message">{error}</p>}
          {message && <p className="success-message">{message}</p>}

          <div className="divider">or</div>

          <button className="google-signin" onClick={handleGoogleSignIn}>
            <img src="/images/googleSVG.png" alt="Google Logo" className="google-icon" />
            <span>Sign in with Google</span>
          </button>
        </div>
      </div>
    </section>
  );
}

export default Login;