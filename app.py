from __future__ import annotations

import os
import random
from dataclasses import asdict
from typing import Dict, List, Optional

from flask import Flask, jsonify, redirect, render_template, request, url_for

from bugverse import (
    GamificationEngine,
    fetch_bug_reports,
    fetch_hunter,
    fetch_leaderboard,
    init_db,
    rank_similar_bugs,
    save_bug_report,
    upsert_hunter,
)

app = Flask(__name__)
app.config["SECRET_KEY"] = os.environ.get("BUGVAULT_SECRET", "bugverse-dev-key")

init_db()
engine = GamificationEngine()


@app.context_processor
def inject_shared_context():
    leaderboard = fetch_leaderboard()
    guild_mood = random.choice(
        [
            "🔥 Hype levels critical",
            "🧠 Brainstorm drizzle",
            "⚡ Bug storm incoming",
            "🌈 Refactor rainbow",
        ]
    )
    return dict(leaderboard=leaderboard, guild_mood=guild_mood)


@app.route("/")
def index():
    reports = fetch_bug_reports()
    quests = build_daily_quests()
    return render_template("index.html", reports=reports[:6], quests=quests)


@app.route("/submit", methods=["POST"])
def submit_bug():
    payload = sanitize_submission(request.form)
    existing = fetch_hunter(payload["alias"])
    reward = engine.score_submission(
        severity=payload["severity"],
        tag_count=len(payload["tags"].split(",")) if payload["tags"] else 0,
        code_length=len(payload["buggy_code"]),
        existing_stats=_row_to_dict(existing),
    )

    save_bug_report(**payload, xp_awarded=reward.xp)
    upsert_hunter(
        alias=payload["alias"],
        delta_xp=reward.xp,
        submission_delta=1,
        badges_to_add=reward.badges,
    )

    return redirect(
        url_for(
            "index",
            highlight=f"{payload['bug_name']} (+{reward.xp}xp) — {reward.artifact}! {reward.flavor}",
        )
    )


@app.route("/scan", methods=["GET", "POST"])
def scan():
    matches: List[Dict] = []
    reward: Optional[Dict] = None
    query_snippet = ""
    alias = ""

    if request.method == "POST":
        alias = request.form.get("alias", "bugless_hero").strip() or "bugless_hero"
        query_snippet = request.form.get("code", "")
        reports = fetch_bug_reports()
        matches = rank_similar_bugs(query_snippet=query_snippet, bug_rows=reports, limit=5)
        best_score = matches[0]["score"] if matches else 0
        existing = fetch_hunter(alias)
        reward_bundle = engine.score_scan(
            similarity=best_score,
            snippets_checked=len(reports),
            existing_stats=_row_to_dict(existing),
        )
        upsert_hunter(
            alias=alias,
            delta_xp=reward_bundle.xp,
            scan_delta=1,
            badges_to_add=reward_bundle.badges,
        )
        reward = asdict(reward_bundle)

    vibe_card = build_vibe_card()
    return render_template(
        "scan.html",
        matches=matches,
        query_snippet=query_snippet,
        alias=alias,
        reward=reward,
        vibe_card=vibe_card,
    )


@app.route("/api/leaderboard")
def leaderboard_api():
    rows = fetch_leaderboard(limit=15)
    payload = [
        {
            "alias": row["alias"],
            "xp": row["xp"],
            "submissions": row["submissions_count"],
            "scans": row["scans_count"],
            "badges": list(filter(None, row["badges"].split(","))) if row["badges"] else [],
        }
        for row in rows
    ]
    return jsonify(payload)


@app.route("/health")
def health():
    return {"status": "ok", "reports": len(fetch_bug_reports())}


def sanitize_submission(form) -> Dict[str, str]:
    return {
        "alias": (form.get("alias") or "anonymous_hacker").strip()[:40],
        "bug_name": (form.get("bug_name") or "Unnamed glitch").strip()[:80],
        "buggy_code": form.get("buggy_code", "")[:4000],
        "fix_description": form.get("fix_description", ""),
        "severity": (form.get("severity") or "medium").lower(),
        "tags": ",".join(filter(None, [tag.strip() for tag in (form.get("tags") or "").split(",")]))[:120],
    }


def _row_to_dict(row):
    if row is None:
        return None
    return {key: row[key] for key in row.keys()}


def build_daily_quests():
    quests = [
        {
            "title": "Patch three array goblins",
            "reward": "+45xp",
            "tip": "Share a tricky boundary bug to rally the guild.",
        },
        {
            "title": "Resonate with a legacy incantation",
            "reward": "+20xp",
            "tip": "Scan a dusty snippet to see if the archives respond.",
        },
        {
            "title": "Gift a teammate a fix story",
            "reward": "Mystery artifact",
            "tip": "Tell us how you tamed a wild stacktrace.",
        },
    ]
    random.shuffle(quests)
    return quests


def build_vibe_card():
    sparks = [
        "Chaotic good energy detected.",
        "Your code hums with cosmic recursion.",
        "Compiler spirits applaud your bravery.",
        "You unlocked a hidden breakpoint of destiny.",
    ]
    return {
        "title": random.choice(["Signal Echo", "Glitch Fortune", "Patch Prophecy"]),
        "body": random.choice(sparks),
        "aura": random.choice(["plasma", "nebula", "aurora", "metro"]),
    }


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=True)
