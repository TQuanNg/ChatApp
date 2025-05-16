# Java Console Chat App
An upgrade from the Simple Chat App that I made 2 years ago.

## 🧰 Prerequisites

- Java 11 or higher
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/download.html) (`postgresql-42.6.0.jar`)
- A running PostgreSQL database (if your app uses it)

## ▶️ How to Run

### ✅ Step 1: Edit `run.bat`

Make sure the path to your PostgreSQL driver is correct. For example:
C:\Program Files\Java\postgresql-42.6.0.jar

### ✅ Step 2:
Run the application using:

```bash
.\run.bat


This script will:
Compile all .java files.

Launch the server in a new terminal window.

Launch the client interface in the current terminal.

❌ Exiting the App
Type q or use the quit option from the menu to exit the chat.

Press Ctrl + C in the client/server console to terminate manually.
