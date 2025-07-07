# cafe-core

**IOC Framework**

[GitHub Repository](https://github.com/taranix81/cafe-core)

## Overview

cafe-core is an Inversion of Control (IOC) framework written in Java. It is structured as a multi-module Maven project, providing a core set of features for dependency injection and shell-based utilities.

## Features

- Modular design with separate components:
  - **cafe-beans**: Core IOC/bean management functionality.
  - **cafe-shell**: Shell utilities and command-line support.
- Built using Java 17.
- Supports modern development with libraries such as Guava, JUnit, Lombok, Logback, SnakeYAML, and Apache Commons.

## Project Structure

```
cafe-core/
├── cafe-beans/    # Core IOC/bean features
│   └── pom.xml
├── cafe-shell/    # Shell/CLI utilities
│   └── pom.xml
├── pom.xml        # Parent Maven configuration
└── .gitignore
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Build

Clone the repository and build with Maven:

```bash
git clone https://github.com/taranix81/cafe-core.git
cd cafe-core
mvn clean install
```

### Modules

- **cafe-beans**  
  Provides the core dependency injection and bean lifecycle management.

- **cafe-shell**  
  Implements shell/CLI integrations, depending on `cafe-beans`.

## Dependencies

Out-of-the-box dependencies include:

- [Google Guava](https://github.com/google/guava)
- [JUnit 5](https://junit.org/junit5/)
- [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)
- [Lombok](https://projectlombok.org/)
- [Logback](http://logback.qos.ch/)
- [SnakeYAML](https://bitbucket.org/asomov/snakeyaml)
- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)

## Contributing

Contributions are welcome! Please fork the repo and submit pull requests.

## License

Currently, no license has been specified. Please contact the repository owner for more information.

## Author

- [taranix81](https://github.com/taranix81)