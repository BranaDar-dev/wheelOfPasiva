# Architecture Guidelines

This document outlines the architectural principles, design patterns, and development practices for the Wheel of Pasiva project.

## Table of Contents
1. [MVVM Architecture](#mvvm-architecture)
2. [SOLID Principles](#solid-principles)
3. [Design Patterns](#design-patterns)
4. [Test-Driven Development (TDD)](#test-driven-development-tdd)
5. [Architecture Flow Diagram](#architecture-flow-diagram)

---

## MVVM Architecture

### Overview
MVVM (Model-View-ViewModel) separates the UI from business logic, making the codebase more maintainable and testable.

### Core Components

#### 1. **Model**
- Represents data and business logic
- Should be platform-agnostic (commonMain)
- Contains data classes, entities, and business rules
- No direct dependency on UI or ViewModel

**Rules:**
- ✅ Models should be pure data classes or sealed classes
- ✅ Business logic should be in Use Cases or Repository layer
- ✅ Models should be immutable when possible
- ❌ Models should NOT contain UI-related code
- ❌ Models should NOT have direct references to ViewModels

**Example Structure:**
```
commonMain/kotlin/
  └── domain/
      ├── model/
      │   ├── User.kt
      │   └── GameState.kt
      └── usecase/
          └── GetUserUseCase.kt
```

#### 2. **View**
- Represents the UI layer (Compose screens)
- Observes ViewModel state
- Sends user actions to ViewModel
- Should be as "dumb" as possible

**Rules:**
- ✅ Views should only handle UI rendering and user input
- ✅ Views should observe ViewModel's StateFlow/State
- ✅ Views should call ViewModel functions for user actions
- ❌ Views should NOT contain business logic
- ❌ Views should NOT directly access repositories or data sources
- ❌ Views should NOT perform data transformations

**Example:**
```kotlin
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is GameUiState.Loading -> LoadingIndicator()
        is GameUiState.Success -> GameContent(state.data)
        is GameUiState.Error -> ErrorMessage(state.message)
    }
}
```

#### 3. **ViewModel**
- Acts as a bridge between View and Model
- Holds UI-related state
- Processes user actions
- Exposes state to the View

**Rules:**
- ✅ ViewModels should expose StateFlow/State for UI state
- ✅ ViewModels should contain only UI-related logic
- ✅ ViewModels should use Use Cases for business operations
- ✅ ViewModels should handle state management (loading, error, success)
- ❌ ViewModels should NOT contain business logic
- ❌ ViewModels should NOT have direct references to Views
- ❌ ViewModels should NOT perform heavy computations (use coroutines)

**Example:**
```kotlin
class GameViewModel(
    private val getGameStateUseCase: GetGameStateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    fun loadGame() {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            try {
                val gameState = getGameStateUseCase()
                _uiState.value = GameUiState.Success(gameState)
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### MVVM Directory Structure

```
commonMain/kotlin/
  └── com/bramish/wheelofpasiva/
      ├── data/              # Data layer
      │   ├── repository/
      │   ├── datasource/
      │   └── local/
      ├── domain/            # Domain layer (Models & Use Cases)
      │   ├── model/
      │   └── usecase/
      └── presentation/       # Presentation layer (MVVM)
          ├── ui/
          │   ├── screen/
          │   ├── component/
          │   └── theme/
          └── viewmodel/
```

---

## SOLID Principles

### 1. **Single Responsibility Principle (SRP)**
A class should have only one reason to change.

**Rules:**
- ✅ Each class should have a single, well-defined purpose
- ✅ Separate concerns: data access, business logic, UI logic
- ❌ Avoid "God classes" that do too many things

**Example:**
```kotlin
// ❌ Bad: Multiple responsibilities
class UserManager {
    fun saveUser(user: User) { }
    fun validateUser(user: User) { }
    fun sendEmail(user: User) { }
    fun formatUserDisplay(user: User) { }
}

// ✅ Good: Single responsibility
class UserRepository {
    fun saveUser(user: User) { }
}

class UserValidator {
    fun validateUser(user: User) { }
}

class EmailService {
    fun sendEmail(user: User) { }
}
```

### 2. **Open/Closed Principle (OCP)**
Software entities should be open for extension but closed for modification.

**Rules:**
- ✅ Use interfaces and abstract classes for extensibility
- ✅ Prefer composition over inheritance
- ✅ Use sealed classes for state management
- ❌ Avoid modifying existing classes when adding new features

**Example:**
```kotlin
// ✅ Good: Open for extension
interface GameRepository {
    fun getGameState(): Flow<GameState>
}

class LocalGameRepository : GameRepository {
    override fun getGameState(): Flow<GameState> { }
}

class RemoteGameRepository : GameRepository {
    override fun getGameState(): Flow<GameState> { }
}
```

### 3. **Liskov Substitution Principle (LSP)**
Objects of a superclass should be replaceable with objects of its subclasses.

**Rules:**
- ✅ Subclasses should not break the contract of the base class
- ✅ Derived classes should enhance, not restrict, base class behavior
- ❌ Avoid overriding methods in ways that change expected behavior

**Example:**
```kotlin
// ✅ Good: All implementations are substitutable
interface DataSource {
    fun fetchData(): Result<Data>
}

class LocalDataSource : DataSource {
    override fun fetchData(): Result<Data> { }
}

class RemoteDataSource : DataSource {
    override fun fetchData(): Result<Data> { }
}
```

### 4. **Interface Segregation Principle (ISP)**
Clients should not be forced to depend on interfaces they don't use.

**Rules:**
- ✅ Create specific, focused interfaces
- ✅ Avoid "fat" interfaces with many methods
- ✅ Split large interfaces into smaller, cohesive ones
- ❌ Don't force classes to implement unused methods

**Example:**
```kotlin
// ❌ Bad: Fat interface
interface UserOperations {
    fun createUser(user: User)
    fun updateUser(user: User)
    fun deleteUser(user: User)
    fun sendEmail(user: User)
    fun generateReport(user: User)
}

// ✅ Good: Segregated interfaces
interface UserRepository {
    fun createUser(user: User)
    fun updateUser(user: User)
    fun deleteUser(user: User)
}

interface EmailService {
    fun sendEmail(user: User)
}

interface ReportGenerator {
    fun generateReport(user: User)
}
```

### 5. **Dependency Inversion Principle (DIP)**
High-level modules should not depend on low-level modules. Both should depend on abstractions.

**Rules:**
- ✅ Depend on abstractions (interfaces), not concrete implementations
- ✅ Use dependency injection
- ✅ ViewModels should depend on Use Cases, not Repositories directly
- ❌ Avoid direct instantiation of dependencies

**Example:**
```kotlin
// ❌ Bad: Direct dependency
class GameViewModel {
    private val repository = LocalGameRepository() // Concrete dependency
}

// ✅ Good: Dependency on abstraction
class GameViewModel(
    private val getGameStateUseCase: GetGameStateUseCase // Interface/Use Case
) : ViewModel()
```

---

## Design Patterns

### 1. **Repository Pattern**
Provides a clean API for data access, abstracting data sources.

**Implementation:**
```kotlin
interface GameRepository {
    suspend fun getGameState(): Result<GameState>
    suspend fun saveGameState(state: GameState): Result<Unit>
}

class GameRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : GameRepository {
    override suspend fun getGameState(): Result<GameState> {
        return try {
            val localState = localDataSource.getGameState()
            if (localState != null) {
                Result.success(localState)
            } else {
                val remoteState = remoteDataSource.getGameState()
                localDataSource.saveGameState(remoteState)
                Result.success(remoteState)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 2. **Use Case Pattern (Clean Architecture)**
Encapsulates business logic in a single, testable unit.

**Implementation:**
```kotlin
class GetGameStateUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(): Result<GameState> {
        return repository.getGameState()
    }
}

class SaveGameStateUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(state: GameState): Result<Unit> {
        return repository.saveGameState(state)
    }
}
```

### 3. **State Pattern (UI State Management)**
Manages UI state using sealed classes.

**Implementation:**
```kotlin
sealed class GameUiState {
    object Loading : GameUiState()
    data class Success(val gameState: GameState) : GameUiState()
    data class Error(val message: String) : GameUiState()
}
```

### 4. **Observer Pattern (StateFlow/Flow)**
ViewModel exposes state, View observes changes.

**Implementation:**
```kotlin
class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // React to state changes
}
```

### 5. **Dependency Injection Pattern**
Use constructor injection for dependencies.

**Implementation:**
```kotlin
// Define dependencies
class GameViewModel(
    private val getGameStateUseCase: GetGameStateUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase
) : ViewModel()

// Inject in View
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            getGameStateUseCase = getGameStateUseCase(),
            saveGameStateUseCase = saveGameStateUseCase()
        )
    )
)
```

### 6. **Factory Pattern**
Creates objects without specifying the exact class.

**Implementation:**
```kotlin
class GameViewModelFactory(
    private val getGameStateUseCase: GetGameStateUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(getGameStateUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

### 7. **Strategy Pattern**
Encapsulates algorithms and makes them interchangeable.

**Example:**
```kotlin
interface GameStrategy {
    fun calculateScore(state: GameState): Int
}

class EasyGameStrategy : GameStrategy {
    override fun calculateScore(state: GameState): Int { }
}

class HardGameStrategy : GameStrategy {
    override fun calculateScore(state: GameState): Int { }
}
```

---

## Test-Driven Development (TDD)

### TDD Cycle (Red-Green-Refactor)

1. **Red**: Write a failing test
2. **Green**: Write minimal code to make it pass
3. **Refactor**: Improve code while keeping tests green

### Rules for TDD

#### 1. **Write Tests First**
- ✅ Write the test before implementing the feature
- ✅ Test should fail initially (Red phase)
- ✅ One test at a time

#### 2. **Test Structure (AAA Pattern)**
```kotlin
@Test
fun `when user loads game, then game state is returned`() = runTest {
    // Arrange
    val expectedState = GameState()
    val repository = mockk<GameRepository> {
        coEvery { getGameState() } returns Result.success(expectedState)
    }
    val useCase = GetGameStateUseCase(repository)
    
    // Act
    val result = useCase()
    
    // Assert
    assertEquals(expectedState, result.getOrNull())
}
```

#### 3. **Test Naming Convention**
- Use descriptive test names: `when [condition], then [expected result]`
- Use backticks for readable test names in Kotlin

#### 4. **Test Categories**

**Unit Tests:**
- Test individual components in isolation
- Mock dependencies
- Fast execution
- Location: `commonTest/kotlin/`

**Integration Tests:**
- Test interaction between components
- Use real dependencies where appropriate
- Location: `commonTest/kotlin/integration/`

**UI Tests:**
- Test Compose UI components
- Use Compose testing framework
- Location: `commonTest/kotlin/ui/`

### Unit Test Examples

#### Testing Use Cases
```kotlin
class GetGameStateUseCaseTest {
    
    @Test
    fun `when repository returns success, then use case returns success`() = runTest {
        // Arrange
        val expectedState = GameState()
        val repository = mockk<GameRepository> {
            coEvery { getGameState() } returns Result.success(expectedState)
        }
        val useCase = GetGameStateUseCase(repository)
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedState, result.getOrNull())
    }
    
    @Test
    fun `when repository returns error, then use case returns error`() = runTest {
        // Arrange
        val error = Exception("Network error")
        val repository = mockk<GameRepository> {
            coEvery { getGameState() } returns Result.failure(error)
        }
        val useCase = GetGameStateUseCase(repository)
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
```

#### Testing ViewModels
```kotlin
class GameViewModelTest {
    
    @Test
    fun `when loadGame is called, then uiState is Loading then Success`() = runTest {
        // Arrange
        val gameState = GameState()
        val useCase = mockk<GetGameStateUseCase> {
            coEvery { invoke() } returns Result.success(gameState)
        }
        val viewModel = GameViewModel(useCase)
        
        // Act
        viewModel.loadGame()
        
        // Assert
        val states = viewModel.uiState.take(2).toList()
        assertEquals(GameUiState.Loading, states[0])
        assertEquals(GameUiState.Success(gameState), states[1])
    }
    
    @Test
    fun `when loadGame fails, then uiState is Error`() = runTest {
        // Arrange
        val error = Exception("Error message")
        val useCase = mockk<GetGameStateUseCase> {
            coEvery { invoke() } returns Result.failure(error)
        }
        val viewModel = GameViewModel(useCase)
        
        // Act
        viewModel.loadGame()
        
        // Assert
        val finalState = viewModel.uiState.value
        assertTrue(finalState is GameUiState.Error)
        assertEquals("Error message", (finalState as GameUiState.Error).message)
    }
}
```

#### Testing Repositories
```kotlin
class GameRepositoryImplTest {
    
    @Test
    fun `when local data exists, then return local data`() = runTest {
        // Arrange
        val localState = GameState()
        val localDataSource = mockk<LocalDataSource> {
            coEvery { getGameState() } returns localState
        }
        val remoteDataSource = mockk<RemoteDataSource>()
        val repository = GameRepositoryImpl(localDataSource, remoteDataSource)
        
        // Act
        val result = repository.getGameState()
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(localState, result.getOrNull())
        coVerify(exactly = 0) { remoteDataSource.getGameState() }
    }
}
```

### Test Directory Structure

```
commonTest/kotlin/
  └── com/bramish/wheelofpasiva/
      ├── domain/
      │   ├── usecase/
      │   │   └── GetGameStateUseCaseTest.kt
      │   └── model/
      ├── data/
      │   └── repository/
      │       └── GameRepositoryImplTest.kt
      └── presentation/
          └── viewmodel/
              └── GameViewModelTest.kt
```

### Testing Best Practices

1. **Isolation**: Each test should be independent
2. **Fast**: Tests should run quickly
3. **Repeatable**: Tests should produce the same results every time
4. **Self-Validating**: Tests should clearly pass or fail
5. **Timely**: Write tests before or alongside code
6. **Use Mocks**: Mock external dependencies (network, database, etc.)
7. **Test Edge Cases**: Test error scenarios, null values, empty states
8. **Maintain Test Coverage**: Aim for >80% code coverage

### Required Dependencies for Testing

```kotlin
// build.gradle.kts (commonTest)
dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.20")
    testImplementation("junit:junit:4.13.2")
}
```

---

## Architecture Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERACTION                         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                            VIEW LAYER                            │
│  (Compose UI - @Composable functions)                            │
│                                                                   │
│  • Observes ViewModel state via StateFlow                         │
│  • Sends user actions to ViewModel                               │
│  • Renders UI based on state                                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ User Actions
                             │ State Observation
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        VIEWMODEL LAYER                           │
│  (Presentation Logic)                                            │
│                                                                   │
│  • Holds UI state (StateFlow)                                    │
│  • Processes user actions                                        │
│  • Calls Use Cases for business operations                       │
│  • Transforms domain models to UI models                         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Calls Use Cases
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER                             │
│  (Business Logic)                                                │
│                                                                   │
│  ┌──────────────┐         ┌──────────────┐                      │
│  │   Use Cases  │────────▶│    Models    │                      │
│  └──────────────┘         └──────────────┘                      │
│         │                                                         │
│         │ Uses Repository Interface                              │
│         ▼                                                         │
│  ┌──────────────┐                                               │
│  │  Repository  │ (Interface)                                    │
│  │  Interface   │                                               │
│  └──────────────┘                                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Implemented by
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                              │
│  (Data Access)                                                   │
│                                                                   │
│  ┌──────────────────────────────────────────┐                   │
│  │      Repository Implementation           │                   │
│  │  (GameRepositoryImpl)                    │                   │
│  └───────┬──────────────────────┬──────────┘                   │
│          │                      │                                │
│          ▼                      ▼                                │
│  ┌──────────────┐      ┌──────────────┐                        │
│  │    Local     │      │    Remote    │                        │
│  │  DataSource  │      │  DataSource  │                        │
│  └──────────────┘      └──────────────┘                        │
│          │                      │                                │
│          ▼                      ▼                                │
│  ┌──────────────┐      ┌──────────────┐                        │
│  │   Database   │      │    API       │                        │
│  │   / Cache    │      │   Service    │                        │
│  └──────────────┘      └──────────────┘                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         TEST LAYER                               │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Unit Tests │  │ Integration  │  │    UI Tests   │          │
│  │              │  │    Tests     │  │              │          │
│  │ • Use Cases  │  │ • Repository │  │ • Compose    │          │
│  │ • ViewModels │  │ • Data Flow  │  │   Components │          │
│  │ • Repos      │  │              │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow Sequence

```
1. User Action
   ↓
2. View → ViewModel (function call)
   ↓
3. ViewModel → Use Case (invoke)
   ↓
4. Use Case → Repository (interface)
   ↓
5. Repository → DataSource (Local/Remote)
   ↓
6. DataSource → Returns Data
   ↓
7. Repository → Returns Result<Data>
   ↓
8. Use Case → Returns Result<DomainModel>
   ↓
9. ViewModel → Updates StateFlow
   ↓
10. View → Observes StateFlow → Updates UI
```

### Dependency Flow

```
View
  ↓ depends on
ViewModel
  ↓ depends on
Use Case (Interface)
  ↓ depends on
Repository (Interface)
  ↓ implemented by
RepositoryImpl
  ↓ depends on
DataSource (Interface)
  ↓ implemented by
LocalDataSource / RemoteDataSource
```

---

## Summary Checklist

### MVVM Checklist
- [ ] Models are in domain layer, no UI dependencies
- [ ] Views only handle UI rendering and user input
- [ ] ViewModels expose StateFlow for state management
- [ ] ViewModels use Use Cases, not Repositories directly
- [ ] Clear separation between layers

### SOLID Checklist
- [ ] Each class has single responsibility
- [ ] Code is open for extension, closed for modification
- [ ] Subclasses are substitutable for base classes
- [ ] Interfaces are focused and specific
- [ ] Dependencies are on abstractions, not concretions

### Design Patterns Checklist
- [ ] Repository pattern for data access
- [ ] Use Case pattern for business logic
- [ ] State pattern for UI state management
- [ ] Observer pattern (StateFlow) for state observation
- [ ] Dependency injection for all dependencies
- [ ] Factory pattern for ViewModel creation

### TDD Checklist
- [ ] Tests written before implementation
- [ ] Tests follow AAA pattern (Arrange-Act-Assert)
- [ ] Tests are isolated and independent
- [ ] All dependencies are mocked in unit tests
- [ ] Edge cases and error scenarios are tested
- [ ] Test coverage >80%

---

## References

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Test-Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)
