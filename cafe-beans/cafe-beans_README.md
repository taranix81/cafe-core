# Cafe-Beans: Lightweight Java IoC with Annotation-based Property Injection

[![Build Status](https://github.com/taranix81/cafe-core/actions/workflows/build.yml/badge.svg)](https://github.com/taranix81/cafe-core/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

## Overview

Cafe-Beans is a Java library for lightweight Inversion of Control (IoC) and dependency injection. It uses annotations for property and service injection, minimizing configuration and boilerplate for your Java projects.

---

## Table of Contents

1. [Quickstart](#quickstart)
2. [Introduction](#introduction)
3. [Features](#features)
4. [Installation](#installation)
5. [Getting Started](#getting-started)
6. [Annotation Reference](#annotation-reference)
7. [Examples](#examples)
8. [Configuration](#configuration)
9. [Advanced Features](#advanced-features)
10. [Troubleshooting & FAQ](#troubleshooting--faq)
11. [License](#license)
12. [Support](#support)
13. [Contributing](#contributing)

---

## Quickstart

```java
@CafeService
public class HelloService {
    @CafeProperty(name = "hello.message")
    private String message;

    public void sayHello() {
        System.out.println(message);
    }
}

@CafeApplication
public class MyAppConfig {}

public class Main extends CafeApplication {
    @CafeInject
    private HelloService helloService;

    @Override
    protected int execute(String[] args) {
        helloService.sayHello();
        return 0;
    }

    public static void main(String[] args) {
        new Main(MyAppConfig.class).run(args);
    }
}
```
`application.properties`:
```
hello.message=Hello, World!
```

---

## Introduction

Cafe-Beans provides a simple, annotation-driven IoC framework for Java. It manages the lifecycle and dependencies of your components, making your application easier to maintain, configure, and extend.

The central engine of the framework is the `CafeApplication` class. `CafeApplication` is a stub class designed to be extended by users to define their own application logic. Each extension of `CafeApplication` can be initialized with a different root configuration class.

---

## Features

- **Annotation-based Dependency Injection**: Use annotations for cleaner, boilerplate-free DI.
- **Property Injection**: Inject configuration values (strings, numbers, etc.) directly from properties files.
- **Configuration File Support**: Load values from `application.properties` or YAML automatically.
- **Lightweight**: No heavy dependencies or XML configuration required.
- **Extensible**: Create custom annotations and providers.
- **Customizable Application Engine:** Extend the `CafeApplication` class to define your application logic.
- **Flexible Configuration:** Easily reuse and modularize through root configuration classes.
- **Custom Injection Mechanisms:** Easily extend the framework by creating your own injection logic.

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

### 2. Create a Beans Factory Configuration

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

```
app.username=admin
app.environment=production
```

### 4. Create a Root Application Configuration Class

Every Cafe-Beans application requires a class annotated with `@CafeApplication`.
This class acts as the root for component scanning and initialization.

```java
@CafeApplication
public class MainConfigApp {
    @CafePostInit
    public void afterInit() {
        System.out.println("Application context initialized!");
    }
}
```

- Annotate your configuration class with `@CafeApplication`.
- Pass this class to your application entry point.

### 5. Application Entry Point

To start your application, extend `CafeApplication` and implement your business logic in the `execute` method. Pass your root configuration class (annotated with `@CafeApplication`) to the constructor.

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

## Annotation Reference

| Annotation           | Target        | Description                                             | Example                                   |
|----------------------|--------------|---------------------------------------------------------|-------------------------------------------|
| `@CafeService`       | Class        | Marks a class as a service/component (singleton/prototype). | `@CafeService`                            |
| `@CafeInject`        | Field        | Inject another bean                                     | `@CafeInject private Foo foo;`            |
| `@CafeProperty`      | Field        | Injects a property value from properties file           | `@CafeProperty(name="key")`               |
| `@CafeFactory`       | Class        | Marks a configuration/provider class                    | `@CafeFactory`                            |
| `@CafeProvider`      | Method       | Marks a bean provider method                            | `@CafeProvider public X makeX() {...}`    |
| `@CafeName`          | Method/Field | Assigns a custom bean ID                               | `@CafeName(name="special")`               |
| `@CafePrimary`       | Field/Method | Marks a bean as the primary instance for its type       | `@CafePrimary`                            |
| `@CafeApplication`   | Class        | Marks the root configuration class                      | `@CafeApplication`                        |
| `@CafeOptional`      | Field        | Marks a field as optional for injection (can be null)   | `@CafeInject @CafeOptional private Dep d;`|
| `@CafePostInit`      | Method       | Post-initialization hook for root config class          | `@CafePostInit public void afterInit() {}`|

---

## Examples

See [Getting Started](#getting-started) above for a full sample.

#### Example Output

```
Username: admin
Environment: production
Application context initialized!
```

---

## Configuration

By default, Cafe-Beans loads `application.properties` from the classpath.

- To use YAML, place an `application.yml` in the classpath.
- To override or provide custom property sources, implement custom logic in your `CafeApplication` extension or configuration class.

Example `application.properties`:
```
app.username=admin
app.environment=production
```

For full configuration options, see the [docs/](docs/) folder.

---

## Advanced Features

- **Custom Annotations:** Extend Cafe-Beans with your own injection logic.
- **Mixed Injection:** Combine property and object injection.
- **Scope Management:** Support for singleton/prototype scopes.
- **Custom Injection Resolvers:** Implement and register your own resolvers for constructors, fields, and methods to customize injection.

---

## Troubleshooting & FAQ

**Q:** Why is my property not injected?  
**A:** Ensure your field is annotated with `@CafeProperty`, and the key exists in your properties file.

**Q:** How do I add a custom injection annotation?  
**A:** See [Advanced Features](#advanced-features).

**Q:** How do I debug missing beans?  
**A:** Make sure your beans are annotated and your configuration class is set as the root.

---

## License

This project is licensed under the MIT License. See [LICENSE](../LICENSE) for details.

---

## Support

For questions, open an issue in the [GitHub repository](https://github.com/taranix81/cafe-core/issues) or contact support@example.com.

---

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

---