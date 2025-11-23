# Bug Tracker Web Application

A simple Flask web application to register buggy code, document fixes, and analyze new code to find similar issues.

## Features

- **Register Bugs**: Save buggy code snippets with descriptions and fix instructions
- **Analyze Code**: Submit code to check against the database for similar bugs
- **Similarity Matching**: Uses text similarity algorithms to find matching buggy code patterns
- **SQLite Database**: Lightweight database to store all bug records

## Installation

1. Install Python 3.7 or higher

2. Install dependencies:
```bash
pip install -r requirements.txt
```

## Running the Application

1. Start the Flask server:
```bash
python app.py
```

2. Open your browser and go to:
```
http://127.0.0.1:5000
```

## Usage

### Register a Bug
1. Click "Register a Bug" on the home page
2. Fill in:
   - **Bug Name**: A descriptive name for the bug
   - **Buggy Code**: The problematic code snippet
   - **How to Fix**: Description of the fix or corrected code
3. Click "Register Bug"

### Analyze Code
1. Click "Analyze Code" on the home page
2. Paste the code you want to check
3. Click "Analyze Code"
4. View results showing similar bugs and their fixes (60%+ similarity threshold)

## Project Structure

```
sudoGTD/
├── app.py                 # Main Flask application
├── templates/
│   ├── index.html        # Home page
│   ├── register.html     # Bug registration page
│   └── query.html        # Code analysis page
├── requirements.txt       # Python dependencies
└── bugs.db               # SQLite database (created automatically)
```

## API Endpoints

- `GET /` - Home page
- `GET /register` - Bug registration form
- `POST /api/register` - Register a new bug (JSON)
- `GET /query` - Code analysis form
- `POST /api/query` - Analyze code (JSON)
- `GET /api/bugs` - Get all bugs (JSON)

## Technologies

- **Backend**: Flask (Python)
- **Database**: SQLite
- **Frontend**: HTML, CSS, JavaScript
- **Similarity Algorithm**: SequenceMatcher from Python's difflib