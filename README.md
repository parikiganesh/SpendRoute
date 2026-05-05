# SpendRoute 💰

A modern, offline-first expense tracking Android app built with Kotlin and Jetpack Compose. Track your income and expenses smartly, get detailed analytics, and manage your finances with ease.

[![Release](https://img.shields.io/badge/Release-v1.1.1-blue.svg)](https://github.com/parikiganesh/SpendRoute/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://www.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

---

## 📱 Features

### Core Functionality
- ✅ **Add/Edit/Delete Transactions** - Manage income and expense transactions with ease
- ✅ **Smart Categorization** - 6 income categories and 9 expense categories
- ✅ **Transaction History** - View all your transactions with timestamps
- ✅ **Search & Filter** - Find transactions by category or keyword
- ✅ **Optional Notes** - Add notes to any transaction for context

### Analytics & Insights
- 📊 **Income vs Expense Charts** - 6-month visualization using Vico charts
- 📈 **Category Breakdown** - See spending by category at a glance
- 💹 **Percentage Changes** - Compare month-over-month and year-over-year trends
- 🎯 **Monthly & Yearly Views** - Toggle between different time periods

### Smart Features
- 🔔 **Daily Reminders** - Get notified at 9 AM & 6 PM to log expenses (WorkManager)
- 💾 **Offline-First** - All data stored locally, no internet required
- 🔐 **Privacy-Focused** - No cloud sync, data never leaves your device
- 📤 **Export Data** - Download transactions as CSV or PDF reports

### User Experience
- 👤 **User Profile** - Personalized greeting with user initials
- 🎨 **Modern UI** - Built with Jetpack Compose and Material Design 3
- ⚡ **Fast & Responsive** - Optimized performance with smooth animations
- 🔄 **In-App Updates** - Automatic update detection with flexible/immediate update options

---

## 🏗️ Architecture

**Architecture Pattern:** MVVM (Model-View-ViewModel)  
**State Management:** Kotlin Flow & StateFlow  
**Database:** Room (SQLite) with automatic migrations  
**UI Framework:** Jetpack Compose  
**Background Tasks:** WorkManager for scheduling

### Project Structure

```
app/src/main/java/com/parikiganesh/spendroute/
├── data/
│   ├── local/              # Room Database
│   │   ├── dao/            # Data Access Objects
│   │   └── entity/         # Database Entities
│   ├── model/              # Domain Models & Constants
│   └── UserPreferences.kt  # SharedPreferences Wrapper
├── repository/             # Data Abstraction Layer
├── viewmodel/              # MVVM ViewModels
├── ui/
│   ├── screens/            # Composable Screens
│   ├── components/         # Reusable UI Components
│   └── theme/              # Theme & Typography
├── navigation/             # Navigation Routes
├── notifications/          # Notification Management
└── utils/                  # Utility Functions

app/src/main/res/
├── values/                 # String Resources
├── drawable/               # Drawable Assets
├── mipmap-*/               # App Icons
└── xml/                    # XML Resources
```

---

## 🛠️ Tech Stack

### Android & Core
- **Language:** Kotlin 2.2.10
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 15)
- **Gradle:** Kotlin DSL
- **KSP:** Kotlin Symbol Processing for annotations

### UI Framework
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Material components
- **Vico** - Charts and graphs

### Database & Storage
- **Room** 2.7.0 - SQLite wrapper with type safety
- **SharedPreferences** - User preferences
- **FileProvider** - Secure file sharing

### Background & Notifications
- **WorkManager** 2.11.2 - Background task scheduling
- **Google Play Core** 2.1.0 - In-app updates

### Testing
- **JUnit** - Unit testing
- **Espresso** - UI testing
- **Compose Test** - Compose UI testing

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11 or higher
- Android SDK 24+ (for testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/parikiganesh/SpendRoute.git
   cd SpendRoute
   ```

2. **Open in Android Studio**
   ```bash
   # Open the project in Android Studio
   ```

3. **Sync Gradle**
   ```
   File → Sync Now
   ```

4. **Build the project**
   ```
   Build → Make Project
   ```

5. **Run on device/emulator**
   ```
   Run → Run 'app'
   ```

---

## 💻 Usage

### Adding a Transaction
1. Tap the **"+"** button (Add tab)
2. Choose **Income** or **Expense**
3. Enter the amount
4. Select a category (Food, Travel, etc.)
5. Optionally add notes
6. Tap **Save**

### Viewing Analytics
1. Go to **Analytics** tab
2. See income vs expense charts
3. View category-wise breakdown
4. Toggle between Monthly/Yearly views

### Managing Transactions
1. Go to **Transactions** tab
2. **Search** for specific transactions
3. **Filter** by type (All/Income/Expense)
4. **Swipe left** to edit or delete
5. **Tap** to view full details

### Exporting Data
1. Go to **Profile** tab
2. Tap **Export Data**
3. Choose **CSV** or **PDF**
4. Share via your preferred app

---

## 📊 Version History

### v1.1.1 (May 5, 2026)
- ✅ Fix: Clear transactionToEdit state when returning to Add screen
- ✅ Fix: Improved back button handling
- ✅ Feature: In-app update system (Flexible & Immediate updates)
- ✅ Enhancement: Android 14 compatible (Play Core 2.1.0)

### v1.1.0 (Previous)
- ✅ Feature: Analytics with charts (6-month trends)
- ✅ Feature: Export transactions to CSV/PDF
- ✅ Feature: Daily reminders (9 AM & 6 PM)
- ✅ Enhancement: Improved notifications UI

### v1.0.0 (Initial Release)
- ✅ Core: Add/Edit/Delete transactions
- ✅ Core: Transaction history view
- ✅ Core: User profile setup
- ✅ Feature: Basic analytics

---

## 🔄 In-App Updates

SpendRoute includes a professional in-app update system:

### FLEXIBLE Updates
- **For:** Bug fixes, improvements, new features
- **User Experience:** Optional ("Update" / "Later" buttons)
- **Example:** v1.1.0 → v1.1.1

### IMMEDIATE Updates
- **For:** Critical bugs, security fixes, breaking changes
- **User Experience:** Mandatory (users must update)
- **Example:** v1.x → v2.0.0

The priority is automatically detected based on the release type.

---

## 🔐 Privacy & Security

- 🔒 **No Cloud Sync** - All data remains on device
- 🔐 **Encrypted Storage** - Room database provides encryption
- 📵 **Offline First** - Works without internet connection
- 👤 **No Analytics Tracking** - Your usage is never tracked
- 📄 **Privacy Focused** - No third-party services

---

## 📝 Features in Detail

### Transaction Management
```
Features:
- Add income and expense transactions
- Categorize by 15+ predefined categories
- Add optional notes for context
- Search and filter by keyword/category
- Edit or delete transactions
- View transaction history
```

### Analytics
```
Metrics:
- Total income and expenses
- Percentage changes (vs previous period)
- 6-month trend charts
- Category-wise spending breakdown
- Monthly and yearly views
```

### Notifications
```
Reminders:
- Morning reminder (9:00 AM)
- Evening reminder (6:00 PM)
- Personalized with user's name
- Scheduled via WorkManager
- Can be toggled on/off in settings
```

### Data Export
```
Formats:
- CSV: Spreadsheet-compatible format
- PDF: Formatted report with summary
- Includes: Date, Time, Category, Amount, Type, Notes
- Shareable via email, cloud storage, etc.
```

---

## 🎨 UI/UX

### Design System
- **Primary Color:** #5B4B9B (Purple)
- **Accent Color:** #4DB8A8 (Teal)
- **Success:** #4CAF50 (Green)
- **Error:** #E53935 (Red)
- **Background:** #F8F7FC (Light Purple)

### Screens
1. **Splash** - App intro with features
2. **Onboarding** - User name setup
3. **Home** - Balance overview & recent transactions
4. **Transactions** - Full transaction list with filters
5. **Add/Edit** - Transaction form
6. **Analytics** - Charts and insights
7. **Profile** - Settings and data management

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests (on device/emulator)
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Test on Android 7.0 (Min SDK)
- [ ] Test on Android 15 (Latest)
- [ ] Test add/edit/delete transactions
- [ ] Test search and filter
- [ ] Test export to CSV/PDF
- [ ] Test notifications
- [ ] Test in-app updates

---

## 📦 Dependencies

### Core Android
- androidx.core:core-ktx:1.18.0
- androidx.activity:activity-compose:1.13.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.10.0

### Compose
- androidx.compose.ui:ui
- androidx.compose.material3:material3
- androidx.compose.material:material-icons-extended

### Database
- androidx.room:room-runtime:2.7.0
- androidx.room:room-ktx:2.7.0

### Features
- androidx.work:work-runtime-ktx:2.11.2
- com.patrykandpatrick.vico:compose:1.13.0
- com.google.android.play:app-update:2.1.0

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin style guide
- Use meaningful commit messages
- Test on multiple Android versions
- Keep UI responsive
- Document complex logic

---

## 🐛 Known Issues & Roadmap

### Known Issues
- None currently (v1.1.1)

### Planned Features (v1.2.0+)
- [ ] Dark mode support
- [ ] Recurring transactions
- [ ] Budget limits with alerts
- [ ] Multi-language support
- [ ] Cloud backup (optional)
- [ ] Share reports with others
- [ ] Expense forecasting

### Future Versions
- **v1.2.0** - Enhanced features
- **v1.3.0** - Budget management
- **v2.0.0** - Complete redesign with sync

---

## 📞 Support & Contact

- **Issues:** [GitHub Issues](https://github.com/parikiganesh/SpendRoute/issues)
- **Email:** [Your Email]
- **GitHub:** [@parikiganesh](https://github.com/parikiganesh)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Ganesh Pariki

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Room Database](https://developer.android.com/training/data-storage/room) - Type-safe database access
- [Vico Charts](https://github.com/patrykandpatrick/vico) - Beautiful charts
- [Material Design 3](https://m3.material.io/) - Design system
- [Google Play Core](https://developer.android.com/guide/playcore) - In-app updates

---

## 📱 Screenshots

### Home Screen
Displays user greeting, current balance, and recent transactions.

### Analytics
Shows income vs expense trends with category breakdown.

### Transaction Management
Add, edit, delete transactions with easy-to-use forms.

### Profile & Settings
Manage notifications, export data, and clear records.

---

## 📈 Stats

- **Lines of Code:** 5000+
- **Kotlin Files:** 20+
- **Composable Screens:** 7
- **ViewModels:** 8
- **Database Entities:** 1 (Transaction)
- **Minimum Target:** Android 7.0 (API 24)
- **Maximum Target:** Android 15 (API 36)

---

**Made with ❤️ by [Pariki Ganesh Kumar Reddy](https://github.com/parikiganesh)**

Last Updated: May 5, 2026  
Version: 1.1.1

