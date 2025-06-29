# Finsta - Instagram-like Social Media App

Finsta is an Android application that replicates core Instagram functionality, allowing users to share photos, follow other users, and interact with posts.

## Features

### Authentication
- User registration and login
- Secure password handling
- Session management

### Home Feed
- View posts from followed users
- Like and comment on posts
- Create new posts with images
- Support for both gallery images and image URLs
- Real-time post updates

### Profile Management
- View personal profile
- Edit profile information
- Customize profile theme
- Set profile music
- View post count, followers, and following
- Grid view of user's posts

### Search & Discovery
- Search for users
- View user profiles
- Follow/unfollow users
- View user's posts

### Post Creation
- Upload images from gallery
- Add images via URL
- Add captions
- Add tags
- Support for multiple image formats

### Social Features
- Follow/unfollow users
- Like posts
- Comment on posts
- View post interactions

## Technical Details

### Architecture
- MVVM (Model-View-ViewModel) architecture
- RESTful API integration
- Retrofit for network calls
- Glide for image loading
- RecyclerView for efficient list handling

### Dependencies
- Retrofit2 for API communication
- Glide for image loading
- Material Design components
- AndroidX libraries
- RecyclerView for efficient list handling

### API Integration
- User authentication
- Post management
- Profile management
- Social interactions (follow, like, comment)

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Gradle 7.0 or higher
- Node.js installed on your system

### Step 1: Configure IP Address
1. Open the file `app/src/main/java/com/example/finstatest/api/ApiServiceInstance.java`
2. Replace the `BASE_URL` string with your local IP address:
   ```java
   private static final String BASE_URL = "http://YOUR_IP_ADDRESS:3000/";
   ```
   For example: `"http://192.168.1.100:3000/"`

### Step 2: Start the Server
1. Open a terminal/command prompt
2. Navigate to the server directory:
   ```bash
   cd server
   ```
3. Install dependencies (if not already installed):
   ```bash
   npm install
   ```
4. Start the server:
   ```bash
   node index.js
   ```
5. The server should now be running on port 3000

### Step 3: Run the Android Application
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the application on your device or emulator

**Important Notes:**
- Make sure your Android device/emulator and the server are on the same network
- The IP address in `ApiServiceInstance.java` must match your computer's local IP address
- The server must be running before you launch the Android app

## Requirements
- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Gradle 7.0 or higher

## Contributing
Feel free to submit issues and enhancement requests!
