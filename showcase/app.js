const buttons = document.querySelectorAll(".install-btn");

buttons.forEach((btn) => {
  btn.addEventListener("click", () => {
    const target = btn.dataset.target;
    const shell = document.querySelector(`[data-progress-shell="${target}"]`);
    if (!shell) return;

    const bar = shell.querySelector(".progress-bar");
    const label = shell.querySelector(".progress-label");

    shell.style.display = "flex";
    shell.classList.remove("done");
    label.textContent = "installing…";
    bar.style.setProperty("--progress", "0%");

    let progress = 0;
    const interval = setInterval(() => {
      progress += Math.random() * 20;
      if (progress >= 100) {
        progress = 100;
        clearInterval(interval);
        shell.classList.add("done");
        label.textContent = "Done! Enjoy the boost.";
      }
      bar.style.setProperty("--progress", `${progress}%`);
    }, 250);
  });
});
