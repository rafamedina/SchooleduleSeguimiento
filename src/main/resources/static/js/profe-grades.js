/**
 * Lógica del modal de calificaciones del profesor.
 * Jerarquía: Periodo → RA (ItemEvaluable) → CE (CriterioEvaluacion).
 * Vanilla JS, sin frameworks. CSRF leído de meta tags (Spring Security).
 */

// ── Estado interno ─────────────────────────────────────────────────────────────
let currentMatriculaId = null;
let currentData = null; // TeacherStudentGradesDTO del servidor

// ── Inicio ─────────────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  wireRosterClicks();
  wireModalButtons();
});

/** Vincula clicks y teclas en cada fila de alumno. */
export function wireRosterClicks() {
  document.querySelectorAll(".roster__item").forEach((row) => {
    row.addEventListener("click", () => openModal(row.dataset.matriculaId));
    row.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        openModal(row.dataset.matriculaId);
      }
    });
  });
}

/** Vincula botones de cierre y guardado del modal. */
function wireModalButtons() {
  const modal = document.getElementById("gradesModal");
  document.getElementById("closeGrades").addEventListener("click", closeModal);
  document
    .getElementById("closeGradesTop")
    .addEventListener("click", closeModal);
  document.getElementById("saveGrades").addEventListener("click", saveGrades);

  modal.addEventListener("cancel", (e) => {
    e.preventDefault();
    closeModal();
  });
}

/** Abre el modal cargando las notas del alumno via API. */
async function openModal(matriculaId) {
  currentMatriculaId = matriculaId;
  const modal = document.getElementById("gradesModal");
  const body = document.getElementById("gradesBody");

  body.innerHTML =
    '<p style="padding:1rem;color:var(--ink-muted)">Cargando…</p>';
  modal.showModal();
  trapFocus(modal);

  try {
    const resp = await fetch(`/profe/api/matricula/${matriculaId}/notas`, {
      headers: { Accept: "application/json" },
    });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    currentData = await resp.json();
    renderModal(currentData);
  } catch (err) {
    body.innerHTML = `<p style="padding:1rem;color:var(--hot)">Error al cargar las notas: ${escHtml(
      err.message,
    )}</p>`;
  }
}

/** Renderiza todo el contenido del modal a partir del DTO del servidor. */
function renderModal(data) {
  document.getElementById("gradesModalTitle").textContent = data.alumnoNombre;
  document.getElementById("gradesModalLabel").textContent =
    data.imparticionLabel;
  updateMediaGlobal(data.mediaGlobal);

  const body = document.getElementById("gradesBody");
  body.innerHTML = "";

  data.periodos.forEach((p) => {
    body.appendChild(renderPeriodo(p));
  });
}

/** Construye el DOM de un periodo con sus bloques de RA. */
function renderPeriodo(periodo) {
  const section = document.createElement("section");
  section.className =
    "periodo-section" + (periodo.cerrado ? " periodo-section--cerrado" : "");
  section.dataset.periodoId = periodo.id;
  section.dataset.peso = periodo.peso != null ? periodo.peso : "1";

  const pesoLabel = periodo.peso != null ? `${periodo.peso}%` : "—";
  const mediaFmt = periodo.media != null ? formatNota(periodo.media) : "—";

  section.innerHTML = `
    <div class="periodo-header">
      <span class="periodo-title">${escHtml(periodo.periodoNombre)}</span>
      <span class="peso-chip">Peso ${pesoLabel}</span>
      ${periodo.cerrado ? '<span class="cerrado-chip">Cerrado</span>' : ""}
      <span class="periodo-media" data-periodo-media="${
        periodo.id
      }">${mediaFmt}</span>
    </div>`;

  periodo.items.forEach((item) => {
    section.appendChild(renderRaBlock(periodo, item));
  });

  return section;
}

