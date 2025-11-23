from flask import Flask, render_template, request, jsonify
import sqlite3
import os
from difflib import SequenceMatcher

app = Flask(__name__)
DATABASE = 'bugs.db'

def init_db():
    """Initialize the database with bugs table."""
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS bugs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            bug_name TEXT NOT NULL,
            buggy_code TEXT NOT NULL,
            fix_description TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

def similarity_score(code1, code2):
    """Calculate similarity between two code snippets."""
    # Normalize whitespace for better comparison
    code1 = ' '.join(code1.split())
    code2 = ' '.join(code2.split())
    return SequenceMatcher(None, code1, code2).ratio()

@app.route('/')
def index():
    """Home page."""
    return render_template('index.html')

@app.route('/register')
def register():
    """Register a new bug."""
    return render_template('register.html')

@app.route('/api/register', methods=['POST'])
def api_register():
    """API endpoint to register a bug."""
    data = request.json
    bug_name = data.get('bug_name', '').strip()
    buggy_code = data.get('buggy_code', '').strip()
    fix_description = data.get('fix_description', '').strip()
    
    if not bug_name or not buggy_code or not fix_description:
        return jsonify({'error': 'All fields are required'}), 400
    
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute(
        'INSERT INTO bugs (bug_name, buggy_code, fix_description) VALUES (?, ?, ?)',
        (bug_name, buggy_code, fix_description)
    )
    conn.commit()
    conn.close()
    
    return jsonify({'message': 'Bug registered successfully'}), 201

@app.route('/query')
def query():
    """Query page to check code against database."""
    return render_template('query.html')

@app.route('/api/query', methods=['POST'])
def api_query():
    """API endpoint to analyze code and find similar bugs."""
    data = request.json
    code_to_check = data.get('code', '').strip()
    
    if not code_to_check:
        return jsonify({'error': 'Code is required'}), 400
    
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute('SELECT id, bug_name, buggy_code, fix_description FROM bugs')
    bugs = cursor.fetchall()
    conn.close()
    
    # Find similar bugs
    threshold = 0.6  # 60% similarity threshold
    matches = []
    
    for bug_id, bug_name, buggy_code, fix_description in bugs:
        score = similarity_score(code_to_check, buggy_code)
        if score >= threshold:
            matches.append({
                'id': bug_id,
                'bug_name': bug_name,
                'buggy_code': buggy_code,
                'fix_description': fix_description,
                'similarity': round(score * 100, 2)
            })
    
    # Sort by similarity (highest first)
    matches.sort(key=lambda x: x['similarity'], reverse=True)
    
    if matches:
        return jsonify({
            'found': True,
            'matches': matches
        })
    else:
        return jsonify({
            'found': False,
            'message': 'No buggy code lookalike found'
        })

@app.route('/api/bugs')
def api_bugs():
    """Get all registered bugs."""
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute('SELECT id, bug_name, buggy_code, fix_description, created_at FROM bugs ORDER BY created_at DESC')
    bugs = cursor.fetchall()
    conn.close()
    
    bug_list = []
    for bug_id, bug_name, buggy_code, fix_description, created_at in bugs:
        bug_list.append({
            'id': bug_id,
            'bug_name': bug_name,
            'buggy_code': buggy_code,
            'fix_description': fix_description,
            'created_at': created_at
        })
    
    return jsonify({'bugs': bug_list})

if __name__ == '__main__':
    init_db()
    app.run(debug=True)
