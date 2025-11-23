from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from bugverse.similarity import rank_similar_bugs, similarity_score


def test_similarity_score_handles_simple_match():
    base = "print('hello world')\nreturn data"
    suspect = "print('hello world')\nreturn data.strip()"
    score = similarity_score(suspect, base)
    assert 0.5 < score <= 1


def test_rank_similar_bugs_orders_results():
    rows = [
        {"buggy_code": "for i in range(len(items)): items[i+1]", "fix_description": "", "severity": "high", "bug_name": "oops"},
        {"buggy_code": "if request.json['email']:", "fix_description": "", "severity": "low", "bug_name": "null"},
    ]
    matches = rank_similar_bugs(query_snippet="for i in range(len(items)):", bug_rows=rows, limit=2, threshold=0.1)
    assert matches
    assert matches[0]["score"] >= matches[-1]["score"]
