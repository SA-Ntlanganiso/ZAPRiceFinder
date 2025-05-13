// chatbot.js
const { GoogleGenerativeAI } = require("@google/generative-ai");
const Product = require("./models/Product");
const genAI = new GoogleGenerativeAI(process.env.GOOGLE_API_KEY);

async function generateSearchQuery(userMessage) {
  try {
    const model = genAI.getGenerativeModel({ 
      model: "gemini-1.5-flash-latest",
      generationConfig: {
        maxOutputTokens: 500
      }
    });

    const prompt = `Extract product search parameters as JSON:
    {
      "category": "main product type",
      "priceMax": "maximum price as number",
      "keywords": ["important features"]
    }

    Examples:
    - "gaming laptops under R15000" → {"category":"laptop","priceMax":15000,"keywords":["gaming"]}
    - "cheap wireless headphones" → {"category":"headphones","priceMax":1000,"keywords":["wireless"]}

    Query: "${userMessage}"
    JSON:`;

    const result = await model.generateContent(prompt);
    const text = (await result.response).text();
    
    // Enhanced JSON cleanup
    const cleanText = text
      .replace(/```json/g, '')
      .replace(/```/g, '')
      .replace(/(\w+):/g, '"$1":') // Ensure proper JSON formatting
      .trim();

    return JSON.parse(cleanText);
  } catch (error) {
    console.error('AI Parsing Error:', error);
    // Fallback to regex parsing
    return {
      priceMax: extractPrice(userMessage),
      keywords: extractKeywords(userMessage)
    };
  }
}

// Helper functions
function extractPrice(message) {
  const match = message.match(/under\s*R?\s*(\d[\d,]*)/i);
  return match ? parseInt(match[1].replace(/,/g, '')) : null;
}

function extractKeywords(message) {
  const keywords = message.match(/(gaming|wireless|bluetooth|cheap|budget)/gi) || [];
  return [...new Set(keywords.map(k => k.toLowerCase()))];
}

// Helper function

// Rest of your chatbot.js remains the same

// In chatbot.js
async function handleChatQuery(userMessage) {
  try {
    const filters = await generateSearchQuery(userMessage);
    
    // Build optimized query
    const query = {};
    
    // Price filter
    if (filters.priceMax) {
      query.numericPrice = { $lte: filters.priceMax };
    }

    // Keyword filter
    if (filters.keywords?.length) {
      query.$or = [
        { productDescription: { $regex: filters.keywords.join('|'), $options: 'i' } },
        { brandName: { $regex: filters.keywords.join('|'), $options: 'i' } }
      ];
    }

    // Execute query
    const products = await Product.find(query)
      .sort({ numericPrice: 1 })
      .limit(5)
      .lean();

    return {
      success: true,
      responseText: products.length > 0 
        ? `Found ${products.length} matching products:` 
        : "No matching products found",
      products
    };
  } catch (error) {
    console.error('Search Error:', error);
    return {
      success: false,
      error: 'Failed to process your request'
    };
  }
}
module.exports = { handleChatQuery };