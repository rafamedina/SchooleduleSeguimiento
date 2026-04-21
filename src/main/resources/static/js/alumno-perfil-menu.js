(function () {
  const btn = document.getElementById("perfilMenuButton");
  if (!btn) return;
  const items = btn.nextElementSibling;
  const close = () => {
    btn.setAttribute("aria-expanded", "false");
    items.classList.remove("is-open");
  };
  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    const open = btn.getAttribute("aria-expanded") === "true";
    btn.setAttribute("aria-expanded", String(!open));
    items.classList.toggle("is-open", !open);
  });
  document.addEventListener("click", (e) => {
    if (!items.contains(e.target) && e.target !== btn) close();
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") close();
  });
})();