/** Construye un bloque <details> para un RA con sus CEs. */
function renderRaBlock(periodo, item) {
  const details = document.createElement("details");
  details.className = "ra-block";
  details.open = true;
  details.dataset.raId = item.resultadoAprendizajeId;
  details.dataset.periodoId = periodo.id;

  const mediaRaFmt = item.mediaRa != null ? formatNota(item.mediaRa) : "—";

  const summary = document.createElement("summary");
  summary.className = "ra-block__summary";
  summary.innerHTML = `
    <span class="ra-block__toggle">▸</span>
    <span class="ra-block__code">${escHtml(item.raCodigo)}</span>
    <span class="ra-block__name" title="${escHtml(
      item.raDescripcion,
    )}">${escHtml(item.itemNombre)}</span>
    <span class="ra-block__media" data-ra-media="${
      item.resultadoAprendizajeId
    }-${periodo.id}">${mediaRaFmt}</span>`;

  details.appendChild(summary);

  // Tabla de CEs
  const table = document.createElement("table");
  table.className = "ce-table";
  table.setAttribute(
    "aria-label",
    `Criterios de evaluación de ${escHtml(item.raCodigo)}`,
  );
  table.innerHTML = `
    <thead>
      <tr>
        <th scope="col">CE</th>
        <th scope="col">Descripción</th>
        <th scope="col">Nota</th>
        <th scope="col">Comentario</th>
      </tr>
    </thead>
    <tbody></tbody>`;

  const tbody = table.querySelector("tbody");
  item.criterios.forEach((ce) => {
    tbody.appendChild(renderCeRow(periodo, item, ce));
  });

  details.appendChild(table);
  return details;
}

/** Construye una fila para un criterio de evaluación. */
function renderCeRow(periodo, item, ce) {
  const tr = document.createElement("tr");
  const valorStr = ce.valor != null ? ce.valor : "";
  const ceKey = `${item.resultadoAprendizajeId}-${periodo.id}`;

  tr.innerHTML = `
    <td class="ce-table__code">${escHtml(ce.codigo)}</td>
    <td class="ce-table__desc">${escHtml(ce.descripcion)}</td>
    <td>
      <label class="sr-only" for="nota-ce-${ce.criterioEvaluacionId}">
        Nota para CE ${escHtml(ce.codigo)} de ${escHtml(
          item.raCodigo,
        )} en ${escHtml(periodo.periodoNombre)}
      </label>
      <input
        class="ce-nota-input grade-input"
        type="number"
        id="nota-ce-${ce.criterioEvaluacionId}"
        name="nota-ce-${ce.criterioEvaluacionId}"
        step="0.01" min="0" max="10"
        data-ce-id="${ce.criterioEvaluacionId}"
        data-ra-key="${ceKey}"
        data-periodo-id="${periodo.id}"
        data-calificacion-id="${ce.calificacionId ?? ""}"
        value="${valorStr}"
        ${periodo.cerrado ? 'disabled aria-disabled="true"' : ""}
        aria-label="Nota para criterio ${escHtml(ce.codigo)} de ${escHtml(
          item.raCodigo,
        )}"/>
    </td>
    <td>
      <label class="sr-only" for="comment-ce-${ce.criterioEvaluacionId}">
        Comentario para CE ${escHtml(ce.codigo)} de ${escHtml(item.raCodigo)}
      </label>
      <input
        class="ce-comment-input comment-input"
        type="text"
        id="comment-ce-${ce.criterioEvaluacionId}"
        name="comment-ce-${ce.criterioEvaluacionId}"
        data-ce-id="${ce.criterioEvaluacionId}"
        maxlength="1000"
        value="${escHtml(ce.comentario ?? "")}"
        ${periodo.cerrado ? 'disabled aria-disabled="true"' : ""}
        placeholder="Comentario opcional"/>
    </td>`;

  tr.querySelector(".ce-nota-input").addEventListener("input", () => {
    recomputeClientMedias(ceKey, periodo.id);
  });

  return tr;
}

/**
 * Recalcula las medias en cascada: CE inputs → mediaRa → mediaPeriodo → mediaGlobal.
 * El servidor es la fuente autoritativa; esto es solo retroalimentación visual.
 */
