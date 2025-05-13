import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './SignUp.css';
import { toast } from 'react-toastify';

function SignUp() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    // Client-side validation
    if (formData.password !== formData.confirmPassword) {
      const msg = 'Passwords do not match';
      setError(msg);
      toast.error(msg);
      return;
    }

    if (formData.password.length < 6) {
      const msg = 'Password must be at least 6 characters';
      setError(msg);
      toast.error(msg);
      return;
    }

    try {
      const response = await axios.post(
        'http://localhost:8081/auth/signup',
        {
          name: formData.username,
          email: formData.email,
          password: formData.password
        },
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );

      const msg = 'Registration successful!';
      setMessage(msg);
      toast.success(msg);
      setError('');

      // Navigate to login with optional state
      navigate('/login', {
        state: {
          email: formData.email,
          message: 'Registration successful! Please login.'
        }
      });

    } catch (err) {
      console.error('Signup error:', err);
      let errMsg = 'An unexpected error occurred.';

      if (err.response) {
        errMsg = err.response.data?.error || err.response.data?.message || 'Registration failed.';
      } else if (err.request) {
        errMsg = 'No response from server. Please check your internet connection.';
      } else {
        errMsg = 'Error: ' + err.message;
      }

      setError(errMsg);
      setMessage('');
      toast.error(errMsg);
    }
  };

  const handleGoogleSignIn = () => {
    window.location.href = "https://accounts.google.com/o/oauth2/auth?...";
  };

  return (
    <section className="signup-container">
      <div className="content-section">
        <div className="content-wrapper">
          <img src="/images/smileShop.png" alt="ZAPriceFinder Logo" className="brand-logo" />
          <h1><span>ZAPriceFinder!!!</span> Making your online shopping easier.</h1>
          <p className="section-paragraph">Find the best prices across multiple stores in seconds.</p>
        </div>
      </div>

      <div className="form-section">
        <div className="form-wrapper">
          <h2>Welcome to <strong><span className='changeColor'>ZAPriceFinder</span></strong></h2>
          <br />
          <p>Enter your credentials to access your account.</p>
          <br />

          <form onSubmit={handleSignUp}>
            <div className="input-container">
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
            </div>
            <div className="input-container">
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
            </div>
            <div className="input-container">
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input 
                  placeholder="Enter password (min 6 characters)" 
                  type="password" 
                  id="password" 
                  value={formData.password} 
                  onChange={handleChange} 
                  required 
                />
              </div>
            </div>
            <div className="input-container">
              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm Password</label>
                <input 
                  placeholder="Confirm your password" 
                  type="password" 
                  id="confirmPassword" 
                  value={formData.confirmPassword} 
                  onChange={handleChange} 
                  required 
                />
              </div>
            </div>
            <br />
            <div className="remember-forgot">
              <div className="remember-me">
                <input type="checkbox" id="remember-me" />
                <label htmlFor="remember-me">Remember me</label>
              </div>
              <a href="#">Forgot password?</a>
            </div>

            <button type="submit" className="login-btn">Sign Up</button>
          </form>

          {error && <p className="error-message">{error}</p>}
          {message && <p className="success-message">{message}</p>}

          <div className="divider">or</div>

          <button className="google-signin" onClick={handleGoogleSignIn}>
            <img src="/images/googleSVG.png" alt="Google Icon" className="google-icon" />
            <span>Sign in with Google</span>
          </button>
        </div>
      </div>
    </section>
  );
}

export default SignUp;
