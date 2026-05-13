# Namma Pustaka (ನಮ್ಮ ಪುಸ್ತಕ)
### Smart Library Assistant for Rural Schools
**Project Title: 97 — Android App Development using GenAI (Education)**

---

## The Problem Statement

In many village schools, the library is just a cupboard of books with no tracking. Students often don't know what books are available, and teachers struggle to keep track of who has borrowed what. This leads to lost books and a lack of reading culture.

---

## The Vision

Namma-Pustaka is a **"Smart Library Assistant"** for rural schools. It turns a simple shelf into a digital catalog. Students can browse book covers on the app, see summaries in Kannada, and reserve a book. For the teacher, it acts as a digital register — automatically flagging when a book is overdue.

---

## App Usage & User Flow

- **Book Catalog** — Browse books by category (Story, Science, History, Biography)
- **QR Borrow** — Scan a QR code on the book to "Issue" it to a student profile
- **Review Corner** — Students can leave a Star Rating and a one-sentence review
- **Reading Leaderboard** — Tracks which student has read the most pages this month

---

## Screenshots

### Book Catalog
The home screen shows all books in a 2-column grid with cover images, availability badges, category chips for filtering, and a search bar.

<img src="screenshots/screenshots/01_book_catalog.png" width="270" alt="Book Catalog"/>

---

### Search
Real-time search by title or author. Selecting a category chip filters the grid instantly.

<img src="screenshots/09_search.png" width="270" alt="Search and Filter"/>

---

### Book Detail
Shows cover, author, category, page count, QR code, star rating, and a Gemini-generated Kannada summary. Buttons to issue, scan QR, or write a review.

<img src="screenshots/02_book_detail.png" width="270" alt="Book Detail"/>

---

### Write a Review
Students can select their name from a dropdown, give a star rating, and write a one-line review.

<img src="screenshots/screenshots/03_write_review.png" width="270" alt="Write Review"/>

---

### QR Code Scanner
CameraX-powered scanner with a white guide frame. Point the camera at any book spine label to look up the book instantly.

<img src="screenshots/04_qr_scan.png" width="270" alt="QR Scanner"/>

---

### Transactions
Select a student to view or create borrow entries. Chips to filter by All / Active / Overdue.

<img src="screenshots/05_transactions.png" width="270" alt="Transactions"/>

---

### Students List
All registered students with their roll number, class, and reading stats. Tap + to add a new student.

<p>
  <img src="screenshots/06_students_list.png" width="270" alt="Students List"/>
  &nbsp;&nbsp;
  <img src="screenshots/08_students_updated.png" width="270" alt="Students Updated"/>
</p>

---

### Add New Student
Dialog to register a student with name, roll number, class, and section.

<img src="screenshots/07_add_student.png" width="270" alt="Add Student"/>

---

## Features

### Book Management
- Grid catalog of all books with cover images (fetched from Open Library API)
- Add books manually or by photographing the book cover using the camera
- ML Kit text recognition reads the title/author from the camera feed
- Gemini AI generates a book summary in Kannada automatically
- Search by title or author (searchable by Book Name or Author)
- Filter by category: Story, Biography, History, Science, and more
- Star ratings and written reviews per book

### QR Code System
- Every book gets a unique QR code (e.g. `BOOK_001`)
- Scan QR codes to instantly look up and issue a book using the camera
- Generate a printable QR label sheet — print or share as an image
- Labels show: QR code, book title, author, and code text

### Borrowing & Returns
- Issue books to students with one tap (Check-in / Check-out workflow)
- Mark books as returned; log pages read by the student
- **Overdue detection — overdue entries automatically turn text Red**
- Full transaction history per student and per book

### Reading Leaderboard
- Top readers ranked by total pages read this month
- Alternate view: top readers by number of books completed
- Motivates students to read more and build a reading culture

### Student Management
- Register students with name, roll number, class, and section
- View each student's active borrows and full reading history

### Gemini AI (Kannada)
- Summarises any book in simple Kannada for young readers
- Displayed on the book detail screen
- Powered by Google Gemini API

---

## Technical Implementation

| Component | Technology Used |
|---|---|
| **Scanning** | Google ML Kit — QR code and barcode tracking |
| **Database** | Room DB — manages Book list and Transaction history |
| **UI Layout** | RecyclerView with Grid layout — shows book covers like a digital shelf |
| **AI Summaries** | Google Gemini API — Kannada summaries via `generativeai` SDK |
| **Camera** | CameraX 1.4.1 — camera preview, capture, and QR scanning |
| **Text Recognition** | ML Kit Text Recognition — reads title/author from book cover photo |
| **QR Generation** | ZXing Core (pure Java) — generates printable QR label images |
| **Book Covers** | Open Library Covers API — fetches cover art by ISBN |
| **Image Loading** | Glide |
| **Networking** | Retrofit + OkHttp |
| **Architecture** | MVVM — ViewModel + LiveData + Repository |
| **Navigation** | Jetpack Navigation Component + Bottom Navigation |
| **Language** | Kotlin |
| **Build** | Gradle (Kotlin DSL) |
| **Min SDK** | API 26 (Android 8.0) |
| **Target SDK** | API 35 (Android 15) |

---

## Impact Goals

- **Literacy Promotion** — Encouraging a modern reading habit in the digital age
- **Resource Management** — Protecting and organising public school assets; no more lost books
- **Digital Habits** — Teaching children basic Check-in / Check-out digital workflows

---

## Success Criteria

