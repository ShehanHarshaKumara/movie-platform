# Movie Rental Platform
<img width="1857" height="933" alt="Screenshot 2026-04-01 162715" src="https://github.com/user-attachments/assets/6722ad8b-2ba1-4b40-a07f-2e08d9b399bb" />
<img width="1911" height="928" alt="Screenshot 2026-04-01 162739" src="https://github.com/user-attachments/assets/419fb398-862b-4489-b3a2-fedd0cd61765" />
<img width="1902" height="938" alt="Screenshot 2026-04-01 162800" src="https://github.com/user-attachments/assets/ec66509f-c626-4e56-81c7-c261d5b9db1d" />
<img width="1912" height="927" alt="Screenshot 2026-04-01 162829" src="https://github.com/user-attachments/assets/03e070c3-910c-4b7d-a79e-1f07e15c5b73" />
<img width="1907" height="935" alt="Screenshot 2026-04-01 162902" src="https://github.com/user-attachments/assets/943b3008-452d-4c8a-a17a-7571886c618f" />
<img width="1915" height="942" alt="Screenshot 2026-04-01 162933" src="https://github.com/user-attachments/assets/7b638afe-b441-42d4-9990-ca6fbdc66223" />
<img width="1902" height="943" alt="Screenshot 2026-04-01 162949" src="https://github.com/user-attachments/assets/231acfcf-25e4-47ba-8fae-7269b86665d5" />
<img width="1908" height="933" alt="Screenshot 2026-04-01 163014" src="https://github.com/user-attachments/assets/5b25993b-8c95-4fd2-a0c1-979b51938cea" />
<img width="1903" height="940" alt="Screenshot 2026-04-01 163035" src="https://github.com/user-attachments/assets/fbbe8098-5109-4530-830e-7ac8faf19286" />
<img width="1897" height="947" alt="Screenshot 2026-04-01 163106" src="https://github.com/user-attachments/assets/a123d766-5637-4a11-a4d8-0539cda92b42" />
<img width="1902" height="922" alt="Screenshot 2026-04-01 163126" src="https://github.com/user-attachments/assets/4d4fe2e2-3cb5-46f6-bd4f-8ea315380590" />
<img width="1897" height="936" alt="Screenshot 2026-04-01 163150" src="https://github.com/user-attachments/assets/c267b3e0-6ea0-4dac-a3f4-c945816cbbd5" />
<img width="1902" height="911" alt="Screenshot 2026-04-01 163213" src="https://github.com/user-attachments/assets/20556348-31ed-4b86-b9bf-189aae22edd7" />
<img width="1915" height="938" alt="Screenshot 2026-04-01 163233" src="https://github.com/user-attachments/assets/11ef3557-40a2-4be8-b7fa-48c2db12a310" />
<img width="1905" height="946" alt="Screenshot 2026-04-01 163252" src="https://github.com/user-attachments/assets/b44bf22c-8fbf-4aac-9d0c-aeaf2a231c3f" />
<img width="1901" height="941" alt="Screenshot 2026-04-01 163327" src="https://github.com/user-attachments/assets/5cb59fdf-a935-4ca8-a8a4-4cab8c8b517e" />
<img width="1848" height="932" alt="Screenshot 2026-04-01 163343" src="https://github.com/user-attachments/assets/91ce9cf2-a9d1-42fd-ae62-1048761a39f1" />
<img width="1867" height="935" alt="Screenshot 2026-04-01 163404" src="https://github.com/user-attachments/assets/eb10b5f1-f6aa-4be6-b484-cc12d804a03a" />
<img width="1850" height="931" alt="Screenshot 2026-04-01 163429" src="https://github.com/user-attachments/assets/11050616-27b7-4041-889f-96bb2fb9b6d6" />
<img width="1853" height="938" alt="Screenshot 2026-04-01 163450" src="https://github.com/user-attachments/assets/9fe5f06b-3db5-47fc-a8ae-e65f0b01294c" />



This project is now rebuilt 
as a working Spring Boot application with file-based storage and optional MySQL database setup.

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

### Render

This repo now includes a `render.yaml` Blueprint for deploying the app on Render with Docker.

Important:

- The Blueprint uses the `starter` plan because this app needs a persistent disk for `data/` and uploaded files.
- The disk is mounted at `/app/data`, which matches the app's `DATA_ROOT` and `APP_UPLOAD_ROOT` settings.
- The health check uses `/movies` because `/` redirects there.

To deploy:

1. Push this repository to GitHub.
2. In Render, create a new Blueprint from the repo.
3. Confirm the `Home` branch and `movie-platform` service.
4. Apply the Blueprint and wait for the first deploy to finish.
