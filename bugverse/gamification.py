from __future__ import annotations

import random
from dataclasses import dataclass
from typing import Dict, Iterable, List, Optional


@dataclass
class RewardBundle:
    xp: int
    badges: List[str]
    artifact: str
    flavor: str


class GamificationEngine:
    severity_weights = {"low": 15, "medium": 35, "high": 60, "critical": 90}
    badge_catalog = {
        "First Catch": lambda stats: stats.get("submissions", 0) == 0,
        "Combo Crafter": lambda stats: stats.get("submissions", 0) >= 5,
        "Scanner": lambda stats: stats.get("scans", 0) >= 3,
        "Bug Whisperer": lambda stats: stats.get("xp", 0) >= 250,
        "Mythic Analyst": lambda stats: stats.get("xp", 0) >= 600,
    }

    artifacts = [
        "Chrono Debug Lens",
        "Stacktrace Tarot",
        "Quantum Rubber Duck",
        "Regex Rune Stone",
        "Segfault Shield",
        "Latency Lute",
    ]

    flavors = [
        "Observers gasp as you stabilize the code rift.",
        "Guild archivists scribble your technique in shimmering ink.",
        "A holographic mentor nods approvingly from the Bug Hall.",
        "A swarm of friendly lints salute your precision.",
    ]

    def _base_stats(self, existing: Optional[Dict]) -> Dict:
        if existing is None:
            return {"submissions": 0, "scans": 0, "xp": 0, "badges": []}
        badges = existing.get("badges")
        return {
            "submissions": existing.get("submissions_count", 0),
            "scans": existing.get("scans_count", 0),
            "xp": existing.get("xp", 0),
            "badges": badges.split(",") if badges else [],
        }

    def score_submission(
        self,
        *,
        severity: str,
        tag_count: int,
        code_length: int,
        existing_stats: Optional[Dict] = None,
    ) -> RewardBundle:
        stats = self._base_stats(existing_stats)
        xp = 40
        xp += self.severity_weights.get(severity.lower(), 20)
        xp += min(tag_count * 4, 20)
        xp += min(code_length // 80, 25)
        xp += random.randint(0, 10)

        badges = self._assign_badges(stats, xp_gain=xp, submission=True)
        artifact = random.choice(self.artifacts)
        flavor = random.choice(self.flavors)
        return RewardBundle(xp=xp, badges=badges, artifact=artifact, flavor=flavor)

    def score_scan(
        self,
        *,
        similarity: float,
        snippets_checked: int,
        existing_stats: Optional[Dict] = None,
    ) -> RewardBundle:
        stats = self._base_stats(existing_stats)
        base = 10 + int(similarity * 40)
        base += min(snippets_checked * 2, 10)
        base += random.randint(0, 6)
        badges = self._assign_badges(stats, xp_gain=base, submission=False)
        artifact = random.choice(self.artifacts)
        flavor = random.choice(self.flavors)
        return RewardBundle(xp=base, badges=badges, artifact=artifact, flavor=flavor)

    def _assign_badges(self, stats: Dict, *, xp_gain: int, submission: bool) -> List[str]:
        unlocked = []
        stats_future = stats.copy()
        stats_future["xp"] = stats.get("xp", 0) + xp_gain
        if submission:
            stats_future["submissions"] = stats.get("submissions", 0) + 1
        else:
            stats_future["scans"] = stats.get("scans", 0) + 1

        owned = set(stats.get("badges", []))
        for name, rule in self.badge_catalog.items():
            try:
                if rule(stats_future) and name not in owned:
                    unlocked.append(name)
            except Exception:
                continue
        return unlocked

    def build_progress_meter(self, xp: int) -> Dict[str, int]:
        level = xp // 200 + 1
        into_level = xp % 200
        return {"level": level, "progress": int((into_level / 200) * 100)}
