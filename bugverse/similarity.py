from __future__ import annotations

import difflib
from typing import Iterable, List, Mapping


def _normalize(code: str) -> str:
    return "\n".join(line.strip().lower() for line in code.strip().splitlines() if line.strip())


def similarity_score(sample: str, reference: str) -> float:
    if not sample.strip() or not reference.strip():
        return 0.0
    sample_norm = _normalize(sample)
    ref_norm = _normalize(reference)
    matcher = difflib.SequenceMatcher(None, sample_norm, ref_norm)
    return round(matcher.ratio(), 4)


def rank_similar_bugs(
    *,
    query_snippet: str,
    bug_rows: Iterable[Mapping],
    limit: int = 5,
    threshold: float = 0.25,
) -> List[Mapping]:
    ranked = []
    for row in bug_rows:
        score = similarity_score(query_snippet, row["buggy_code"])
        if score >= threshold:
            insight = build_insight(score, row["severity"], row["fix_description"])
            ranked.append({"row": row, "score": score, "insight": insight})
    ranked.sort(key=lambda item: item["score"], reverse=True)
    return ranked[:limit]


def build_insight(score: float, severity: str, remedy: str) -> str:
    tiers = [
        (0.8, "Twin anomaly detected"),
        (0.6, "Strong family resemblance"),
        (0.45, "Similar vibes"),
        (0.3, "Possible cousin"),
        (0.25, "Faint echo"),
    ]
    headline = "Code glitch resonance low"
    for threshold, label in tiers:
        if score >= threshold:
            headline = label
            break
    return f"{headline}: severity {severity or 'unknown'}. Remedy hint: {remedy[:140]}..."
