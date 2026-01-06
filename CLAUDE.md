# Wheel of Pasiva - Project Guidelines

This is a Kotlin Multiplatform project with Firebase integration.

## Architecture Principles

### MVVM (Model-View-ViewModel) Pattern
- **ALWAYS** use MVVM architecture for UI components
- **Model**: Data classes and business logic
- **View**: UI layer (Compose/SwiftUI/Native views)
- **ViewModel**: UI state management and business logic coordination
- ViewModels should expose UI state through StateFlow or LiveData
- Views should only observe state and dispatch user actions to ViewModels
- No business logic in Views

### SOLID Principles
Follow SOLID principles in all code:
- **S**ingle Responsibility: Each class should have one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes must be substitutable for their base types
- **I**nterface Segregation: Many specific interfaces are better than one general interface
- **D**ependency Inversion: Depend on abstractions, not concretions

### Clean Architecture
Organize code in layers with clear separation of concerns:
- **Presentation Layer**: UI components, ViewModels, UI state
- **Domain Layer**: Use cases, business logic, domain models
- **Data Layer**: Repositories, data sources, API clients, database

**Dependency Rule**: Dependencies should point inward (Presentation → Domain ← Data)
- Domain layer should have NO dependencies on other layers
- Use dependency injection for loose coupling

### Design Patterns
Use appropriate design patterns when they solve real problems:
- **Repository Pattern**: For data access abstraction
- **Factory Pattern**: For object creation complexity
- **Observer Pattern**: For reactive state management
- **Strategy Pattern**: For interchangeable algorithms
- **Dependency Injection**: For loose coupling and testability
- Only use patterns when they add value, avoid over-engineering

## Gradle Management

### Critical Rule: Gradle Sync
**ALWAYS sync the project with Gradle files after ANY changes to Gradle files**

When you modify any of these files:
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `libs.versions.toml`
- Any module-level `build.gradle.kts`

**You MUST run:**
```bash
./gradlew --refresh-dependencies
```

Or if the user has a specific sync command, use that.

## Code Quality Standards

### Kotlin Best Practices
- Use immutable data structures (`val` over `var`)
- Leverage Kotlin's null safety features
- Use coroutines for asynchronous operations
- Prefer extension functions for utility methods
- Use sealed classes for state representation

### Testing
- Write unit tests for ViewModels and use cases
- Use test doubles (mocks, fakes) for dependencies
- Maintain test coverage for business logic

### Firebase Integration
- Keep Firebase-specific code in the Data layer
- Use repository pattern to abstract Firebase operations
- Handle Firebase exceptions appropriately
- Consider offline capabilities

## Project Structure Example

```
commonMain/
├── data/
│   ├── repository/
│   ├── datasource/
│   └── model/
├── domain/
│   ├── usecase/
│   ├── model/
│   └── repository/
└── presentation/
    ├── viewmodel/
    ├── state/
    └── ui/
```

## Notes
- This is a Kotlin Multiplatform project - ensure code works across platforms
- Follow platform-specific guidelines when necessary for iOS/Android
- Keep shared code truly platform-agnostic