function recomputeClientMedias(raKey, periodoId) {
  // 1. mediaRa = media de los CE inputs del RA
  const raInputs = document.querySelectorAll(
    `input.grade-input[data-ra-key="${raKey}"]`,
  );
  const raValues = [];
  raInputs.forEach((inp) => {
    const v = parseFloat(inp.value);
    if (!isNaN(v) && inp.value.trim() !== "") raValues.push(v);
  });
  const mediaRa =
    raValues.length > 0
      ? raValues.reduce((a, b) => a + b, 0) / raValues.length
      : null;

  const raMediaEl = document.querySelector(`[data-ra-media="${raKey}"]`);
  if (raMediaEl)
    raMediaEl.textContent = mediaRa != null ? formatNota(mediaRa) : "—";

  // 2. mediaPeriodo = media de todos los mediaRa del periodo
  const allRaMediaEls = document.querySelectorAll(
    `section[data-periodo-id="${periodoId}"] [data-ra-media]`,
  );
  const periodoValues = [];
  allRaMediaEls.forEach((el) => {
    const v = parseFloat(el.textContent);
    if (!isNaN(v)) periodoValues.push(v);
  });
  const mediaPeriodo =
    periodoValues.length > 0
      ? periodoValues.reduce((a, b) => a + b, 0) / periodoValues.length
      : null;

  const periodoMediaEl = document.querySelector(
    `[data-periodo-media="${periodoId}"]`,
  );
  if (periodoMediaEl)
    periodoMediaEl.textContent =
      mediaPeriodo != null ? formatNota(mediaPeriodo) : "—";

  // 3. mediaGlobal = Σ(mediaPeriodo * peso) / Σ(peso)
  recomputeGlobalMedia();
}

function recomputeGlobalMedia() {
  const periodoSections = document.querySelectorAll("section[data-periodo-id]");
  let sumaPonderada = 0;
  let sumaPesos = 0;

  periodoSections.forEach((sec) => {
    const pid = sec.dataset.periodoId;
    const peso = parseFloat(sec.dataset.peso) || 1;
    const mediaEl = sec.querySelector(`[data-periodo-media="${pid}"]`);
    if (!mediaEl) return;
    const v = parseFloat(mediaEl.textContent);
    if (!isNaN(v)) {
      sumaPonderada += v * peso;
      sumaPesos += peso;
    }
  });

  const mediaGlobal = sumaPesos > 0 ? sumaPonderada / sumaPesos : null;
  updateMediaGlobal(mediaGlobal);
}

/** Envía las notas al servidor y re-renderiza el modal con la respuesta autoritativa. */
async function saveGrades() {
  const btn = document.getElementById("saveGrades");
  btn.disabled = true;
  btn.textContent = "Guardando…";

  const entries = [];
  document
    .querySelectorAll("input.grade-input:not([disabled])")
    .forEach((inp) => {
      const ceId = parseInt(inp.dataset.ceId, 10);
      const valorRaw = inp.value.trim();
      const valor = valorRaw !== "" ? parseFloat(valorRaw) : null;
      const commentEl = document.getElementById(`comment-ce-${ceId}`);
      const comentario = commentEl ? commentEl.value.trim() || null : null;
      entries.push({ criterioEvaluacionId: ceId, valor, comentario });
    });

  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector(
    'meta[name="_csrf_header"]',
  )?.content;

  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

  try {
    const resp = await fetch(
      `/profe/api/matricula/${currentMatriculaId}/notas`,
      {
        method: "POST",
        headers,
        body: JSON.stringify({
          matriculaId: parseInt(currentMatriculaId, 10),
          entries,
        }),
      },
    );

    if (!resp.ok) {
      const errData = await resp.json().catch(() => ({}));
      throw new Error(
        errData.message || errData.error || `HTTP ${resp.status}`,
      );
    }

    currentData = await resp.json();
    renderModal(currentData);
  } catch (err) {
    alert(`Error al guardar: ${err.message}`);
  } finally {
    const saveBtn = document.getElementById("saveGrades");
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.textContent = "Guardar";
    }
  }
}

/** Cierra el modal y devuelve el foco al elemento que lo abrió. */
function closeModal() {
  const modal = document.getElementById("gradesModal");
  modal.close();
  currentMatriculaId = null;
  currentData = null;
  releaseFocus();
}

/** Actualiza el display de media global. */
function updateMediaGlobal(media) {
  const el = document.getElementById("mediaGlobal");
  if (!el) return;
  el.textContent = media != null ? formatNota(media) : "—";
}

// ── Focus trap (accesibilidad modal) ────────────────────────────────────────────
let lastFocused = null;
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

// ── Utilidades ──────────────────────────────────────────────────────────────────
function formatNota(n) {
  return parseFloat(n).toFixed(2);
}

function escHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
