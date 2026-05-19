(function () {
  "use strict";

  const TOUR_KEY = "schooledule_tour_admin_visto";

  const pasos = [
    {
      element: ".admin-sidebar",
      popover: {
        title: "Bienvenido a Schooledule",
        description:
          "Panel de administración multi-centro. Desde el menú lateral accedes a todos los módulos del sistema.",
        side: "right",
        align: "start",
      },
    },
    {
      element: ".stat-grid",
      popover: {
        title: "Estadísticas globales",
        description:
          "Resumen en tiempo real: centros activos, usuarios, matrículas y módulos del curso actual.",
        side: "top",
        align: "start",
      },
    },
    {
      element: 'a[href="/admin/usuarios"]',
      popover: {
        title: "Gestión de usuarios",
        description:
          "Crea usuarios individuales o importa alumnos en bloque desde Excel.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/centros"]',
      popover: {
        title: "Centros educativos",
        description:
          "Gestiona los centros de formación. Cada centro tiene sus propios grupos, módulos y profesores.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/cursos"]',
      popover: {
        title: "Cursos académicos",
        description:
          "Define los años lectivos del sistema. El curso activo determina qué matrículas y notas están vigentes.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/grupos"]',
      popover: {
        title: "Grupos de alumnos",
        description:
          "Organiza a los alumnos por clase, centro y curso académico.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/modulos"]',
      popover: {
        title: "Módulos formativos",
        description:
          "Importa módulos con sus RAs y CEs desde una plantilla Excel. Aquí se define el currículo académico.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/alumnos"]',
      popover: {
        title: "Alumnos",
        description:
          "Consulta las matrículas activas de cada alumno y su asignación a grupos e imparticiones.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/imparticiones"]',
      popover: {
        title: "Imparticiones",
        description:
          "Asigna cada módulo a un grupo con su profesor responsable.",
        side: "right",
        align: "center",
      },
    },
    {
      element: 'a[href="/admin/auditoria"]',
      popover: {
        title: "Auditoría de notas",
        description:
          "Registro inmutable de todos los cambios de calificaciones. Cada modificación queda trazada.",
        side: "right",
        align: "center",
      },
    },
    {
      element: ".sidebar-footer",
      popover: {
        title: "¿Necesitas ayuda?",
        description:
          'Puedes repetir este tour en cualquier momento desde el enlace "Tour guiado" del menú.',
        side: "right",
        align: "end",
      },
    },
  ];

  function iniciarTourAdmin() {
    const driverInstance = window.driver.js.driver({
      showProgress: true,
      nextBtnText: "Siguiente →",
      prevBtnText: "← Anterior",
      doneBtnText: "Finalizar",
      onDestroyStarted: function () {
        localStorage.setItem(TOUR_KEY, "true");
        driverInstance.destroy();
      },
      steps: pasos,
    });
    driverInstance.drive();
  }

  window.iniciarTourAdmin = iniciarTourAdmin;

  document.addEventListener("DOMContentLoaded", function () {
    const params = new URLSearchParams(location.search);
    if (params.get("tour") === "1") {
      const url = new URL(location.href);
      url.searchParams.delete("tour");
      history.replaceState({}, "", url);
      iniciarTourAdmin();
    } else if (!localStorage.getItem(TOUR_KEY)) {
      iniciarTourAdmin();
    }
  });
})();
