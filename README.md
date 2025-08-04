

````markdown
# 🧭 Navigator Platform

A modular, Kotlin-based backend platform built for powering various financial workflows at **Nivasa Finance**. This monorepo is structured to support scalable microservices and domain-driven design through clean separation of concerns.

---

## 📂 Project Structure

```plaintext
navigator-platform/
│
├── .idea/                  # IntelliJ project settings
├── .vscode/                # VSCode debug configs
├── buildSrc/               # Shared Gradle build logic
│   └── src/
│   └── build.gradle.kts
│
├── common/                 # Common utility/shared code
│   └── src/
│   └── build.gradle.kts
│
├── features/               # Domain-specific features (modular)
│   ├── advisor/
│   ├── creditbureau/
│   └── person/
│
├── main/                   # Main entrypoint and Spring Boot/Ktor App
│   └── src/
│       └── main/
│           └── java/com.nivasafinance/
│               └── NavigatorApplication.kt
│
├── gradle/                 # Gradle wrapper config
│   ├── wrapper/
│   └── libs.versions.toml
│
├── build.gradle.kts        # Root Gradle config
├── settings.gradle.kts     # Module includes
├── detekt.yml              # Static code analysis rules (Kotlin)
└── .gitignore

````

---

## 🛠️ Developer Setup (Mac + IntelliJ/VSCode)

> Follow these steps **in order** for a complete development environment.

### ✅ 1. Install Homebrew (macOS)

[https://brew.sh](https://brew.sh)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### ✅ 2. Install SDKMAN (for Java)

[https://sdkman.io/install](https://sdkman.io/install)

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.2-tem
```

### ✅ 3. Install PostgreSQL & PGAdmin

```bash
brew install postgresql@17
brew services start postgresql@17
```

> Optionally, install [pgAdmin](https://www.pgadmin.org/) as a GUI client.

Also, add PostgreSQL to your path:

```bash
echo 'export PATH="/opt/homebrew/opt/postgresql@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Create the default DB:

```bash
createdb navigator-platform
```

---

### ✅ 4. Install Git, Redis

```bash
brew install git redis
```

### ✅ 5. Setup GitHub Access

1. [Create GitHub Account](https://github.com)
2. Accept the GitHub invitation to `navigator-platform` repo
3. Generate SSH key and add it to GitHub:
   [GitHub SSH Setup Guide](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account)

```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
pbcopy < ~/.ssh/id_ed25519.pub
```

Paste into GitHub ➝ **Settings > SSH & GPG keys**

---

### ✅ 6. Clone and Run the Project

```bash
# Clone
git clone git@github.com:Nivasa-Finance/navigator-platform.git
cd navigator-platform

# Ensure DB is running
brew services start postgresql@17
createdb navigator-platform

# Clean and build project (REQUIRED on first setup)
./gradlew clean build

# Run the main service
./gradlew :main:bootRun
```

> **⚠️ Important:** Always run `./gradlew clean build` when setting up the project for the first time or after major changes. This ensures all dependencies are downloaded, Git hooks are installed, and the project compiles correctly.

---

### ✅ 7. Install IDEs

Choose one:

* [IntelliJ IDEA](https://www.jetbrains.com/idea/)
* [VSCode](https://code.visualstudio.com/)
* [Cursor](https://www.cursor.so/) (AI-powered VSCode fork)

Install Kotlin & Java extensions in VSCode.

---

## 📘 Learning Resources

> Complete this learning checklist:

| Skill             | Link                                                                                                                                           |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Git Basics        | [Git Cheat Sheet PDF](https://education.github.com/git-cheat-sheet-education.pdf)                                                              |
| Kotlin            | [Nivasa Kotlin Notes](https://docs.google.com/document/d/11o7Nu43PVLArng2KejEJD9_p282fvb-zqxgiHToa-BU/edit?usp=sharing)                        |
| PostgreSQL        | [W3Schools SQL](https://www.w3schools.com/postgresql/)                                                                                         |
| JSONB in Postgres | [Forest Admin Blog](https://www.forestadmin.com/blog/how-to-use-jsonb-to-manipulate-json-fields-in-postgresql/)                                |
| Spring Boot CRUD  | [Medium Tutorial](https://medium.com/@samuelcatalano/creating-rest-crud-api-using-kotlin-and-spring-boot-with-h2-memory-database-df8d4081e382) |

---

## 🤝 Contributing

Create a branch:

```bash
git checkout -b feature/your-feature-name
```

Commit and push:

```bash
git commit -am "Added new feature"
git push origin feature/your-feature-name
```

Create a PR from GitHub.

---

## 📬 Contact

Reach out to `tech@nivasa.in` or ask in `#tech-resources` Slack channel.

---

## 📄 License

Private & Confidential – Nivasa Finance (2025)
```
