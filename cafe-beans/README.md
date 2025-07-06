# Cafe-Beans Documentation: Java Dependency Injection Library (IOC with Property Injection)

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
    <artifactId>cafe-beans</artifactId>
    <groupId>org.taranix.cafe</groupId>
    <version>0.0.4-SNAPSHOT</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```gradle
implementation 'org.taranix.cafe:cafe-beans:0.0.4-SNAPSHOT'
```

### Manual Download

Download the compiled library JAR from [here]() and include it in your classpath.

---

## Getting Started

### 1. Service Class with Dependencies
Create a class and annotate it by `@CafeService`. 
- Annotate fields with `@CafeInject` to specify the dependent values. 
- Annotate fields with `@CafeProperty` for property injection.
- Annotate fields with `@CafeName` to specify bean id (works with `@CafeInject`).


### 2. Configuration Class with Providers
Create a class and annotate it by `@CafeFactory`. 
- Annotate fields with `@CafeInject` to specify the dependent values.
- Annotate fields with `@CafeProperty` for property injection.
- Annotate methods with `@CafeProvider` to specify bean creation.
- Annotate methods with `@CafeName` to specify bean id (works with `@CafeProvider`).
- Annotate methods with `@CafePrimary` to specify bean which should be dominant between many instances of the same type (works with `@CafeProvider`).



### 3. Configure the Properties
Define a properties file (`application.properties` or `application.yaml`) that contains the configuration values needed by your application.

### 4. Application Class
Create a class which extends `org.taranix.cafe.beans.CafeApplication`.  
This class will contain main logic of you application

### 5. Application configuration Class 
Create a class and annotate it `@CafeApplication`.   
This class is a root for scanning and initialize components by Cafe-Beans


---

## Annotations

### 1. `@CafeInject`

Indicates that a field or constructor should be injected with a dependency.

Example:
```java
@CafeInject
private ServiceClass service;
```

### 2. `@CafeProperty`

Used to inject a property value into a field. The property key is specified as the annotation value.

Example:
```java
@CafeProperty(name="app.username")
private String username;
```

### 3. `@CafeService`

Marks a class as a managed component. These classes are scanned and instantiated by the IoC container.

Example:
```java
@CafeService
public class MyComponent {
    // ...
}
```

### 4. `@CafeFactory`
Indicates a configuration class that provides bean definitions or other settings. Annotate method with `@CafeProvider` to initialize and manage beans

Example:
```java
@CafeFactory
public class AppConfig {
    // Bean definitions
    @CafeProvider
    Double primeNumber(){
        return 13.0d;
    }

    @CafeProvider
    @CafeName(name="second")
    Double primeNumber(){
        return 17.0d;
    }
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
@CafeService
public class AppSettings {

    @CafeProperty(name="app.username")
    private String username;

    @CafeProperty(name="app.environment")
    private String environment;

    public void printSettings() {
        System.out.println("Username: " + username);
        System.out.println("Environment: " + environment);
    }
}
```
#### 3. Main Application Config Class
```java
@CafeApplication
public class MainConfigApp{
    
}

```

#### 4. Main Application
```java
public class MainApp extends CafeApplication{
    @CafInject
    private AppSettings appSettings;
    
    protected int execute(String[] args){
        appSettings.printSettings();
    }
    
    public static void main(String[] args) {
        MainApp mainApp = new MainApp(MainConfigApp.class);
        mainApp.run(new String[]{});
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