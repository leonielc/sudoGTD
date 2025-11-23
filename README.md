# Sudo GTD — Bug Guild Vault

A playful Flask web app where teammates archive tricky bugs, share how they fixed them, and scan new snippets to see if the vault already holds a similar anomaly. Every interaction feeds a gamified leaderboard so the most prolific bug slayers rise to the top.

## Features

- **Bug Archive** – Submit code, name the bug, add tags, and describe the remedy.
- **Similarity Scanner** – Paste new snippets to surface previously catalogued bugs using fuzzy matching.
- **Gamification Layer** – Earn XP, badges, and artifacts for both submissions and scans.
- **Live Leaderboard** – Watch contributions in real time with a single click.
- **Daily Quests & Vibe Cards** – Rotating prompts and fortune-style flavor keep the experience lively.

## Quickstart

```fish
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export FLASK_APP=app.py
flask run --debug
```

Visit <http://127.0.0.1:5000> to register bugs, then hop to `/scan` to query the vault.

## Tests

```fish
source .venv/bin/activate
pytest
```

## Project layout

```
app.py                # Flask entry point
bugverse/             # Storage, similarity, and gamification modules
static/               # CSS + JS
templates/            # Jinja templates
tests/                # Pytest suite
```

## Notes

- The app seeds the vault with three playful demo bugs on first launch.
- XP and badges are lightweight and stored alongside user aliases in SQLite.
- Similarity scoring relies on a normalized difflib ratio—easy to run anywhere.
