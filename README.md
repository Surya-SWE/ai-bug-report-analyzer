# AI Bug Report Analyzer

A powerful AI-powered Android application designed to intelligently analyze and parse crash logs from bug report files. Perfect for Android developers, QA engineers, and anyone who needs to quickly identify and understand application crashes.

## 🚀 Features

- **AI-Powered Analysis**: Smart parsing of complex crash logs with intelligent pattern recognition
- **Fatal Exception Detection**: Automatically identifies and extracts FATAL EXCEPTION blocks
- **Severity Classification**: AI-driven crash severity assessment (LOW, MEDIUM, HIGH, CRITICAL)
- **Process Information**: Extracts process names and IDs from crash logs
- **Modern UI**: Beautiful Material Design 3 interface with intuitive navigation
- **Real-time Processing**: Fast analysis of large log files with progress tracking
- **Filter & Search**: Advanced filtering by severity and intelligent search capabilities

## 🎯 Perfect For

- **Android Developers**: Quickly analyze crash reports during development
- **QA Engineers**: Efficiently process and categorize bug reports
- **DevOps Teams**: Monitor application stability and crash patterns
- **Support Teams**: Understand and communicate crash issues effectively

## 🔍 Play Store Keywords

This app is optimized for Play Store searches including:
- "bug report analyzer"
- "crash log analyzer"
- "AI bug report"
- "crash log parser"
- "Android crash analyzer"
- "log file analyzer"
- "exception analyzer"
- "debug log viewer"

## 📱 Screenshots

- **File Upload**: Easy drag-and-drop interface for log files
- **AI Analysis**: Intelligent parsing with visual progress indicators
- **Crash Details**: Comprehensive crash information with stack traces
- **Filtering**: Advanced filtering by severity and process
- **Modern UI**: Clean, intuitive Material Design 3 interface

## 🛠 Technical Features

- Built with Kotlin and Jetpack Compose
- Material Design 3 theming
- MVVM architecture with Hilt dependency injection
- Efficient file processing for large log files
- Responsive design for all screen sizes

## 📄 Supported Formats

- Text files (.txt) containing Android crash logs
- Firebase crash logs
- Android system logs
- Custom crash report formats

Download now and experience the power of AI-driven bug report analysis!

## Project Structure

```
app/src/main/java/com/example/bugreportanalyzer/
├── data/
│   ├── model/           # Data models (CrashReport, DeviceInfo, etc.)
│   └── parser/          # Bug report parsing logic
├── ui/
│   ├── components/      # Reusable UI components
│   ├── screen/          # Main screen composables
│   ├── theme/           # Material 3 theme configuration
│   └── viewmodel/       # ViewModels for state management
├── BugReportAnalyzerApp.kt  # Hilt application class
└── MainActivity.kt      # Main activity
```

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Kotlin 1.9.10 or higher

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd BugReportAnalyzer
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the project directory and select it

3. **Sync the project**:
   - Wait for Gradle sync to complete
   - If prompted, update any dependencies

4. **Build and run**:
   - Connect an Android device or start an emulator
   - Click the "Run" button or press Shift+F10

## Usage

1. **Launch the app** on your Android device
2. **Tap "Select File"** to choose a .txt file containing crash logs
3. **View the analysis results**:
   - File information (name, size, crash count)
   - List of detected crashes with severity indicators
   - Tap on any crash to view detailed information
4. **Review crash details**:
   - Exception type and message
   - Root cause analysis
   - Suggested fixes
   - Stack trace (if available)
   - Thread and device information

## Sample Bug Report Format

The app can parse bug reports in various formats. Here's an example:

```
2024-01-15 10:30:45.123
FATAL EXCEPTION: main
Process: com.example.app, PID: 12345
java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.lang.Object.toString()' on a null object reference
    at com.example.app.MainActivity.onCreate(MainActivity.java:45)
    at android.app.Activity.performCreate(Activity.java:7009)
    ...

Thread: main
Device: Samsung Galaxy S21
Android Version: 12.0
```

## Technologies Used

- **Jetpack Compose**: Modern Android UI toolkit
- **Material 3**: Latest Material Design components
- **Hilt**: Dependency injection
- **ViewModel**: State management
- **Kotlin Coroutines**: Asynchronous programming
- **Kotlinx DateTime**: Date/time handling

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture with Clean Architecture principles:

- **Presentation Layer**: UI components and ViewModels
- **Domain Layer**: Business logic and data models
- **Data Layer**: Parsing logic and data handling

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions, please create an issue in the repository or contact the development team. 