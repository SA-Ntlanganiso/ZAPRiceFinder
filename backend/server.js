// backend/server.js
require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const Product = require('./models/Product');
const { handleChatQuery } = require('./chatbot');

const app = express();

// ── MongoDB Connection ─────────────────────────────
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
})
.then(() => console.log('Connected to MongoDB'))
.catch(err => console.error('MongoDB connection error:', err));

// ── Middleware ─────────────────────────────────────
app.use(cors());
app.use(express.json());

// ── Chatbot Endpoint ───────────────────────────────
// Example Node.js / Express route

// server.js
app.post('/api/chatbot/ask', async (req, res) => {
  try {
    const { message } = req.body;
    
    if (!message || message.trim().length < 2) {
      return res.json({
        success: true,
        responseText: "Please ask about products (e.g. 'Show me gaming laptops under R15000')",
        products: []
      });
    }

    // Handle simple greetings
    if (/^(hi|hello|hey)$/i.test(message.trim())) {
      return res.json({
        success: true,
        responseText: "Hello! How can I help you find products today?",
        products: []
      });
    }

    // Process product query
    const result = await handleChatQuery(message);
    
    if (!result.success) {
      return res.status(400).json(result);
    }

    // Format prices
    const formattedProducts = result.products.map(p => ({
      ...p,
      formattedPrice: `R ${p.numericPrice.toLocaleString('en-ZA')}`
    }));

    res.json({
      ...result,
      products: formattedProducts
    });
    
  } catch (error) {
    console.error('Server Error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error' 
    });
  }
});

// ── Health Check ────────────────────────────────────
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'ok', 
    timestamp: new Date(),
    dbState: mongoose.connection.readyState
  });
});

const PORT = process.env.PORT || 8082;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`MongoDB URI: ${process.env.MONGODB_URI}`);
});
