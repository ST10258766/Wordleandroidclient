<p align="center">
  <img src= "https://github.com/Atiyyahm/Wordleandroidclient/blob/master/WordRush_Logo.png" width="220" alt="WordRush Logo"/>
</p>

#  WordRush

WordRush is a next-generation Android word challenge application that blends fun, learning, and competition.  
Built in **Kotlin**, powered by **Firebase**, and connected via a **Render-hosted REST API**, it brings daily puzzles, speed modes, and AI-driven multiplayer into one sleek experience.

---

## 📖 Table of Contents

1. [Introduction](#introduction)  
2. [Purpose](#purpose)  
3. [Objectives](#objectives)  
4. [Design Considerations](#design-considerations)  
5. [System Architecture](#system-architecture)  
6. [REST API Documentation](#rest-api-documentation)  
7. [Features](#features)  
8. [Screens](#screens)  
9. [Tech Stack](#tech-stack)  
10. [Functional Requirements](#functional-requirements)  
11. [Non-Functional Requirements](#non-functional-requirements)  
12. [GitHub and CI/CD](#github-and-cicd)
13. [Use of GitHub and GitHub actions](#use-of-github-and-github-actions)
14. [Release Notes](#release-notes)
15. [Demo Video](#demo-video)  
16. [Repository Links](#repository-links)
17. [Code Attribution](#code-attribution)
18. [References](#references)  
19. [AI Usage Disclosure](#ai-usage-disclosure)

---

## 🟢 Introduction

In an era where word games like *Wordle* captivate millions, **WordRush** redefines the genre with modern mechanics, cloud connectivity, and visual flair.  
Developed in **Kotlin (Android Studio)**, it integrates a **Node.js + Express REST API** hosted on **Render**, backed by **Firebase Firestore** and **RapidAPI’s WordsAPI**.

This ensures:
- A global daily puzzle  
- Reliable validation and scalable sessions  
- Cloud-backed progress tracking and leaderboards  

<p align="center">
  <img src="https://media1.giphy.com/media/v1.Y2lkPWFkZWE2ZTUyOHlhOWRwd204N2poZ2NjMjhnNGwwbTZqbnJ6Mmt3aWg2OGNoY21oaSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/e34lA8V77WQS0Ut1sG/giphy.gif" width="400"/>
</p>

---

## 🟠 Purpose

To create an interactive, educational, and competitive platform that strengthens vocabulary and mental agility through quick, rewarding gameplay.  
Unlike traditional word games, WordRush fuses **gamification**, **streak rewards**, and **timed challenges** to keep players engaged.

---

## 🎯 Objectives

- Develop a scalable, secure mobile application that enhances linguistic ability.  
- Integrate **Firebase Authentication** (Email + Google SSO).  
- Implement a **REST API layer** for core gameplay and word validation.  
- Introduce **multiple game modes** (Daily, Speedle, Multiplayer AI).  
- Automate builds and deployments using **GitHub Actions** and **Render**.  

---

## 💡 Design Considerations

### 🖋 User Experience (UX)
- **Dark/Light Themes:** Modern gradients reduce eye strain.  
- **Central Dashboard:** Card-based navigation for intuitive access.  
- **Instant Feedback:** Green = correct, Yellow = misplaced, Grey = incorrect.  
- **Progress Tracking:** Streaks, achievements, and badge animations.  
- **Accessibility:** Consistent layouts across all device sizes.

### 🎨 Visual Design
- **Color Palette:** Navy, Cyan, Neon Green, Red, and Gold.  
  - 🟦 Blue → Calm logic (Daily Mode)  
  - 🟩 Green → Action (Speedle Mode)  
  - 🟥 Red → Competition (Multiplayer)  
  - 🟨 Gold → Achievement (Streaks)  
- **Typography:** Rounded sans-serif for readability.  
- **Layout:** Rounded cards, shadows, and vibrant glow accents.

---

## ⚙️ System Architecture

WordRush follows a three-layer cloud architecture:

| Layer | Description |
|-------|--------------|
| **Frontend (Android)** | Kotlin + Material Components. Manages UI, logic, and Firebase login. |
| **Backend (Node.js + Express)** | Hosted on Render. Handles validation, word data, and gameplay APIs. |
| **Cloud (Firebase + WordsAPI)** | Firestore stores user data; WordsAPI provides definitions/synonyms. |

**Data Flow Overview**
1. User logs in via Firebase (Email or Google SSO).  
2. App requests a word or session from API.  
3. API validates via WordsAPI and Firestore.  
4. Data returned → stored and displayed dynamically.  

---

## 🔗 REST API Documentation

**Base URL:** `https://wordleappapi.onrender.com/api/v1`

| Endpoint | Method | Description |
|-----------|---------|-------------|
| `/word/today` | GET | Retrieves or seeds the daily 5-letter word |
| `/word/validate` | POST | Validates player’s guess |
| `/speedle/start` | POST | Starts timed Speedle mode |
| `/speedle/validate` | POST | Checks guesses & updates timer |
| `/speedle/finish` | POST | Ends session, saves score |
| `/speedle/leaderboard` | GET | Displays top players |

**Security:** Authenticated with Firebase ID Token.  

---

## 🧩 Features

### Core Gameplay
- **Daily Puzzle** shared globally  
- **Real-time feedback** with colored indicators  
- **Definition reveal** after each game
- **Leaderboards** global ranking for top players



### Highlighted Modes
- **Speedle:** Timed sprint (60/90/120 seconds)  
- **Multiplayer vs AI:** Compete against intelligent opponents (Easy–Hard)  
- **Hints & Power-ups:** Get clues at a cost of time  
- **Badges & Streaks:** Unlock glowing achievements  
- **Themes:** Switch between dark and light styles
- **Multiplayer with a Friend**: invite a friend and compete in real-time

<p align="center">
  <img src="https://media0.giphy.com/media/v1.Y2lkPWFkZWE2ZTUyOHlhOWRwd204N2poZ2NjMjhnNGwwbTZqbnJ6Mmt3aWg2OGNoY21oaSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/62HRHz7zZZYThhTwEI/giphy.gif" width="400"/>
</p>

---

## 📱 Screens

| Screen | Description |
|--------|--------------|
| **Welcome** | Eye-catching logo introduction |
| **Login/Register** | Email + Google SSO |
| **Dashboard** | Central navigation hub |
| **Speedle** | Timer-based challenge interface |
| **Multiplayer AI** | Play versus smart AI |
| **Badges/Profile** | View progress and stats |
| **Settings** | Toggle themes and haptics |

| Welcome | Launch | Register | Login | Dashboard |
|---------|--------|----------|-------|-----------|
| <img src="images/welcome%20screen.jpeg" width="250"/> | <img src="images/launch%20screen.jpeg" width="250"/> | <img src="images/Register.jpeg" width="250"/> | <img src="images/login.jpeg" width="250"/> |<img src="images/Home%20Screen.jpeg" width="250"/> |


---

| Tutorial Screen | Daily WordRush | Speedle Mode Time  | Speedle mode  |
|-----------------|----------------|------------------ |----------------|
| <img src="images/Tutorial.jpeg" width="250"/> | <img src="images/dailywordlescreen.png" width="250"/> | <img src="images/speedlescreen1.png" width="250"/> | <img src="images/speedlescreen2.png" width="250"/> |

---

| Multi-player mode Selection  | Muti-player with AI | End Game Screens   | Badges  |
|-----------------|----------------|------------------ |----------------|
| <img src="images/MutiplayerAi_S1.jpeg" width="250"/> | <img src="images/MutiplayerAi_S2.jpeg" width="250"/> | <img src="images/EndGameScreen.png" width="250"/> |<img src="https://github.com/Atiyyahm/Wordleandroidclient/blob/master/images/badges.png" width="250"/> |


---
| Hints  | Navbar | Profile   | Settings   |
|-----------------|----------------|------------------ |----------------|
| <img src="images/hints.png" width="250"/> | <img src="https://github.com/Atiyyahm/Wordleandroidclient/blob/master/images/navbar.png" width="250"/> | <img src="images/profile%20screen.png" width="250"/> |<img src="images/settings%20screen.png" width="250"/> |


---
---
| Light Mode & Dark Mode |
|----------------|
| <img src="images/dm_lm.png" width="250"/> 


---
## 🧰 Tech Stack

| Layer | Technology |
|--------|-------------|
| **Frontend** | Kotlin, Android Jetpack, Material Components |
| **Backend** | Node.js, Express, CORS, Axios |
| **Hosting** | Render |
| **Database** | Firebase Firestore |
| **Authentication** | Firebase Auth (Email & Google) |
| **External API** | RapidAPI WordsAPI |
| **CI/CD** | GitHub Actions |
| **Notifications** |Firebase Cloud Messaging|
| **Offline Support** |Room + Firestore Sync|
| **Localisation** |Multi-language Strings XML|


---

## ⚡ Functional Requirements

- Daily WordRush (shared puzzle)  
- Speedle Mode (timed)  
- Multiplayer vs AI (Easy–Hard)  
- Hints, Streaks & Badges  
- Profile & Settings  
- Firebase Authentication
- Biometric Login
- Offline Mode with Sync
- Real-time Push Notifications
- Multi-Language Support
- Leaderboard

---

## 🛡 Non-Functional Requirements

- **Security:** Firebase ID tokens, Firestore rules, secrets via Render, Biometrics
- **Performance:** Cached daily puzzles, timeouts, and error handling  
- **Accessibility:** Screen reader labels, high contrast themes, language switching 
- **Maintainability:** Modular helpers, semantic commits, CI checks, logging + comments
- **Reliability:** Offline-first design with automatic sync

---

## 🔁 GitHub and CI/CD

- **Frontend Repo:** [WordRush Android (Kotlin)](https://github.com/Atiyyahm/Wordleandroidclient.git)  
- **Backend Repo:** [WordRush API (Node.js)](https://github.com/ST10268917/WordleApp.git)

Both use **GitHub Actions** for build automation.  
Render auto-deploys backend changes on merge to `main`.

---
## 💻 Use of GitHub and GitHub actions: 

- Used GitHub as the main version-control platform for all source code.
- Maintained separate repositories for Android (Kotlin) and the Node.js API.
- Used branches and commits to manage development in a structured way.
- Implemented GitHub Actions for automated CI checks (builds, linting, workflow validation).
- Enabled automatic backend deployment to Render on every merge to main.
- Improved reliability and reduced manual errors through continuous integration and delivery.
- Ensured a professional, consistent, and streamlined development process.

---
## 📝 Release Notes 

### Part 1: Research & Design 
This phase focused on defining the vision, design, user experience, and architectural structure of WordRush.

**Research & Design Outcomes:**
- UX planning: Themes, navigation flow and feedback colours.
- Visual design system: Colour palette, typography and card layouts.
- Mode planning: Daily, Speedle and Multiplayer.
- Feature mapping: Hints, badges, streaks and settings.
- Wireframes and conceptual UI design.
  
This stage ensured a solid blueprint before development.

### Part 2: Prototype Development 
This stage focused on building the first functional version of WordRush to demonstrate the core gameplay and validate the overall concept.

**Prototype Features:**
- Fully working API integration.
- Google SSO login implemented.
- Definition revealed after each completed game.
- Speedle mode with timed rounds.
- Multiplayer vs AI ( Easy - Hard).
- Hint System ( definition or synonym).
- Badges and daily streak tracking.
- Basic settings screen ( theme, sound, account).
- Tutorial onboarding screen.
  
This prototype enabled early testing, refinement and validation of the main gameplay systems.

### Part 3: Final Development Features
This phase delivered the completed version of WordRush with all advanced, innovative features required for release.

**Final Features Added:**
- Biometric Login
- Leaderboard for daily results.
- Push Notifications for streaks, reminders and daily puzzles.
- Multi-language support, including full Afrikaans translations.
- Offline Mode with Sync, allowing gameplay without an internet connection.
- Final UI improvements, animations and stability fixes.
  
These completed features prepared WordRush for deployment to the Google Play Store.

---
## 🎥 Demo Video

[![Watch the Demo](https://youtu.be/HSwqAYXv03g)

---

## 🧭 Repository Links

- Frontend → [ST10258766/Wordleandroidclient](https://github.com/ST10258766/Wordleandroidclient)  
- Backend → [ST10268917/WordleApp](https://github.com/ST10268917/WordleApp.git)

---

<p align="center">
  <img src="https://media3.giphy.com/media/v1.Y2lkPWFkZWE2ZTUyOHlhOWRwd204N2poZ2NjMjhnNGwwbTZqbnJ6Mmt3aWg2OGNoY21oaSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/NdieEAYwEZJot8ZA92/giphy.gif" width="380"/>
</p>

---
## 🧾Code Attribution

This project utilises code from the following sources:

- *Smith, J.* (2024). Dynamic Language Switching in Android. Available at: https://github.com/jsmith/android-language-switcher [Accessed 18 Nov. 2025].

- *Brown, L.* (2023). Firebase Push Notifications in Android. Available at: https://github.com/lbrown/firebase-push-notifications [Accessed 18 Nov. 2025].

- *Taylor, M.* (2022). Implementing Multiplayer with Firebase in Android. Available at: https://github.com/mtaylor/firebase-multiplayer [Accessed 18 Nov. 2025].

- *Jackson, R.* (2021). AI Multiplayer Opponent Logic in Android Games. Available at: https://github.com/rjackson/ai-multiplayer [Accessed 18 Nov. 2025].

  ---
## 🧠 References

- Fowler, M. (2022). *Continuous Integration and Delivery: Modern DevOps Practices.* ThoughtWorks.  
- GitHub Docs. (2024). *Understanding GitHub Actions Workflows.*  
- GitHub Marketplace. (2024). *Automated Build Android App Workflow.*  
- Martin, R.C. (2023). *Clean DevOps: Building and Testing Automation in Practice.*  
- OWASP. (2023). *Secrets Management Cheat Sheet.*  
- Render. (2024). *Continuous Deployment and Environment Variables.*

---

## 🤖 AI Usage Disclosure

During development, **AI** was used responsibly to:
- **Logo Concept Assistance**: Helped to generate the visual idea for the WordRush logo.
- **Debugging support**: Assisted in identifying issues with RetrofitClient, Firebase Google SSO setup and general API request handling.
- **Coding Assistance**: Guided fixing syntax errors, optimising Kotlin functions, structuring ViewModels, and improving modularity in both frontend and backend code.
- **Troubleshooting**: Helped resolve API integration problems and JSON parsing issues.
- **Learning Aid**: Served as a tool to understand best practices, design patterns, clean code principles and Android UI/UX standards. 

All implementation and creative decisions were completed manually.  
AI served only as a supportive tool for learning and troubleshooting.

<p align="center">
  <img src="https://media4.giphy.com/media/v1.Y2lkPWFkZWE2ZTUyOHlhOWRwd204N2poZ2NjMjhnNGwwbTZqbnJ6Mmt3aWg2OGNoY21oaSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/NFgM484nJxhYpvDz9C/giphy.gif" width="420"/>
</p>

---
