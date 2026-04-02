# Contributing to CodeForces Android

First of all, thank you for considering contributing to the CodeForces Android App! It is people like you that make FOSS Weekend a great event. 

This document provides guidelines and instructions for contributing to this repository.

## Table of Contents
1. [Getting Started](#getting-started)
2. [Finding an Issue](#finding-an-issue)
3. [Making Changes](#making-changes)
4. [Pull Request Guidelines](#pull-request-guidelines)
5. [Code Style & Best Practices](#code-style--best-practices)

## Getting Started

1. **Fork the repository**: Click the "Fork" button in the upper right corner of the GitHub repo to create your own copy.
2. **Clone your fork**:
   ```bash
   git clone https://github.com/<your-username>/CodeForces.git
   ```
3. **Open the project in Android Studio**:
   - Launch Android Studio.
   - Select **Open an existing Android Studio project**.
   - Navigate to the folder you cloned and select it.
4. **Sync Gradle**: Wait for Gradle to sync and download all dependencies (Retrofit, Glide, etc.).
5. **Run the app**: Connect your Android device or start an emulator, and press Run (`Shift + F10`).

## Finding an Issue

- If you are participating in **FOSS Weekend**, look for issues labeled with `foss-weekend`, `good first issue`, `easy`, `medium`, or `hard`.
- Read the issue description carefully to understand the requirements.
- **Claim the issue**: Comment on the issue asking to be assigned (e.g., "I'd like to work on this!"). Please wait until an maintainer assigns you before starting work to avoid duplicate efforts.

## Making Changes

1. **Create a new branch** for your feature or bug fix:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-number-description
   ```
2. Make your code changes in Android Studio.
3. Test your changes locally to ensure the app builds successfully and your change works as expected without causing regressions.
4. **Commit your changes**. Write clear, concise commit messages identifying what changed:
   ```bash
   git commit -m "Fix memory leak in ProblemsFragment by clearing adapter"
   ```

## Pull Request Guidelines

Before submitting a Pull Request (PR), please ensure that your PR meets these requirements:
- **Reference the assigned issue**: In your PR description, use closing keywords like `Fixes #123` or `Resolves #456`.
- **Keep it focused**: A single PR should address a single issue. Avoid bundling unrelated changes (like formatting a file not related to your feature) in one PR.
- **Provide screenshots/videos**: If your change affects the UI, attach screenshots or a screen recording showing the before and after states. This drastically speeds up the review process!
- Wait for reviews. Maintainers will review your PR and may request changes. Be open to feedback!

## Code Style & Best Practices

- This project is written in **Kotlin**. Please follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **No hardcoded strings**: If you are adding text to the UI, define it in `res/values/strings.xml` rather than hardcoding it in XML layouts or Kotlin files.
- **Use ViewBinding**: This project uses ViewBinding. Ensure you do not use `findViewById` unless necessary.
- **Clean Architecture**: Place files in their correct packages (`models`, `ui`, `api`, `adapter`).

### Happy Coding!
If you have any questions, feel free to reach out to the repository maintainers during the FOSS Weekend event.
