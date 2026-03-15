# AI Agents Documentation

This folder contains comprehensive documentation for AI agents to understand and generate code for the Wofuf project. The documentation is organized by architectural layers and concerns.

## 📁 Folder Structure

### `UseCase/`
Use case generation guidelines and patterns
- **`Overview.md`** - Use case generation overview and workflow
- **`Structure.md`** - File organization and naming conventions
- **`Patterns.md`** - Code templates and implementation patterns
- **`Examples.md`** - Real-world examples from the codebase

### `Domain/`
Domain layer patterns and conventions
- **`Entities.md`** - Aggregate root and entity patterns
- **`ValueObjects.md`** - Value object implementation patterns
- **`Events.md`** - Domain event patterns and Eventuate Tram integration

### `Infrastructure/`
Infrastructure layer patterns
- **`Repositories.md`** - Repository interface and implementation patterns
- **`Mappers.md`** - Entity-DTO mapping patterns
- **`Config.md`** - Configuration patterns and API constants

### `API/`
API layer patterns and conventions
- **`Controllers.md`** - REST controller patterns and HTTP methods
- **`DTOs.md`** - Data transfer object patterns
- **`Errors.md`** - Error handling and custom exception patterns

## 🎯 Code Style Guidelines

### Kotlin Conventions
- **Package Structure**: `dev.saraki.wofuf.modules.{module}.{layer}.{feature}`
- **Class Naming**: PascalCase for classes, interfaces, and objects
- **Method Naming**: camelCase for methods and properties
- **Constant Naming**: UPPER_SNAKE_CASE for constants
- **File Naming**: Match class name with `.kt` extension

### Spring Boot Patterns
- **Dependency Injection**: Constructor injection preferred
- **Annotations**: Use Spring annotations appropriately
- **Configuration**: Centralized configuration in config classes
- **Exception Handling**: Custom exceptions extending Result.Failure

### DDD Principles
- **Layer Separation**: Strict separation between domain, application, and infrastructure
- **Domain Purity**: Keep domain logic free from infrastructure concerns
- **Value Objects**: Immutable objects for domain primitives
- **Aggregates**: Define clear aggregate boundaries
- **Event-Driven**: Domain events for significant state changes

## 🚀 Quick Start for AI Agents

1. **Read the Overview**: Start with `UseCase/Overview.md` for the big picture
2. **Understand Structure**: Review `UseCase/Structure.md` for file organization
3. **Study Patterns**: Examine `UseCase/Patterns.md` for code templates
4. **Reference Examples**: Look at `UseCase/Examples.md` for real implementations
5. **Deep Dive**: Explore specific layer documentation as needed

## 📚 Key Reference Files

### Core Shared Classes
- `shared/core/Result.kt` - Result monad for error handling
- `shared/core/UseCase.kt` - Base use case interface
- `shared/core/Guard.kt` - Input validation utilities
- `shared/domain/AggregateRoot.kt` - Base aggregate root class
- `shared/domain/ValueObject.kt` - Base value object class

### Module Structure Examples
- `modules-players/` - Complete player management module
- `modules-forum/` - Complex forum with posts and comments
- `modules-users/` - Authentication and user management

## 🔧 Development Workflow

### Code Generation Process
1. **Analyze Requirements** - Understand the business need
2. **Identify Patterns** - Find similar existing implementations
3. **Apply Templates** - Use provided patterns and templates
4. **Customize Logic** - Adapt to specific business requirements
5. **Test & Validate** - Ensure compilation and functionality

### Quality Assurance
- **Compilation**: Always verify code compiles successfully
- **Consistency**: Match existing code style and patterns
- **Testing**: Include appropriate error handling and edge cases
- **Documentation**: Update relevant documentation for new patterns

## 🎯 Best Practices

### For AI Code Generation
- **Follow Templates Exactly**: Use provided templates as starting points
- **Maintain Consistency**: Match naming conventions and structure
- **Include Error Handling**: Always handle potential failure cases
- **Use Proper Imports**: Import only necessary classes and packages
- **Document Assumptions**: Note any assumptions made during generation

### Common Patterns to Remember
- **Result<T> for Operations**: All operations return Result<T>
- **Private Constructors**: Domain objects use private constructors
- **Companion Object Creation**: Use companion objects for factory methods
- **DTO Separation**: Never expose domain objects directly in APIs
- **Layer Isolation**: Keep domain logic separate from infrastructure

## 📞 Support

- **Pattern Questions**: Refer to specific layer documentation
- **Implementation Examples**: Check existing use cases in modules
- **Architecture Decisions**: Review README.md and AGENTS.md
- **Code Style Issues**: Compare with existing implementations

---

**Remember**: Consistency is key in this codebase. Always examine existing implementations and follow established patterns to maintain code quality and architectural integrity.
