import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import './SignUp.css';

function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleAuth = async (e) => {
    e.preventDefault();
    try {
      const url = isLogin 
        ? 'http://localhost:8081/auth/login'
        : 'http://localhost:8081/auth/signup';
      
      const payload = isLogin
        ? { email: formData.email, password: formData.password }
        : formData;

      const response = await axios.post(url, payload);
      
      if (isLogin) {
        // Use the auth context login function
        login(response.data.token, {
          email: formData.email,
          username: response.data.username // Make sure your backend returns this
        });
        navigate('/dashboard');
      } else {
        setMessage(response.data);
        // Clear form and switch to login
        setFormData({ username: '', email: '', password: '' });
        setIsLogin(true);
      }
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed');
      setMessage('');
    }
  };

  return (
    <section className="auth-container">
      {/* Your existing UI structure */}
      <div className="content-section">
        {/* ... your content ... */}
      </div>

      <div className="form-section">
        <div className="form-wrapper">
          <h2>{isLogin ? 'Welcome back!' : 'Create an account'}</h2>
          
          <form onSubmit={handleAuth}>
            {!isLogin && (
              <div className="form-group">
                <label htmlFor="username">Username</label>
                <input 
                  placeholder="Enter a username" 
                  type="text" 
                  id="username" 
                  value={formData.username} 
                  onChange={handleChange} 
                  required 
                />
              </div>
            )}
            
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input 
                placeholder="Enter your email" 
                type="email" 
                id="email" 
                value={formData.email} 
                onChange={handleChange} 
                required 
              />
            </div>
            
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

            {error && <div className="error-message">{error}</div>}
            {message && <div className="success-message">{message}</div>}

            <button type="submit" className="auth-btn">
              {isLogin ? 'Log In' : 'Sign Up'}
            </button>
          </form>

          <p className="toggle-auth" onClick={() => setIsLogin(!isLogin)}>
            {isLogin ? 'Need an account? Sign Up' : 'Already have an account? Log In'}
          </p>

          {/* Google auth button if needed */}
        </div>
      </div>
    </section>
  );
}

export default AuthPage;