document.addEventListener("DOMContentLoaded", () => {
  // Referencias al DOM
  const formulario = document.getElementById("formulario");
  const email = document.getElementById("email");
  const password = document.getElementById("password");
  const toast = document.getElementById("toast");

  // Limpieza preventiva de sesión al cargar la página
  fetch("/killSession").catch((err) => console.log("Sesión limpia"));

  // --- EVENTO PRINCIPAL: LOGIN ---
  formulario.addEventListener("submit", (e) => {
    e.preventDefault(); // Evita que la página se recargue sola

    const emailMetido = email.value.trim();
    const passwordMetido = password.value.trim();

    // 1. Petición de Login
    fetch("/loginSession", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: emailMetido,
        password: passwordMetido,
      }),
    })
      .then(async (response) => {
        // Leemos la respuesta como texto primero para evitar errores de parseo
        const textoRespuesta = await response.text();
        let data;

        try {
          // Intentamos convertir a JSON (lo normal si todo va bien)
          data = JSON.parse(textoRespuesta);
        } catch (err) {
          // Si falla, es que el servidor devolvió texto plano
          data = { message: textoRespuesta };
        }

        if (response.ok) {
          return data; // Pasamos los datos al siguiente .then
        } else {
          // Si hay error (401, 404), lanzamos una excepción con el mensaje del servidor
          throw new Error(
            data.message || data.error || textoRespuesta || "Error desconocido",
          );
        }
      })
      .then((data) => {
        console.log("Login correcto, analizando roles...", data);

        // A. Convertimos el string de roles en Array
        // Ejemplo: "-ADMIN-PROFESOR" -> ["ADMIN", "PROFESOR"]
        // El .filter(Boolean) elimina los huecos vacíos
        const listaRoles = data.roles
          ? data.roles.split("-").filter(Boolean)
          : [];

        // B. Decidimos qué hacer
        if (listaRoles.length > 1) {
          // CASO 1: Muchos roles -> Abrir modal
          abrirDialogoRoles(listaRoles);
        } else if (listaRoles.length === 1) {
          // CASO 2: Un rol -> Entrar directo
          seleccionarRol(listaRoles[0]);
        } else {
          throw new Error("El usuario no tiene roles asignados");
        }
      })
      .catch((error) => {
        mostrarError(error.message);
      });
  });

  // --- FUNCIONES AUXILIARES ---

  function abrirDialogoRoles(roles) {
    const dialog = document.getElementById("dialogoRoles");
    const contenedor = document.getElementById("contenedorBotones");

    // Limpiamos botones anteriores
    contenedor.innerHTML = "";

    roles.forEach((rol) => {
      const btn = document.createElement("button");

      // ¡IMPORTANTE! type="button" evita que este botón envíe el formulario de fondo
      btn.type = "button";
      btn.textContent = rol;
      btn.className = "btn-rol"; // Clase opcional para CSS

      btn.onclick = () => {
        dialog.close();
        seleccionarRol(rol);
      };

      contenedor.appendChild(btn);
    });

    // Abrimos el modal nativo
    if (typeof dialog.showModal === "function") {
      dialog.showModal();
    } else {
      alert("Tu navegador no soporta <dialog>. Por favor actualízalo.");
    }
  }

  function seleccionarRol(rolElegido) {
    console.log("Rol seleccionado:", rolElegido);

    // 2. Petición de Selección de Rol
    fetch("/control", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ rol: rolElegido }),
    })
      .then((response) => {
        if (response.ok) return response.json();
        throw new Error("Error al establecer el perfil");
      })
      .then((data) => {
        // 3. Redirección Final
        console.log("Redirigiendo a:", data.redirectUrl);
        window.location.href = data.redirectUrl;
      })
      .catch((error) => {
        console.error(error);
        mostrarError("Error al acceder al panel: " + error.message);
      });
  }

  function mostrarError(mensaje) {
    console.error("Error UI:", mensaje);
    toast.textContent = mensaje;
    toast.style.display = "flex";
    toast.style.backgroundColor = "#e74c3c"; // Rojo error

    setTimeout(() => {
      toast.style.display = "none";
    }, 3000);
  }
});
