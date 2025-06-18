# Group and Play 🎮

## Descripción
Group and Play es una aplicación web que permite a los usuarios crear y administrar grupos de juego.

## Instalación y Ejecución

### **1. Requisitos Previos**
Antes de comenzar, asegúrate de tener instalados los siguientes programas:

- **Java 17 o superior** → [Descargar JDK](https://adoptium.net/)
- **Maven** → [Descargar Maven](https://maven.apache.org/)
- **MySQL** (si usas base de datos persistente) → [Descargar MySQL](https://dev.mysql.com/downloads/)
- **Git** → [Descargar Git](https://git-scm.com/)

Puedes verificar las versiones instaladas con:
```sh
java -version
mvn -version
git --version
## Tecnologías utilizadas
- Spring Boot
- MySQL
- Maven
```

A continuación se explican las dos formas principales de instalar y ejecutar el proyecto. Se recomienda usar Docker por su simplicidad.

---

### 🔧 Opción 1: Arranque con Docker (recomendado)

1. **Inicia Docker Desktop**. Abre la aplicación y verifica que en la esquina inferior izquierda aparezca el mensaje `Engine running`.
2. **Clona el repositorio** desde tu editor de código favorito (por ejemplo, Visual Studio Code):

   ```bash
   git clone https://github.com/Adrcabmar/Group-And-Play.git
   ```

3. Dentro de la carpeta `frontend`, renombra el archivo `.env example.txt` a `.env`.
4. Desde la carpeta raíz del proyecto, ejecuta el siguiente comando para construir las imágenes:

   ```bash
   docker compose up --build
   ```

5. Docker construirá el backend, el frontend y levantará la base de datos. Este proceso puede tardar varios minutos. Una vez finalizado, verás los siguientes contenedores en Docker Desktop:
   - `frontend`
   - `backend`
   - `mariadb`
6. Accede a la web desde tu navegador en:  
   [http://localhost:3000/](http://localhost:3000/)
7. Para detener los servicios, ejecuta desde la carpeta raíz:

   ```bash
   docker compose down
   ```

> ⚠️ **Nota**: Asegúrate de que los puertos **3000**, **3036** y **8080** estén libres antes de ejecutar el proyecto.

---

### 🛠️ Opción 2: Arranque manual sin Docker

#### 1. Levantar la base de datos MariaDB

- Crea una base de datos llamada `groupandplay`.
- Crea un usuario `groupplay` con contraseña `group123` y dale permisos sobre la base de datos creada.

#### 2. Ejecutar el backend (Spring Boot)

1. Clona el repositorio:

   ```bash
   git clone https://github.com/Adrcabmar/Group-And-Play.git
   ```

2. Dentro de la carpeta `frontend`, renombra el archivo `.env example.txt` a `.env`.
3. Desde la raíz del proyecto, instala las dependencias del backend sin ejecutar los tests:

   ```bash
   mvn clean install -DskipTests
   ```

4. Inicia el backend sin tests:

   ```bash
   mvn spring-boot:run -DskipTests
   ```

5. El backend estará disponible en [http://localhost:8080/](http://localhost:8080/).

#### 3. Ejecutar el frontend (React + Vite)

1. Accede a la carpeta del frontend:

   ```bash
   cd frontend
   ```

2. Instala las dependencias:

   ```bash
   npm install
   ```

3. Inicia el servidor de desarrollo:

   ```bash
   npm run dev
   ```

4. El frontend estará disponible en:  
   [http://localhost:5173/](http://localhost:5173/)
