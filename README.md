# Library Documentation: Java Dependency Injection Library (IOC with Property Injection)

This document describes a Java-based library designed to implement Inversion of Control (IoC), with support for injecting properties into classes using annotations. The library simplifies dependency injection for Java applications and promotes loose coupling between components.

---

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Installation](#installation)
4. [Getting Started](#getting-started)
5. [Annotations](#annotations)
6. [Examples](#examples)
7. [Configuration](#configuration)
8. [License](#license)

---

## Introduction

This library provides a lightweight IoC framework for Java, enabling dependency injection through annotations. It includes property injection, which allows easy assignment of configuration values (e.g., from a configuration file) to fields in a class.

With IoC, the lifecycle and dependencies of objects are managed by the framework. This makes applications easier to maintain and extend.

---

## Features

- **Annotation-based Dependency Injection**: Annotate fields, constructors, and classes for dependency injection.
- **Property Injection**: Inject values like strings, numbers, and environmental properties into fields.
- **Configuration Support**: Load properties from external configuration files into your Java components automatically.
- **Lightweight Design**: No large dependencies or complex configurations—easy to add to any Java project.

---

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>ioc-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```gradle
implementation 'com.example:ioc-library:1.0.0'
```

### Manual Download

Download the compiled library JAR from [here](https://example.com/download) and include it in your classpath.

---

## Getting Started

### 1. Define a Class with Dependencies
Create a class and annotate fields with `@Inject` to specify the dependent values. Use `@Value` for property injection.

### 2. Configure the Properties
Define a properties file (`application.properties`) that contains the configuration values needed by your application.

### 3. Bootstrap the IoC Container
Initialize the IoC container to scan and manage your classes and dependencies.

---

## Annotations

### 1. `@Inject`

Indicates that a field or constructor should be injected with a dependency.

Example:
```java
@Inject
private ServiceClass service;
```

### 2. `@Value`

Used to inject a property value into a field. The property key is specified as the annotation value.

Example:
```java
@Value("app.username")
private String username;
```

### 3. `@Component`

Marks a class as a managed component. These classes are scanned and instantiated by the IoC container.

Example:
```java
@Component
public class MyComponent {
    // ...
}
```

### 4. `@Configuration`

Indicates a configuration class that provides bean definitions or other settings.

Example:
```java
@Configuration
public class AppConfig {
    // Bean definitions
}
```

---

## Examples

### Define a Component with Property Injection

Here's an example of injecting properties into a class using annotations:

#### 1. Application Properties
Define the properties in a file named `application.properties`:
```properties
app.username=admin
app.environment=production
```

#### 2. Create a Class with Dependencies
```java
@Component
public class AppSettings {

    @Value("app.username")
    private String username;

    @Value("app.environment")
    private String environment;

    public void printSettings() {
        System.out.println("Username: " + username);
        System.out.println("Environment: " + environment);
    }
}
```

#### 3. Main Application
```java
public class MainApp {
    public static void main(String[] args) {
        IoCContainer container = new IoCContainer("application.properties");
        
        // Retrieve managed component
        AppSettings appSettings = container.getBean(AppSettings.class);
        appSettings.printSettings();
    }
}
```

#### Output
```
Username: admin
Environment: production
```

---

## Configuration

### Property Loader
The library utilizes a properties file (e.g., `application.properties`) to load configuration values. The default file name is `application.properties`, but you can specify a custom file name when initializing the IoC container.

### IoCContainer Initialization
To initialize the IoC container, provide a base package for component scanning and a path to the properties file.

Example:
```java
IoCContainer container = new IoCContainer("com.example.myapp", "config/settings.properties");
```

---

## Advanced Features

1. **Custom Annotations**: Extend the library by creating custom annotations for additional configuration or injection logic.
2. **Mixed Configurations**: Mix property injection and object injection seamlessly.
3. **Scope Management**: (Upcoming) Add support for different bean scopes (e.g., singleton, prototype).

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

For any questions, visit our [GitHub Repository](https://github.com/example/ioc-library) or contact support@example.com.

Enjoy coding with simplicity! ❤️