const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const cors = require('cors');
const User = require('./models/User');

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());

const MONGO_URI = 'mongodb+srv://appUser:appUser@finstacluster1.it3foso.mongodb.net/?retryWrites=true&w=majority&appName=FinstaCluster1'

// Connect to MongoDb Atlas
mongoose.connect(MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
    console.log('Connected to MongoDB Atlas');
}).catch((err) => {
   console.error('MongoDB connection error:', err);
});

//Routes; all endpoints needed by the Android app to get data from the database

// base route
app.get('/', (req, res) => {
  res.send('Backend is running');
});

//get users endpoint
app.get('/users', async(req, res) => {
  try {
    const users = await User.find();
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

//get users by id endpoint
app.get('/users/:id', async(req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) return res.status(404).json({ error: 'User ID not found'});
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

//get users by username endpoint
app.get('/users/username/:username', async(req, res) => {
  try {
    const user = await User.findOne({ username: req.params.username });
    if (!user) return res.status(404).json({ error: 'User with this username not found'});
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

//add user endpoint
app.post('/users', async(req, res) => {
  console.log('Received: ', req.body); //see what Android is sending
  try {
    const user = new User(req.body);
    const savedUser = user.save();
    res.status(201).json(savedUser);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

//user count endpoint
app.get('/usercount', async(req, res) => {
  try {
    const count = await User.countDocuments();
    res.json({count});
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = 3000;
app.listen(PORT, () => {
   console.log(`Server running on port ${PORT}`);
});
