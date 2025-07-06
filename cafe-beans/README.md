# Cafe-Beans: Lightweight Java IoC with Annotation-based Property Injection

Cafe-Beans is a lightweight Java library implementing Inversion of Control (IoC) and dependency injection using annotations. It supports property injection, allowing you to easily inject configuration values directly into your classes.

---

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Installation](#installation)
4. [Getting Started](#getting-started)
5. [Annotations](#annotations)
6. [Examples](#examples)
7. [Configuration](#configuration)
8. [Advanced Features](#advanced-features)
9. [License](#license)
10. [Support](#support)

---

## Introduction

Cafe-Beans provides a simple, annotation-driven IoC framework for Java. It manages the lifecycle and dependencies of your components, making your application easier to maintain, configure, and extend.

---

## Features

- **Annotation-based Dependency Injection**: Use annotations for cleaner, boilerplate-free DI.
- **Property Injection**: Inject configuration values (strings, numbers, etc.) directly from properties files.
- **Configuration File Support**: Load values from `application.properties` or YAML automatically.
- **Lightweight**: No heavy dependencies or XML configuration required.
- **Extensible**: Create custom annotations and providers.

---

## Installation

### Maven

```xml
<dependency>
    <groupId>org.taranix.cafe</groupId>
    <artifactId>cafe-beans</artifactId>
    <version>0.0.4-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
implementation 'org.taranix.cafe:cafe-beans:0.0.4-SNAPSHOT'
```

### Manual Download

Download the latest JAR from the [Releases page](https://github.com/taranix81/cafe-core/releases) and add it to your classpath.

---

## Getting Started

### 1. Define a Service

Annotate your service class with `@CafeService` and its dependencies with `@CafeInject` or `@CafeProperty`.

```java
@CafeService
public class AppSettings {
    @CafeProperty(name = "app.username")
    private String username;

    @CafeProperty(name = "app.environment")
    private String environment;

    public void printSettings() {
        System.out.println("Username: " + username);
        System.out.println("Environment: " + environment);
    }
}
```

### 2. Create a Configuration Class

Use `@CafeFactory` for configuration providers.

```java
@CafeFactory
public class AppConfig {
    @CafeProvider
    public Double primeNumber() {
        return 13.0d;
    }

    @CafeProvider
    @CafeName(name = "second")
    public Double secondPrime() {
        return 17.0d;
    }
}
```

### 3. Provide Application Properties

Create `application.properties`:
```properties
app.username=admin
app.environment=production
```

### 4. Create a root application configuration class

Every Cafe-Beans application requires a class annotated with `@CafeApplication`.  
This class acts as the root for component scanning and initialization.

```java
@CafeApplication
public class MainConfigApp {
    // You can declare additional configuration or leave empty.
}
```

- Annotate your configuration class with `@CafeApplication`.
- Pass this class to your application entry point (see MainApp example above).

### 5. Application Entry Point

Extend `CafeApplication` in your main class:

```java
public class MainApp extends CafeApplication {
    @CafeInject
    private AppSettings appSettings;

    @Override
    protected int execute(String[] args) {
        appSettings.printSettings();
        return 0;
    }

    public static void main(String[] args) {
        MainApp mainApp = new MainApp(MainConfigApp.class);
        mainApp.run(args);
    }
}
```

---

## Annotations

- `@CafeService`: Marks a class as a service/component. There are 2 scopes: Singleton/ Prototype
- `@CafeInject`: Field  injection.
- `@CafeProperty(name = "...")`: Injects a property value.
- `@CafeFactory`: Marks a configuration/provider class.
- `@CafeProvider`: Marks a bean provider method.
- `@CafeName(name = "...")`: Assigns a custom bean ID.
- `@CafePrimary`: Marks a bean as the primary instance for its type.
- `@CafeApplication`: Marks the root configuration class.
- `@CafeOptional` : Marks field that injection can be null
- `@CafePostInit` : Marks method in root application configuration class to be executed after application context is initialized

---

## Examples

### application.properties

```properties
app.username=admin
app.environment=production
```

### Service and Application Classes

See [Getting Started](#getting-started).

#### Example Output

```
Username: admin
Environment: production
```

---

## Configuration

By default, Cafe-Beans loads `application.properties` from the classpath. 

## Advanced Features

- **Custom Annotations**: Extend Cafe-Beans with your own injection logic.
- **Mixed Injection**: Combine property and object injection.
- **Scope Management**: (Upcoming) Support for singleton/prototype scopes.

---

## License

This project is licensed under the MIT License. See [LICENSE](../LICENSE) for details.

---

## Support

For questions, open an issue in the [GitHub repository](https://github.com/taranix81/cafe-core/issues) or contact support@example.com.

Enjoy building with Cafe-Beans! ☕️
