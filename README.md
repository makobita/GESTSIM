# GESTSIM – URANO Simulador (Hully Gully) para Android

Simulador 3D de la atracción URANO con pupitre de mando, configuración y actualización automática desde GitHub.

## 1. Subirlo a GitHub (repositorio `makobitta/GESTSIM`)

Sube **todo** el contenido de esta carpeta al repositorio, incluidas las carpetas ocultas `.github` y la carpeta `keystore`.

> El repositorio tiene que ser **público** para que la app pueda leer las actualizaciones sin contraseña.

## 2. Compilador automático

En cuanto subes algo a la rama `main` (o `master`), GitHub:

1. Compila el APK (pestaña **Actions** → *Compilar y publicar APK*, unos 3–5 min).
2. Le pone versión nueva: `v1.0.1`, `v1.0.2`, `v1.0.3`… (número de compilación).
3. Lo publica en **Releases** como `GESTSIM.apk`, con el mensaje del commit como notas.

También puedes lanzarlo a mano: **Actions → Compilar y publicar APK → Run workflow**.

Si la primera vez falla con un error de permisos: **Settings → Actions → General → Workflow permissions → "Read and write permissions"** y guarda.

## 3. Instalar la primera vez

Entra en **Releases** del repositorio, descarga `GESTSIM.apk` en el móvil y ábrelo (acepta "instalar apps de origen desconocido").

Si tenías instalada una versión anterior hecha con otro paquete, **desinstálala una vez** (la firma ha cambiado). A partir de aquí todas se actualizan encima.

## 4. Actualizar desde la app

Botón **⚙** (arriba en el pupitre) → **ACTUALIZAR**:

- **Buscar actualización**: mira la última Release de GitHub.
- Si hay una más nueva: **Descargar e instalar** → la descarga y abre el instalador de Android.
- La primera vez Android pedirá permitir "instalar apps desconocidas" para GESTSIM: actívalo y vuelve, se instala sola.
- **Buscar al abrir**: si está en ON, al abrir la app avisa (punto rojo en ⚙) cuando hay versión nueva.

En el mismo menú ⚙ se activan o desactivan la **Noria**, el **Barco vikingo**, **La Cárcel** y el **Entorno** (parking, pared y parque). Se recuerda al cerrar la app.

## 5. Hacer cambios en el simulador

Todo el simulador está en `app/src/main/assets/index.html`. Cámbialo, súbelo, y el compilador saca la versión nueva; las apps instaladas la verán en ⚙ → Actualizar.

## Firma (importante)

Las actualizaciones sólo se instalan encima si **todas las versiones van firmadas con la misma clave**. Por eso va incluida `keystore/gestsim.jks` (contraseña `gestsim2026`, alias `gestsim`). **No la borres ni la cambies.**

Como el repositorio es público, cualquiera podría usar esa clave. Si quieres más seguridad, guárdala como *Secrets* (Settings → Secrets and variables → Actions):

- `KEYSTORE_BASE64` → contenido de `base64 keystore/gestsim.jks`
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

y después borra la carpeta `keystore` del repositorio. Usa **la misma** clave, o las apps instaladas dejarán de actualizarse.

## Android Studio (opcional)

Descarga `https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js` en `app/src/main/assets/three.min.js` y haz **Build → Build APK(s)**.