| Criterion | Status |
|---|---|
| Add a new book via a simple camera-based entry | Implemented — AddBookActivity uses CameraX + ML Kit |
| Overdue status turns text colour to Red automatically | Implemented — `markOverdue()` runs on startup |
| Library catalog is searchable by Book Name or Author | Implemented — real-time search in CatalogFragment |
| Browse books by category | Implemented — chip filter bar (Story, Science, History…) |
| QR code scan to issue a book to a student | Implemented — QRScanActivity + ML Kit Barcode |
| Students can leave a Star Rating and one-sentence review | Implemented — ReviewDialog on BookDetailActivity |
| Reading Leaderboard by pages read this month | Implemented — LeaderboardFragment |
| Kannada summaries for books | Implemented — Gemini AI via GeminiHelper |

---

## Tech Stack Summary

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts, ViewBinding, RecyclerView, GridLayoutManager |
| Navigation | Jetpack Navigation Component + Bottom Navigation |
| Database | Room (SQLite) with Kotlin Coroutines |
| Architecture | MVVM — ViewModel + LiveData + Repository |
| Camera | CameraX 1.4.1 |
| QR Scanning | ML Kit Barcode Scanning |
| Text Recognition | ML Kit Text Recognition |
| QR Generation | ZXing Core (pure Java) |
| AI Summary | Google Gemini API (`generativeai` SDK) |
| Book Covers | Open Library Covers API |
| Image Loading | Glide |
| Networking | Retrofit + OkHttp |
| Build | Gradle (Kotlin DSL) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |

---

## Project Structure

```
app/src/main/java/com/example/nammapustaka1/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt          # Room database + seed data
│   │   ├── BookDao.kt
│   │   ├── StudentDao.kt
│   │   ├── TransactionDao.kt
│   │   └── ReviewDao.kt
│   ├── model/
│   │   ├── Book.kt
│   │   ├── Student.kt
│   │   ├── Transaction.kt
│   │   └── Review.kt
│   └── repository/
│       └── LibraryRepository.kt
├── viewmodel/
│   └── LibraryViewModel.kt         # Central ViewModel + Factory
├── adapter/
│   ├── BookGridAdapter.kt
│   ├── LeaderboardAdapter.kt
│   ├── TransactionAdapter.kt
│   └── QRLabelAdapter.kt           # QR label grid with ZXing
├── ui/
│   ├── MainActivity.kt
│   ├── catalog/
│   │   └── CatalogFragment.kt      # Home screen: book grid + filters
│   ├── detail/
│   │   └── BookDetailActivity.kt   # Book info, AI summary, issue/return
│   ├── addbbook/
│   │   └── AddBookActivity.kt      # Camera + ML Kit + Gemini entry
│   ├── qrscan/
│   │   └── QRScanActivity.kt       # CameraX QR scanner
│   ├── qrlabel/
│   │   ├── QRLabelActivity.kt      # Print / share QR label sheet
│   │   └── QRLabelPrintAdapter.kt
│   ├── leaderboard/
│   │   └── LeaderboardFragment.kt
│   ├── mybooks/
│   │   └── MyBooksFragment.kt      # Active borrows + overdue list
│   └── students/
│       └── StudentsFragment.kt
└── utils/
    ├── GeminiHelper.kt             # Gemini AI Kannada summaries
    └── OpenLibraryHelper.kt        # Cover URL + QR code helper
```

---

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK API 26–35
- Internet connection (for Open Library covers and Gemini AI)

### Steps

1. **Clone / copy** the project into Android Studio.

2. **Fix the package name** — all source files use `com.example.nammapustaka1`.
   If your project was created with a different package, press `Ctrl+Shift+R`
   and replace `com.example.nammapustaka1` with your actual package name everywhere.

3. **Sync Gradle**
   Click *File → Sync Project with Gradle Files*.

4. **Run** on an emulator (API 34 recommended) or a physical device.

> The app seeds 5 sample books and 5 students automatically on the first launch.

### Gemini API Key
The API key is set in `utils/GeminiHelper.kt`:
```kotlin
private const val API_KEY = "AIzaSyC-PNup-YdMf2rvmjhjXlIFhLC5zWgx0rk"
```
Replace this with your own key from [Google AI Studio](https://aistudio.google.com/) for production use.

---

## Known Notes

- **16 KB page alignment dialog** — appears on API 35 emulators only. The app runs
  perfectly in compatible mode. Tap *"Don't Show Again"* or use an API 34 emulator.
  `useLegacyPackaging = true` in `build.gradle.kts` minimises the warning.
- **Book covers** require internet. Offline, the placeholder icon is shown instead.
- **Gemini summaries** require internet. The summary section stays empty offline.

---

## Navigation Guide

| Screen | How to Access |
|---|---|
| Book Catalog | Bottom nav — ಪುಸ್ತಕಗಳು (first tab) |
| Book Detail + AI Summary | Tap any book card |
| Add Book (Camera) | FAB + button on catalog screen |
| QR Scan | Toolbar QR icon |
| QR Label Print | Toolbar ⋮ menu → "QR ಲೇಬಲ್ ಮುದ್ರಿಸಿ" |
| Transactions | Bottom nav — ವ್ಯವಹಾರ (second tab) |
| Leaderboard | Bottom nav — ಶ್ರೇಣಿ (third tab) |
| Students | Bottom nav — ವಿದ್ಯಾರ್ಥಿ (fourth tab) |

---

## License

Built for educational use in rural Karnataka schools.
Free to use, modify, and distribute for non-commercial purposes.
