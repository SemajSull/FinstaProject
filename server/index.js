const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
// const bcrypt = require('bcryptjs'); // Removed bcryptjs for plain text password
const User = require('./models/User');
const Post = require('./models/Post');
const Follow = require('./models/Follow');

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());

// Configure multer for image upload
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        const uploadDir = 'uploads/';
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir);
        }
        cb(null, uploadDir);
    },
    filename: function (req, file, cb) {
        cb(null, Date.now() + path.extname(file.originalname));
    }
});

const upload = multer({ storage: storage });

// Serve uploaded files
app.use('/uploads', express.static('uploads'));

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

// Get post and follower counts for a user by username
app.get('/users/:username/counts', async (req, res) => {
  try {
    const user = await User.findOne({ username: req.params.username });
    if (!user) return res.status(404).json({ error: 'User not found' });

    const [postCount, followerCount, followingCount] = await Promise.all([
      Post.countDocuments({ authorId: user._id }),
      Follow.countDocuments({ followeeId: user._id }),
      Follow.countDocuments({ followerId: user._id })
    ]);

    res.json({ postCount, followerCount, followingCount });
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
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
        console.log('Fetching posts for user:', userId);

        // Find all users that the current user follows
        const followedUsers = await Follow.find({ followerId: new mongoose.Types.ObjectId(userId) });
        console.log('Found followed users:', followedUsers.length);

        const followedUserIds = followedUsers.map(follow => follow.followeeId);
        console.log('Followed user IDs:', followedUserIds);

        // If the user follows no one, return an empty array
        if (followedUserIds.length === 0) {
            console.log('No followed users found, returning empty array');
            return res.status(200).json([]);
        }

        // Find posts from these followed users, sorted by creation date
        const posts = await Post.find({ authorId: { $in: followedUserIds } })
                                .populate('authorId', 'username')
                                .sort({ createdAt: -1 });

        console.log('Found posts:', posts.length);

        // Map the posts to a format compatible with your Android Post model
        const formattedPosts = posts.map(post => ({
            id: post._id.toString(),
            username: post.authorId.username,
            imageUrl: post.imageUrl,
            caption: post.caption,
            likesCount: post.likesCount || 0,
            comments: [], // We'll add comments later
            createdAt: post.createdAt,
            isLiked: false
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

        // Debug log
        console.log('Follow request:', { followerId, followeeId });

        // Validate ObjectIds
        if (!mongoose.Types.ObjectId.isValid(followerId) || !mongoose.Types.ObjectId.isValid(followeeId)) {
            return res.status(400).json({ error: 'Invalid user ID(s)' });
        }

        // Check if both users exist
        const [follower, followee] = await Promise.all([
            User.findById(followerId),
            User.findById(followeeId)
        ]);

        if (!follower || !followee) {
            return res.status(404).json({ error: 'One or both users not found' });
        }

        // Check if follow relationship already exists
        const existingFollow = await Follow.findOne({
            followerId: new mongoose.Types.ObjectId(followerId),
            followeeId: new mongoose.Types.ObjectId(followeeId)
        });

        if (existingFollow) {
            return res.status(400).json({ error: 'Already following this user' });
        }

        // Create new follow relationship
        const follow = new Follow({
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

// Search users endpoint
app.get('/users/search/:query', async (req, res) => {
    try {
        const query = req.params.query;
        console.log('Searching for users with query:', query);
        
        // Simple exact match first
        const user = await User.findOne({ username: query });
        
        if (user) {
            // If exact match found, return just that user
            const formattedUser = {
                id: user._id.toString(),
                username: user.username,
                bio: user.bio || '',
                profileImageUrl: user.profileImageUrl || '',
                theme: user.theme || 'default',
                backgroundMusicUrl: user.backgroundMusicUrl || '',
                createdAt: user.createdAt
            };
            return res.status(200).json([formattedUser]);
        }

        // If no exact match, try partial match
        const users = await User.find({
            username: { $regex: query, $options: 'i' }
        }).select('-passwordHash');

        console.log('Found users:', users.length);

        // Format the response
        const formattedUsers = users.map(user => ({
            id: user._id.toString(),
            username: user.username,
            bio: user.bio || '',
            profileImageUrl: user.profileImageUrl || '',
            theme: user.theme || 'default',
            backgroundMusicUrl: user.backgroundMusicUrl || '',
            createdAt: user.createdAt
        }));

        // Always return 200 with array (empty if no results)
        res.status(200).json(formattedUsers);
    } catch (error) {
        console.error('Error searching users:', error);
        res.status(500).json({ error: 'Failed to search users: ' + error.message });
    }
});

// Get posts for a specific user
app.get('/posts/user/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        console.log('Fetching posts for user:', userId);

        const posts = await Post.find({ authorId: new mongoose.Types.ObjectId(userId) })
                              .populate('authorId', 'username')
                              .sort({ createdAt: -1 });

        console.log('Found posts:', posts.length);

        // Map the posts to a format compatible with your Android Post model
        const formattedPosts = posts.map(post => ({
            id: post._id.toString(),
            username: post.authorId.username,
            imageUrl: post.imageUrl,
            caption: post.caption,
            likesCount: post.likesCount || 0,
            comments: [], // We'll add comments later
            createdAt: post.createdAt,
            isLiked: false
        }));

        res.status(200).json(formattedPosts);

    } catch (error) {
        console.error("Error fetching user posts:", error);
        res.status(500).json({ error: error.message });
    }
});

// Get a single user by ID
app.get('/users/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        console.log('Fetching user:', userId);

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        // Format the response to exclude sensitive information
        const formattedUser = {
            id: user._id,
            username: user.username,
            bio: user.bio,
            profileImageUrl: user.profileImageUrl,
            theme: user.theme,
            backgroundMusicUrl: user.backgroundMusicUrl,
            createdAt: user.createdAt
        };

        res.status(200).json(formattedUser);

    } catch (error) {
        console.error("Error fetching user:", error);
        res.status(500).json({ error: error.message });
    }
});

// Check if one user follows another
app.get('/follows/check/:followerId/:followeeId', async (req, res) => {
    try {
        const { followerId, followeeId } = req.params;
        console.log('Checking follow status:', { followerId, followeeId });

        const follow = await Follow.findOne({
            followerId: new mongoose.Types.ObjectId(followerId),
            followeeId: new mongoose.Types.ObjectId(followeeId)
        });

        res.status(200).json(!!follow); // Convert to boolean

    } catch (error) {
        console.error("Error checking follow status:", error);
        res.status(500).json({ error: error.message });
    }
});

// Unfollow a user
app.delete('/follows/:followerId/:followeeId', async (req, res) => {
    try {
        const { followerId, followeeId } = req.params;
        console.log('Unfollowing user:', { followerId, followeeId });

        const result = await Follow.deleteOne({
            followerId: new mongoose.Types.ObjectId(followerId),
            followeeId: new mongoose.Types.ObjectId(followeeId)
        });

        if (result.deletedCount === 0) {
            return res.status(404).json({ error: 'Follow relationship not found' });
        }

        res.status(200).send();

    } catch (error) {
        console.error("Error unfollowing user:", error);
        res.status(500).json({ error: error.message });
    }
});

// Create a new post
app.post('/posts', upload.single('image'), async (req, res) => {
    try {
        const { caption, tags, authorId } = req.body;
        console.log('Creating post:', { caption, tags, authorId });

        if (!req.file) {
            return res.status(400).json({ error: 'No image file provided' });
        }

        // Create the post
        const post = new Post({
            authorId: new mongoose.Types.ObjectId(authorId),
            imageUrl: `/uploads/${req.file.filename}`,
            caption: caption || '',
            tags: tags ? tags.split(',').map(tag => tag.trim()) : [],
            createdAt: new Date(),
            likesCount: 0,
            commentsCount: 0
        });

        await post.save();
        console.log('Post created successfully:', post._id);

        res.status(201).json(post);

    } catch (error) {
        console.error("Error creating post:", error);
        res.status(500).json({ error: error.message });
    }
});

// Update user bio and profile image
app.put('/users/:id/profile', async (req, res) => {
  try {
    const { bio, profileImageUrl } = req.body;
    const userId = req.params.id;
    const update = {};
    if (bio !== undefined) update.bio = bio;
    if (profileImageUrl !== undefined) update.profileImageUrl = profileImageUrl;
    const user = await User.findByIdAndUpdate(userId, update, { new: true });
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ message: 'Profile updated', user });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Upload profile image
app.post('/users/:id/profile-image', upload.single('image'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No image file provided' });
    }
    // Return the URL to the uploaded image
    const imageUrl = `/uploads/${req.file.filename}`;
    res.json({ imageUrl });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = 3000;
app.listen(PORT, () => {
   console.log('Server running on port ' + PORT);
});