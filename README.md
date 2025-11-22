# MiniSecDash - JavaFX Application

A network security dashboard application built with JavaFX.

## Prerequisites
- Java 21 (Eclipse Temurin)
- Maven 3.6+

## Build and Run

### Using VS Code
1. Open this workspace in VS Code
2. Press F5 or click Run/Debug to launch the application
3. Or use Ctrl+Shift+P → "Tasks: Run Task" → "maven-build" to build

### Using Maven Command Line
```powershell
# Build and run
mvn clean javafx:run

# Package for distribution
mvn clean package -DskipTests
```

### Manual Run (after build)
```powershell
# Build first
mvn clean compile dependency:copy-dependencies

# Run with JavaFX modules
java --module-path target/dependency --add-modules javafx.controls,javafx.fxml -cp target/classes MiniSecDash.Main
```

## Features
- Network device scanning
- Port monitoring
- Security dashboard with TableView
- Real-time updates using ExecutorService

## Project Structure
```
src/main/java/MiniSecDash/
  ├── Main.java              # JavaFX Application entry point
  ├── ScannerService.java    # Network scanning service
  ├── ScannerTask.java       # Background scanning task
  ├── Device.java            # Device model
  ├── Port.java              # Port model
  └── NetworkGraph.java      # Network visualization
```