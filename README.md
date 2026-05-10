<div align="center">

![GAMV Banner](https://img.shields.io/badge/GAMV-Gestor%20de%20M%C3%A1quinas%20Virtuales-00BCD4?style=for-the-badge&logo=android&logoColor=white)

![Android](https://img.shields.io/badge/Android-API%2029+-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-11-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![CI](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

</div>

---

**GAMV** *(Gestor Android de Máquinas Virtuales)* es una aplicación Android que permite gestionar máquinas virtuales KVM/QEMU alojadas en servidores remotos, mediante conexión SSH. Desde el dispositivo móvil es posible consultar el estado de cada máquina, encenderla, apagarla, pausarla, hibernarla, reiniciarla y administrar sus instantáneas, sin necesidad de acceder físicamente al servidor.

La aplicación sigue una arquitectura por capas (_Clean Architecture_) con el patrón MVVM, separando claramente la lógica de negocio de la capa de datos y de la interfaz de usuario.

---

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 11 |
| Arquitectura | Clean Architecture + MVVM |
| Base de datos local | Room (SQLite) |
| Cifrado | Google Tink (AES-256-GCM) + Android Keystore |
| Conexión SSH | JSch |
| Inyección de dependencias | ServiceLocator manual |
| Tests unitarios | JUnit 4 + Mockito |
| Tests de integración | Espresso + Apache SSHD (servidor SSH embebido) |
| CI/CD | GitHub Actions |
| Versión mínima de Android | API 29 (Android 10) |

---

## Manual de usuario

La aplicación se organiza en torno a tres secciones accesibles desde la barra de navegación inferior: **Inicio**, **Añadir** y **Ajustes**.

### Gestión de anfitriones

En la pantalla de inicio se muestra la lista de anfitriones guardados. Al pulsar sobre una tarjeta se inicia la conexión SSH y se navega a la pantalla de máquinas virtuales de ese servidor. Cada tarjeta incluye un botón para **editar** el anfitrión (icono de lápiz) y otro para **eliminarlo** (icono de papelera). La eliminación no puede deshacerse.

Para añadir o editar un anfitrión, es necesario completar el siguiente formulario:

| Campo | Obligatorio | Descripción |
|---|:---:|---|
| Nombre | Sí | Nombre para identificar el anfitrión. Máximo 20 caracteres. |
| IP | Sí | Dirección IPv4 o nombre de dominio del servidor (p. ej., `192.168.1.10`). |
| Puerto | No | Puerto SSH. Si se deja vacío, se usará el puerto 22 por defecto. Valor entre 1 y 65535. |
| Usuario | Sí | Nombre de usuario con el que se iniciará la sesión SSH. Máximo 20 caracteres. |
| Contraseña | No | Contraseña del usuario SSH. Si se omite, la aplicación la solicitará en cada conexión. Máximo 20 caracteres. |

> **Nota de seguridad:** la contraseña se almacena cifrada mediante el sistema Android Keystore, por lo que no es accesible por otras aplicaciones del dispositivo.

### Gestión de máquinas virtuales

Al acceder a un anfitrión, se muestra la lista de sus máquinas virtuales con el nombre y el estado de cada una. Cada tarjeta incluye una barra de acciones con las siguientes opciones:

- **Encendido / Apagado:** enciende la máquina si está apagada, o la apaga si está en ejecución. Si el apagado normal no concluye en el tiempo previsto, la aplicación ejecuta un apagado forzado de forma automática.
- **Reinicio:** reinicia la máquina virtual. Solo disponible para máquinas en estado `running`. El proceso realiza un apagado seguido de un encendido, verificando cada transición de estado.
- **Pausa / Reanudar:** pausa la ejecución de la máquina o la reanuda si ya estaba pausada. El icono cambia según el estado actual.
- **Hibernar / Restaurar:** guarda el estado completo de la máquina en disco y la detiene, o la restaura desde ese punto guardado. El icono cambia según el estado actual.
- **Crear instantánea:** abre un diálogo para introducir el nombre de la instantánea y crearla.
- **Ver instantáneas:** navega a la pantalla de gestión de instantáneas de esa máquina.

### Gestión de instantáneas

La pantalla de instantáneas muestra una lista con el nombre, la fecha y hora de creación, y el estado de la máquina en el momento en que se tomó cada instantánea. Desde aquí es posible **revertir** la máquina a una instantánea anterior o **eliminarla**.

### Ajustes

Desde la sección de ajustes es posible cambiar el idioma de la aplicación entre **español** e **inglés**.