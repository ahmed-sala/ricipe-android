# 🍽️ MenueMaster - Meal Planner App

A feature-rich Android meal planning application that helps users discover, save, and organize their favorite recipes. Built with modern Android development practices and clean architecture.



## 📱 Features

- **Recipe Discovery**: Browse and search thousands of recipes from TheMealDB API
- **User Authentication**: Secure login and registration with Firebase Authentication
- **Favorites Management**: Save your favorite recipes locally and sync across devices
- **Meal Planning**: Create and organize weekly meal plans
- **Recipe Details**: View detailed cooking instructions, ingredients, and nutritional information
- **Offline Support**: Access saved recipes even without internet connection
- **User Profile**: Manage personal information and change password
- **Data Synchronization**: Seamlessly sync user data across devices using Firestore

## 🏗️ Architecture & Tech Stack

### Architecture Pattern
- **MVP (Model-View-Presenter)**: Feature-based modular architecture for better code organization and testability

### Technologies & Libraries

#### Core Android
- **Language**: Java
- **UI**: XML Layouts
- **Min SDK**: 24 (Android 7.0)

#### Local Database
- **Room Database**: For local data persistence
  - User authentication data
  - Favorite recipes
  - Meal plans

#### Networking
- **Retrofit**: RESTful API client for TheMealDB API
- **RxJava**: Reactive programming for asynchronous operations and data streams
- **Gson**: JSON serialization/deserialization

#### Backend Services
- **Firebase Authentication**: User authentication and authorization
- **Cloud Firestore**: Real-time database for user data synchronization

#### Dependency Injection
- Modern dependency management with proper separation of concerns

## 📂 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/yourpackage/menumaster/
│   │   │   ├── features/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── data/
│   │   │   │   │   ├── view/
│   │   │   │   │   └── presentation/
│   │   │   │   ├── recipes/
│   │   │   │   │   ├── data/
│   │   │   │   │   ├── view/
│   │   │   │   │   └── presentation/
│   │   │   │   ├── favorites/
│   │   │   │   │   ├── data/
│   │   │   │   │   ├── view/
│   │   │   │   │   └── presentation/
│   │   │   │   ├── planner/
│   │   │   │   │   ├── data/
│   │   │   │   │   ├── view/
│   │   │   │   │   └── presentation/
│   │   │   │   └── profile/
│   │   │   │       ├── data/
│   │   │   │       ├── view/
│   │   │   │       └── presentation/

