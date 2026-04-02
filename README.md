# CodeForces Android Companion App 🚀

Welcome to the **CodeForces Android Companion App**! This open-source mobile application allows competitive programmers to interact with CodeForces directly from their Android phones. You can easily browse problems, track upcoming contests, and monitor user profiles!

## 🌟 Key Features

- **Profile Tracking:** Enter a CodeForces handle to dynamically load their avatar, rank, rating statistics, and recent activity.
- **Problem Set Browser:** Scroll through the historical Codeforces problem archive. Filter problems dynamically by difficulty (Minimum & Maximum Rating) and by algorithmic Tags (Math, Greedy, Graph Theory, etc.).
- **Contest Dashboard:** Stay up-to-date with all `Upcoming`, `Ongoing`, and `Finished` Codeforces contests. 
- **Contest Reminders:** Automatically schedule local notification alarms before a contest starts so you never miss another rating opportunity!
- **Deep Linking:** Launch contests or problem statements seamlessly.
- **Theme Support:** Fully optimized components utilizing modern Material Design.

## 🛠️ Tech Stack 

This application is built natively for Android using modern best practices and libraries:

- **Language:** Fully written in [Kotlin](https://kotlinlang.org/)
- **Networking:** [Retrofit2](https://square.github.io/retrofit/) + GSON Converter for robust API consumption
- **UI Architecture:** ViewBinding for type-safe layout inflation
- **Image Loading:** [Glide](https://github.com/bumptech/glide) for profile pictures and fast caching
- **Components:** Android Jetpack libraries, Recycler views, and Material Components. 
- **API Source:** Official [CodeForces Public API](https://codeforces.com/apiHelp)

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer recommended)
- Java 11
- Minimum SDK: 24 (Android 7.0)

### Installation
1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/Kishan8548/CodeForces.git
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle download and sync all the necessary dependencies.
4. Click the `▶ Run` button to install the app on your selected emulator or physical device!

---

## 🤝 Contributing

We enthusiastically welcome community contributions! Whether it's fixing a UI bug, cleaning up architecture, or adding a new feature—we want your help.

Please read our detailed [CONTRIBUTING.md](./CONTRIBUTING.md) guide before you start. It explains exactly how to submit a Pull Request successfully!

## 📜 License

[MIT License](LICENSE)
