# Movie Rental Platform

This project is now rebuilt as a working Spring Boot application with file-based storage and optional MySQL database setup.

## Features

- User management: register, login, logout, profile update, password validation
- Movie management: add, update, delete, search, and view movies
- Rental management: rent, return, track rentals, calculate rental fees and late fees
- Review management: add, edit, delete reviews and sort top-rated movies with Bubble Sort
- Recently watched history: stack-based LIFO watch history
- Admin tools: manage users, rentals, reviews, and reports

## Storage

All data is stored in the local `data/` folder:

- `users.txt`
- `movies.txt`
- `rentals.txt`
- `reviews.txt`
- `recently-watched.txt`

Legacy `data/user.txt` is migrated automatically into `data/users.txt`.

## MySQL Database

The project now also includes optional MySQL support.

- SQL schema file: `database/movie_rental_platform_db.sql`
- Spring profile: `mysql`
- When the `mysql` profile is enabled, the app creates the tables automatically and can seed MySQL from the current `data/` files on first run.

Run with MySQL profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

Or with environment variables:

```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD=""
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

The JDBC URL uses `createDatabaseIfNotExist=true`, so the `movie_rental_platform_db` database is created automatically when MySQL is reachable.

## Default Admin Account

- Username: `admin`
- Password: `Admin@123`

## Run

Use the Maven wrapper from this folder:

```powershell
.\mvnw.cmd spring-boot:run
```

Build and test:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

If port `8080` is already in use, you can run on another port:

```powershell
$env:SERVER_PORT="8081"
.\mvnw.cmd spring-boot:run
```

## Deployment Notes

If you deploy this project to Vercel and see `404: NOT_FOUND`, that response is coming from Vercel rather than from Spring Boot. This project is a server-rendered Spring Boot application with file-backed storage, so it should be deployed on a platform that can run a long-lived JVM process or a Docker container.

Good fits include Render, Railway, Fly.io, Azure App Service, AWS Elastic Beanstalk, or any VPS that can run Docker or Java.

The app now supports these deployment-friendly environment variables:

- `PORT` or `SERVER_PORT` for the HTTP port
- `DATA_ROOT` for movie, user, rental, and review storage
- `APP_UPLOAD_ROOT` for uploaded images (defaults to `DATA_ROOT/uploads`)

### Docker

Build the image:

```powershell
docker build -t movie-rental-platform .
```

Run the container:

```powershell
docker run --rm -p 8080:8080 -e PORT=8080 movie-rental-platform
```

To keep file-based data between restarts, mount a persistent volume or disk to `/app/data` on your host platform.
