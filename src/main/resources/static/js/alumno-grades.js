/**
 * Modal de calificaciones del alumno (read-only).
 * Misma jerarquía que el profe: Periodo → RA (ItemEvaluable) → CE (CriterioEvaluacion).
 * Sin inputs editables ni botón Guardar: se limita a mostrar las notas de la BD.
 */

let lastFocused = null;

document.addEventListener("DOMContentLoaded", () => {
  wireCardClicks();
  wireModalButtons();
});

function wireCardClicks() {
  document.querySelectorAll(".asignatura-card").forEach((card) => {
    const matriculaId = card.dataset.matriculaId;
    if (!matriculaId) return;
    card.addEventListener("click", () => openModal(matriculaId));
    card.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        openModal(matriculaId);
      }
    });
  });
}

function wireModalButtons() {
  const modal = document.getElementById("gradesModal");
  if (!modal) return;
  document.getElementById("closeGrades").addEventListener("click", closeModal);
  document
    .getElementById("closeGradesTop")
    .addEventListener("click", closeModal);
  modal.addEventListener("cancel", (e) => {
    e.preventDefault();
    closeModal();
  });
}

async function openModal(matriculaId) {
  const modal = document.getElementById("gradesModal");
  const body = document.getElementById("gradesBody");

  body.innerHTML =
    '<p style="padding:1rem;color:var(--ink-muted)">Cargando…</p>';
  modal.showModal();
  trapFocus(modal);

  try {
    const resp = await fetch(`/alumno/api/matricula/${matriculaId}/notas`, {
      headers: { Accept: "application/json" },
    });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    const data = await resp.json();
    renderModal(data);
  } catch (err) {
    body.innerHTML = `<p style="padding:1rem;color:var(--hot)">Error al cargar las notas: ${escHtml(
      err.message,
    )}</p>`;
  }
}

function renderModal(data) {
  document.getElementById("gradesModalTitle").textContent = data.alumnoNombre;
  document.getElementById("gradesModalLabel").textContent =
    data.imparticionLabel;
  updateMediaGlobal(data.mediaGlobal);

  const body = document.getElementById("gradesBody");
  body.innerHTML = "";

  if (!data.periodos || data.periodos.length === 0) {
    body.innerHTML =
      '<p style="padding:1rem;color:var(--ink-muted)">Todavía no hay periodos configurados para esta asignatura.</p>';
    return;
  }

  data.periodos.forEach((p) => {
    body.appendChild(renderPeriodo(p));
  });
}

function renderPeriodo(periodo) {
  const section = document.createElement("section");
  section.className =
    "periodo-section" + (periodo.cerrado ? " periodo-section--cerrado" : "");
  section.dataset.periodoId = periodo.id;

  const pesoLabel = periodo.peso != null ? `${periodo.peso}%` : "—";
  const mediaFmt = periodo.media != null ? formatNota(periodo.media) : "—";

  section.innerHTML = `
    <div class="periodo-header">
      <span class="periodo-title">${escHtml(periodo.periodoNombre)}</span>
      <span class="peso-chip">Peso ${pesoLabel}</span>
      ${periodo.cerrado ? '<span class="cerrado-chip">Cerrado</span>' : ""}
      <span class="periodo-media">${mediaFmt}</span>
    </div>`;

  if (!periodo.items || periodo.items.length === 0) {
    const empty = document.createElement("p");
    empty.className = "periodo-empty";
    empty.style.cssText =
      "color:var(--ink-muted);font-size:.875rem;padding:.5rem 0;";
    empty.textContent = "Sin actividades evaluables en este periodo.";
    section.appendChild(empty);
    return section;
  }

  periodo.items.forEach((item) => {
    section.appendChild(renderRaBlock(periodo, item));
  });

  return section;
}

function renderRaBlock(periodo, item) {
  const details = document.createElement("details");
  details.className = "ra-block";
  details.open = true;

  const mediaRaFmt = item.mediaRa != null ? formatNota(item.mediaRa) : "—";

  const summary = document.createElement("summary");
  summary.className = "ra-block__summary";
  summary.innerHTML = `
    <span class="ra-block__toggle">▸</span>
    <span class="ra-block__code">${escHtml(item.raCodigo)}</span>
    <span class="ra-block__name" title="${escHtml(
      item.raDescripcion,
    )}">${escHtml(item.itemNombre)}</span>
    <span class="ra-block__media">${mediaRaFmt}</span>`;

  details.appendChild(summary);

  const table = document.createElement("table");
  table.className = "ce-table";
  table.setAttribute(
    "aria-label",
    `Criterios de evaluación de ${item.raCodigo}`,
  );
  table.innerHTML = `
    <thead>
      <tr>
        <th scope="col">CE</th>
        <th scope="col">Descripción</th>
        <th scope="col" style="text-align:right">Nota</th>
        <th scope="col">Comentario</th>
      </tr>
    </thead>
    <tbody></tbody>`;

  const tbody = table.querySelector("tbody");
  item.criterios.forEach((ce) => {
    tbody.appendChild(renderCeRow(ce));
  });

  details.appendChild(table);
  return details;
}

function renderCeRow(ce) {
  const tr = document.createElement("tr");
  const valorTxt = ce.valor != null ? formatNota(ce.valor) : "—";
  const notaClass =
    ce.valor != null && parseFloat(ce.valor) < 5
      ? "nota-badge nota-badge--fail"
      : ce.valor != null
        ? "nota-badge nota-badge--pass"
        : "";

  tr.innerHTML = `
    <td class="ce-table__code">${escHtml(ce.codigo)}</td>
    <td class="ce-table__desc">${escHtml(ce.descripcion)}</td>
    <td style="text-align:right">
      <span class="${notaClass}">${valorTxt}</span>
    </td>
    <td class="ce-table__desc">${escHtml(ce.comentario ?? "—")}</td>`;

  return tr;
}

function closeModal() {
  const modal = document.getElementById("gradesModal");
  modal.close();
  releaseFocus();
}

function updateMediaGlobal(media) {
  const el = document.getElementById("mediaGlobal");
  if (!el) return;
  el.textContent = media != null ? formatNota(media) : "—";
}

const FOCUSABLE =
  'button, [href], input:not([disabled]), select, textarea, [tabindex]:not([tabindex="-1"])';

function trapFocus(modal) {
  lastFocused = document.activeElement;
  const focusable = modal.querySelectorAll(FOCUSABLE);
  if (focusable.length) focusable[0].focus();
  modal.addEventListener("keydown", handleFocusTrap);
}

function handleFocusTrap(e) {
  if (e.key !== "Tab") return;
  const modal = document.getElementById("gradesModal");
  const focusable = Array.from(modal.querySelectorAll(FOCUSABLE));
  if (focusable.length === 0) return;
  const first = focusable[0];
  const last = focusable[focusable.length - 1];

  if (e.shiftKey && document.activeElement === first) {
    e.preventDefault();
    last.focus();
  } else if (!e.shiftKey && document.activeElement === last) {
    e.preventDefault();
    first.focus();
  }
}

function releaseFocus() {
  const modal = document.getElementById("gradesModal");
  modal.removeEventListener("keydown", handleFocusTrap);
  if (lastFocused) lastFocused.focus();
}

function formatNota(n) {
  return parseFloat(n).toFixed(2);
}

function escHtml(str) {
  if (str == null) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
