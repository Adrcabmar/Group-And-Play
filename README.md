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
### **2. Clonar el Proyecto**
Abre una terminal y ejecuta:
git clone https://github.com/Adrcabmar/Group-And-Play.git

### **3. Configurar la Base de Datos**
Si usas MySQL, crea una base de datos con:

```sh
Copiar
Editar
```
Asegúrate de que application-mysql.properties tenga la configuración correcta:
```sh
spring.datasource.url=jdbc:mysql://localhost/groupandplay
spring.datasource.username=root
spring.datasource.password=tu_contraseña
```
### **4. Compilar y Ejecutar la Aplicación**
Ejecuta los siguientes comandos en la raíz del proyecto:
```sh
./mvnw clean package   # Compilar la aplicación
./mvnw spring-boot:run # Ejecutar la aplicación
```
### **5. Acceder a la Aplicación**
Una vez que la aplicación esté en ejecución, accede desde tu navegador a:

🔗 http://localhost:8080

## Tecnologías Utilizadas

Spring Boot 3
Maven
MySQL / H2
Thymeleaf (para el frontend)
Hibernate / JPA (para ORM)
