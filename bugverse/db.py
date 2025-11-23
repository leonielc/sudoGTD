import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Iterable, List, Optional

BASE_DIR = Path(__file__).resolve().parent.parent
DB_PATH = BASE_DIR / "data" / "bugbank.db"
DB_PATH.parent.mkdir(parents=True, exist_ok=True)


def get_connection() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db(seed: bool = True) -> None:
    conn = get_connection()
    cur = conn.cursor()

    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS bug_reports (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alias TEXT NOT NULL,
            bug_name TEXT NOT NULL,
            buggy_code TEXT NOT NULL,
            fix_description TEXT NOT NULL,
            severity TEXT,
            tags TEXT,
            xp_awarded INTEGER DEFAULT 0,
            created_at TEXT NOT NULL
        )
        """
    )

    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS hunters (
            alias TEXT PRIMARY KEY,
            xp INTEGER DEFAULT 0,
            submissions_count INTEGER DEFAULT 0,
            scans_count INTEGER DEFAULT 0,
            badges TEXT DEFAULT '',
            last_active TEXT
        )
        """
    )

    conn.commit()

    if seed:
        seed_demo_data(conn)

    conn.close()


def seed_demo_data(conn: sqlite3.Connection) -> None:
    cur = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM bug_reports")
    count = cur.fetchone()[0]
    if count:
        return

    demo_rows = [
        {
            "alias": "retro_wizard",
            "bug_name": "Off-by-one portal",
            "buggy_code": "for (int i = 0; i <= items.length; i++) { summon(items[i]); }",
            "fix_description": "Loop until i < items.length to avoid summoning the void.",
            "severity": "high",
            "tags": "loop,array",
        },
        {
            "alias": "stackmage",
            "bug_name": "Recursive hydra",
            "buggy_code": "def hydra(n): return 1 if n==0 else hydra(n-1)+hydra(n-2)",
            "fix_description": "Cache heads with memoization or risk exponential chaos.",
            "severity": "medium",
            "tags": "recursion,performance",
        },
        {
            "alias": "nullbard",
            "bug_name": "Null siren song",
            "buggy_code": "song = request.json['anthem']\nreturn song.upper()",
            "fix_description": "Guard the request payload before singing in uppercase.",
            "severity": "low",
            "tags": "api,null",
        },
    ]

    now = datetime.utcnow().isoformat()
    for row in demo_rows:
        cur.execute(
            """
            INSERT INTO bug_reports (
                alias, bug_name, buggy_code, fix_description, severity, tags, xp_awarded, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                row["alias"],
                row["bug_name"],
                row["buggy_code"],
                row["fix_description"],
                row["severity"],
                row["tags"],
                50,
                now,
            ),
        )

        upsert_hunter(
            alias=row["alias"],
            delta_xp=50,
            submission_delta=1,
            scan_delta=0,
            badges_to_add=["Founding Archivist"],
            connection=conn,
        )

    conn.commit()


def save_bug_report(
    *,
    alias: str,
    bug_name: str,
    buggy_code: str,
    fix_description: str,
    severity: str,
    tags: str,
    xp_awarded: int,
) -> None:
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(
        """
        INSERT INTO bug_reports (
            alias, bug_name, buggy_code, fix_description, severity, tags, xp_awarded, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            alias,
            bug_name,
            buggy_code,
            fix_description,
            severity,
            tags,
            xp_awarded,
            datetime.utcnow().isoformat(),
        ),
    )
    conn.commit()
    conn.close()


def fetch_bug_reports() -> List[sqlite3.Row]:
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(
        "SELECT id, alias, bug_name, buggy_code, fix_description, severity, tags, xp_awarded, created_at FROM bug_reports ORDER BY created_at DESC"
    )
    rows = cur.fetchall()
    conn.close()
    return rows


def upsert_hunter(
    *,
    alias: str,
    delta_xp: int,
    submission_delta: int = 0,
    scan_delta: int = 0,
    badges_to_add: Optional[Iterable[str]] = None,
    connection: Optional[sqlite3.Connection] = None,
) -> None:
    badges_to_add = badges_to_add or []
    own_conn = connection is None
    conn = connection or get_connection()
    cur = conn.cursor()

    cur.execute("SELECT alias, xp, submissions_count, scans_count, badges FROM hunters WHERE alias = ?", (alias,))
    row = cur.fetchone()

    if row is None:
        badge_str = ",".join(badges_to_add)
        cur.execute(
            """
            INSERT INTO hunters (alias, xp, submissions_count, scans_count, badges, last_active)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (
                alias,
                max(delta_xp, 0),
                max(submission_delta, 0),
                max(scan_delta, 0),
                badge_str,
                datetime.utcnow().isoformat(),
            ),
        )
    else:
        badges = set(filter(None, row["badges"].split(",")))
        badges.update(badges_to_add)
        cur.execute(
            """
            UPDATE hunters
            SET xp = xp + ?,
                submissions_count = submissions_count + ?,
                scans_count = scans_count + ?,
                badges = ?,
                last_active = ?
            WHERE alias = ?
            """,
            (
                delta_xp,
                submission_delta,
                scan_delta,
                ",".join(sorted(badges)),
                datetime.utcnow().isoformat(),
                alias,
            ),
        )

    conn.commit()
    if own_conn:
        conn.close()


def fetch_leaderboard(limit: int = 8) -> List[sqlite3.Row]:
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(
        """
        SELECT alias, xp, submissions_count, scans_count, badges
        FROM hunters
        ORDER BY xp DESC, submissions_count DESC
        LIMIT ?
        """,
        (limit,),
    )
    rows = cur.fetchall()
    conn.close()
    return rows


def fetch_hunter(alias: str) -> Optional[sqlite3.Row]:
    conn = get_connection()
    cur = conn.cursor()
    cur.execute(
        "SELECT alias, xp, submissions_count, scans_count, badges FROM hunters WHERE alias = ?",
        (alias,),
    )
    row = cur.fetchone()
    conn.close()
    return row
