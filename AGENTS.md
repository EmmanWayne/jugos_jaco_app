# AGENTS.md — Project metadata for AI agents

## Tech Stack
- **Runtime / Language**: Java 11 / Kotlin
- **Framework**: Android SDK (Target SDK 35, Min SDK 29)
- **UI Library**: Material Components for Android (Material Design 2/3)
- **Networking**: Retrofit 2 + OkHttp + Volley
- **Graphics / Image Loading**: Glide, PhotoView
- **Lifecycle**: ViewModel & LiveData (Jetpack)
- **Navigation**: Jetpack Navigation Component
- **View Binding**: Enabled for layout interaction
- **Package Manager**: Gradle (Kotlin DSL / .kts)

## Folder Structure
- `app/src/main/java/com/jugos_jaco_app/` # Root package for Activities and core logic
- `app/src/main/java/com/jugos_jaco_app/ui/` # Feature-based UI components (Activities, Fragments)
- `app/src/main/java/com/jugos_jaco_app/ui/models/` # Data models (POJOs)
- `app/src/main/java/com/jugos_jaco_app/ui/api/` # Retrofit/Volley API definitions
- `app/src/main/java/com/jugos_jaco_app/ui/adapters/` # RecyclerView Adapters
- `app/src/main/java/com/jugos_jaco_app/ui/utilities/` # Helper classes and utility functions
- `app/src/main/res/layout/` # XML layout files

## Naming Conventions
- **Classes (Activities, Fragments, Models)**: PascalCase (`MainActivity.java`, `User.java`)
- **Layout Files**: snake_case (`activity_main.xml`, `fragment_login.xml`)
- **Resource IDs**: camelCase (`loginButton`, `etPassword`)
- **Adapters**: PascalCase with `Adapter` suffix (`ProductAdapter.java`)
- **Drawables/Resources**: snake_case (`logo_jugos_jaco.png`)

## Architecture Patterns
- **MVVM / MVC**: Using ViewModels and LiveData for state management.
- **Feature-based Packaging**: UI components organized by feature under the `ui/` directory.
- **View Binding**: All layouts accessed via generated binding classes to ensure type safety.
- **Navigation Graph**: Fragment transitions managed via Jetpack Navigation component.

## Test Conventions (NO IMPLEMENTED YET)
- **Unit Tests**: Located in `app/src/test/` using JUnit 4.
- **Instrumentation Tests**: Located in `app/src/androidTest/` using Espresso and AndroidX Test.
- **Run Unit Tests**: `./gradlew test`
- **Run Instrumentation Tests**: `./gradlew connectedAndroidTest`

## Key Third-Party Abstractions
- **Glide**: Core library for asynchronous image loading and caching.
- **Retrofit**: Primary abstraction for RESTful API communication.
- **Volley**: Secondary/Legacy networking library for specific requests.
- **PhotoView**: Used for implementing zoomable image galleries.
- **Material Components**: Standard UI building blocks following Material Design guidelines.