│   │   │   └── utils/
│   │   └── res/
│   │       ├── layout/
│   │       ├── values/
│   │       └── drawable/
```

## 🔌 API Reference

This app uses **TheMealDB API** for recipe data:

- **Base URL**: `https://www.themealdb.com/api/json/v1/1/`
- **API Documentation**: [TheMealDB API Docs](https://www.themealdb.com/api.php)

### Key Endpoints Used:
- `search.php?s={meal_name}` - Search meals by name
- `lookup.php?i={meal_id}` - Get meal details by ID
- `filter.php?c={category}` - Filter meals by category
- `categories.php` - List all meal categories
- `random.php` - Get random meal

## 🚀 Getting Started

### Prerequisites

Before running the project, ensure you have:

- **Android Studio** (Arctic Fox or later recommended)
- **JDK 11** or higher
- **Android SDK** with minimum API level 24
- **Firebase Account** (for authentication and Firestore)
- **Internet Connection** (for API calls and Firebase services)

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/ahmed-sala/ricipe-android.git
   cd ricipe-android
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select `File > Open`
   - Navigate to the cloned repository folder
   - Click `OK` and wait for Gradle sync

3. **Firebase Configuration**
   
   a. Create a Firebase project:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add Project" and follow the setup wizard
   
   b. Add Android app to Firebase:
   - In Firebase Console, click "Add app" and select Android
   - Enter your package name (e.g., `com.yourpackage.menumaster`)
   - Download the `google-services.json` file
   - Place it in the `app/` directory of your project
   
   c. Enable Firebase Services:
   - **Authentication**: Enable Email/Password sign-in method
   - **Cloud Firestore**: Create a database in production mode
   - Update Firestore security rules as needed:
     ```
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /users/{userId} {
           allow read, write: if request.auth != null && request.auth.uid == userId;
         }
       }
     }
     ```

4. **Build the Project**
   ```bash
   ./gradlew build
   ```
   Or use Android Studio: `Build > Make Project`

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click the `Run` button (▶️) in Android Studio
   - Or use command line:
     ```bash
     ./gradlew installDebug
     ```

### Configuration

If you need to modify API endpoints or configuration:

1. Update the base URL in your Retrofit API service class
2. Ensure all Firebase configuration is correctly set up
3. Verify that `google-services.json` is in the correct location

## 📋 Project Management

- **GitHub Repository**: [ricipe-android](https://github.com/ahmed-sala/ricipe-android)
- **Project Board**: [Trello-style Workflow](https://github.com/users/ahmed-sala/projects/2/views/1)

Track development progress, feature requests, and bug reports on the project board.

## 🔐 Data Flow

```
User Action
    ↓
View (Activity/Fragment)
    ↓
Presenter
    ↓
Repository
    ↓
├── Local (Room Database) ←→ Firestore Sync
└── Remote (Retrofit API)
    ↓
RxJava Stream
    ↓
Presenter
    ↓
View Update
```

## 🛠️ Key Dependencies

```gradle
// Room Database
implementation 'androidx.room:room-runtime:2.x.x'
annotationProcessor 'androidx.room:room-compiler:2.x.x'

// Retrofit
implementation 'com.squareup.retrofit2:retrofit:2.x.x'
implementation 'com.squareup.retrofit2:converter-gson:2.x.x'
implementation 'com.squareup.retrofit2:adapter-rxjava2:2.x.x'

// RxJava
implementation 'io.reactivex.rxjava2:rxjava:2.x.x'
implementation 'io.reactivex.rxjava2:rxandroid:2.x.x'

// Firebase
implementation 'com.google.firebase:firebase-auth:21.x.x'
implementation 'com.google.firebase:firebase-firestore:24.x.x'

// Glide (for image loading)
implementation 'com.github.bumptech.glide:glide:4.x.x'
```

## 📝 Features in Detail

### Authentication
- Email/Password registration and login
- Password reset functionality
- Password change for logged-in users
- Persistent login sessions

### Recipe Management
- Search recipes by name or category
- View detailed recipe information
- Save recipes to favorites
- Remove from favorites

### Meal Planning
- Create weekly meal plans
- Assign recipes to specific days
- Edit and delete meal plans
- View planned meals at a glance

### Data Synchronization
- Automatic sync of user data to Firestore
- Offline-first approach with Room
- Conflict resolution for data changes

## 🐛 Troubleshooting

### Common Issues

1. **Firebase initialization error**
   - Ensure `google-services.json` is in the `app/` directory
   - Verify package name matches Firebase configuration

2. **API not responding**
   - Check internet connection
   - Verify TheMealDB API is accessible
   - Check Retrofit configuration

3. **Room database errors**
   - Clear app data and reinstall
   - Check database migrations

4. **Gradle sync issues**
   - Clean and rebuild project
   - Invalidate caches and restart Android Studio

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Ahmed Sala**
- GitHub: [@ahmed-sala](https://github.com/ahmed-sala)
- Project Link: [MenueMaster](https://github.com/ahmed-sala/ricipe-android)

## 🙏 Acknowledgments

- [TheMealDB](https://www.themealdb.com/) for providing the recipe API
- Firebase for backend services
- Android community for excellent libraries and tools

## 📧 Support

For support, please open an issue in the [GitHub repository](https://github.com/ahmed-sala/ricipe-android/issues) or contact through the project board.

---

**Happy Cooking! 🍳**
