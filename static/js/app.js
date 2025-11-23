document.addEventListener("DOMContentLoaded", () => {
  const refreshBtn = document.querySelector("[data-fetch-leaderboard]");
  const storyBtn = document.querySelector("[data-roll-story]");

  if (refreshBtn) {
    refreshBtn.addEventListener("click", async () => {
      refreshBtn.disabled = true;
      refreshBtn.textContent = "Syncing...";
      try {
        const resp = await fetch("/api/leaderboard");
        const data = await resp.json();
        const list = document.querySelector(".leaderboard");
        list.innerHTML = "";
        data.forEach((player, idx) => {
          const li = document.createElement("li");
          li.innerHTML = `
            <div class="alias">${idx + 1}. ${player.alias}</div>
            <div class="xp">${player.xp} xp</div>
            <div class="meta">⚔️ ${player.submissions} • 🔍 ${player.scans}</div>
            ${player.badges.length ? `<div class="badges">${player.badges.join(', ')}</div>` : ""}
          `;
          list.appendChild(li);
        });
      } catch (err) {
        console.error(err);
      } finally {
        refreshBtn.disabled = false;
        refreshBtn.textContent = "Refresh signal";
      }
    });
  }

  if (storyBtn) {
    const hype = [
      "A rogue pointer tried to escape but you lassoed it back.",
      "New anomaly: caffeine overflow. Mitigated with tea injections.",
      "A cosmic linter beams: 'no semi, no peace'.",
      "Guild seers foresee a segmentation fault at dawn. Prepare!",
    ];
    storyBtn.addEventListener("click", () => {
      alert(hype[Math.floor(Math.random() * hype.length)]);
    });
  }
});
