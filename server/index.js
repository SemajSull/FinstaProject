const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const cors = require('cors');
// const bcrypt = require('bcryptjs'); // Removed bcryptjs for plain text password
const User = require('./models/User');

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());

const MONGO_URI = 'mongodb+srv://appUser:appUser@finstacluster1.it3foso.mongodb.net/?retryWrites=true&w=majority&appName=FinstaCluster1'

// Connect to MongoDb Atlas
mongoose.connect(MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  dbName: 'finsta'
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
    
    // Explicitly format the user object to ensure _id is a string
    const formattedUser = {
        id: user._id.toString(), // Convert ObjectId to String
        username: user.username,
        // Include other fields as needed, but exclude passwordHash for security
        bio: user.bio,
        profileImageUrl: user.profileImageUrl,
        theme: user.theme,
        backgroundMusicUrl: user.backgroundMusicUrl,
        createdAt: user.createdAt
    };
    res.json(formattedUser);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Sign-in endpoint (plain text password for testing)
app.post('/signin', async (req, res) => {
    const { username, password } = req.body;

    try {
        const user = await User.findOne({ username });
        if (!user) {
            console.log(`Sign-in attempt for non-existent user: ${username}`);
            return res.status(404).json({ message: 'User not found' });
        }

        console.log(`Attempting sign-in for user: ${username}`);
        console.log(`Password from request: ${password}`);
        console.log(`Password from database: ${user.passwordHash}`);
        
        // Compare sent password with plain text password in database
        const isMatch = (password === user.passwordHash); // Plain text comparison
        console.log(`Password comparison result (isMatch): ${isMatch}`);

        if (!isMatch) {
            return res.status(400).json({ message: 'Invalid credentials' });
        }

        res.status(200).json({ message: 'Sign-in successful' });

    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

//add user endpoint (save plain text password)
app.post('/users', async(req, res) => {
  console.log('Received: ', req.body); //see what Android is sending
  try {
    const { username, password, bio, profileImageUrl, theme, backgroundMusicUrl } = req.body;

    // Save password in plain text for testing
    const passwordHash = password; // Store plain text password

    const user = new User({ 
        username,
        passwordHash,
        bio,
        profileImageUrl,
        theme,
        backgroundMusicUrl
    });
    const savedUser = await user.save();
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

// Get posts from followed users for the home feed
app.get('/posts/followed/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;

        // Find all users that the current user follows
        const followedUsers = await mongoose.model('Follow', new mongoose.Schema({}), 'follows')
                                        .find({ followerId: new mongoose.Types.ObjectId(userId) });

        const followedUserIds = followedUsers.map(follow => follow.followeeId);

        // If the user follows no one, return an empty array
        if (followedUserIds.length === 0) {
            return res.status(200).json([]);
        }

        // Find posts from these followed users, sorted by creation date
        // Populate the authorId with the username from the User collection
        const posts = await mongoose.model('Post', new mongoose.Schema({}), 'posts')
                                .find({ authorId: { $in: followedUserIds } })
                                .populate('authorId', 'username') // Populate authorId with username
                                .sort({ createdAt: -1 }); // Most recent first

        // Map the posts to a format compatible with your Android Post model
        const formattedPosts = posts.map(post => ({
            id: post._id.toString(), // Convert ObjectId to String
            username: post.authorId.username, // Use the populated username
            imageUrl: post.imageUrl,
            caption: post.caption,
            likesCount: post.likesCount || 0,
            // Note: comments are not directly in the Post schema, you'd fetch these separately if needed
            comments: [], // Initialize as empty for now to avoid crashes
            createdAt: post.createdAt,
            isLiked: false // This is client-side state, so default to false
        }));

        res.status(200).json(formattedPosts);

    } catch (error) {
        console.error("Error fetching followed posts:", error);
        res.status(500).json({ error: error.message });
    }
});

// Follow a user endpoint
app.post('/users/:followerId/follow/:followeeId', async (req, res) => {
    try {
        const followerId = req.params.followerId;
        const followeeId = req.params.followeeId;

        // Check if both users exist
        const [follower, followee] = await Promise.all([
            User.findById(followerId),
            User.findById(followeeId)
        ]);

        if (!follower || !followee) {
            return res.status(404).json({ error: 'One or both users not found' });
        }

        // Check if follow relationship already exists
        const existingFollow = await mongoose.model('Follow', new mongoose.Schema({}), 'follows')
            .findOne({ followerId: new mongoose.Types.ObjectId(followerId), followeeId: new mongoose.Types.ObjectId(followeeId) });

        if (existingFollow) {
            return res.status(400).json({ error: 'Already following this user' });
        }

        // Create new follow relationship
        const follow = new mongoose.model('Follow', new mongoose.Schema({}), 'follows')({
            followerId: new mongoose.Types.ObjectId(followerId),
            followeeId: new mongoose.Types.ObjectId(followeeId),
            createdAt: new Date()
        });

        await follow.save();
        res.status(201).json({ message: 'Successfully followed user' });

    } catch (error) {
        console.error("Error following user:", error);
        res.status(500).json({ error: error.message });
    }
});

const PORT = 3000;
app.listen(PORT, () => {
   console.log(`Server running on port ${PORT}`);
});
