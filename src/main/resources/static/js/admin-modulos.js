const modal = document.getElementById("moduloResumenModal");
const titleEl = document.getElementById("resumenModalTitle");
const metaEl = document.getElementById("resumenModalMeta");
const bodyEl = document.getElementById("resumenModalBody");
const closeBtn = document.getElementById("resumenModalClose");

document.addEventListener("DOMContentLoaded", () => {
  closeBtn.addEventListener("click", () => modal.close());
  modal.addEventListener("click", (e) => {
    if (e.target === modal) modal.close();
  });

  document.addEventListener("click", (e) => {
    const btn = e.target.closest("[data-action='ver-resumen']");
    if (btn) abrirResumenModulo(btn.dataset.moduloId);
  });
});

async function abrirResumenModulo(id) {
  titleEl.textContent = "Cargando…";
  metaEl.textContent = "";
  bodyEl.textContent = "";
  modal.showModal();
  try {
    const resp = await fetch(`/admin/modulos/${id}/resumen`, {
      headers: { Accept: "application/json" },
    });
    if (!resp.ok) throw new Error(`Error ${resp.status}`);
    const data = await resp.json();
    renderResumen(data);
  } catch (err) {
    const msg = document.createElement("p");
    msg.textContent = "No se pudo cargar el resumen: " + err.message;
    bodyEl.textContent = "";
    bodyEl.appendChild(msg);
  }
}

function renderResumen(d) {
  titleEl.textContent = d.nombre;

  const plural = (n, s) => `${n} ${s}${n !== 1 ? "s" : ""}`;
  metaEl.textContent =
    `${d.codigo} · ${d.activo ? "Activo" : "Inactivo"} · ` +
    `${plural(d.numImparticiones, "impartición")} · ` +
    `${plural(d.numCursosConRas, "curso")} con RAs · ` +
    `${plural(d.numRasTotal, "RA")} · ` +
    `${plural(d.numCesTotal, "CE")}`;

  bodyEl.textContent = "";

  if (d.cursos.length === 0) {
    const empty = document.createElement("p");
    empty.className = "resumen-empty";
    empty.textContent =
      "Este módulo no tiene resultados de aprendizaje definidos.";
    bodyEl.appendChild(empty);
    return;
  }

  d.cursos.forEach((curso) => {
    const sec = document.createElement("section");
    sec.className = "resumen-curso";

    const h3 = document.createElement("h3");
    h3.className = "resumen-curso__title";
    h3.textContent =
      `${curso.cursoNombre} — ` +
      `${plural(curso.numRas, "RA")}, ${plural(curso.numCes, "CE")}`;
    sec.appendChild(h3);

    curso.ras.forEach((ra) => {
      const raDiv = document.createElement("div");
      raDiv.className = "resumen-ra";

      const header = document.createElement("div");
      header.className = "resumen-ra__header";

      const code = document.createElement("span");
      code.className = "resumen-ra__code";
      code.textContent = ra.codigo;

      const desc = document.createElement("span");
      desc.className = "resumen-ra__desc";
      desc.textContent = ra.descripcion;

      const peso = document.createElement("span");
      peso.className = "resumen-ra__peso";
      peso.textContent = ra.pesoSugerido != null ? `${ra.pesoSugerido}%` : "";

      header.appendChild(code);
      header.appendChild(desc);
      header.appendChild(peso);
      raDiv.appendChild(header);

      if (ra.ces && ra.ces.length > 0) {
        const cesDiv = document.createElement("div");
        cesDiv.className = "resumen-ces";

        ra.ces.forEach((ce) => {
          const chip = document.createElement("span");
          chip.className = "resumen-ce-chip";
          chip.title =
            ce.descripcion +
            (ce.peso != null ? ` · ${ce.peso}%` : "") +
            (ce.instrumento ? ` · ${ce.instrumento}` : "");

          const ceCode = document.createElement("span");
          ceCode.className = "resumen-ce-chip__code";
          ceCode.textContent = ce.codigo;
          chip.appendChild(ceCode);

          if (ce.peso != null) {
            chip.appendChild(document.createTextNode(` ${ce.peso}%`));
          }

          cesDiv.appendChild(chip);
        });

        raDiv.appendChild(cesDiv);
      }

      sec.appendChild(raDiv);
    });

    bodyEl.appendChild(sec);
  });
}
