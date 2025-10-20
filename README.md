# Video Platform v5 - HLS + JWT Signed Manifests (Automated HLS on upload)

## What's included
- Spring Boot app (Java 17, Spring Boot 3.1.6)
- H2 in-memory DB for users and metadata
- JWT service to sign HLS manifest URLs
- Automatic HLS generation via `ffmpeg` when a video is uploaded (VideoService invokes ffmpeg)
- Protected HLS manifest and segments served by HlsController with token verification
- Admin UI: upload videos, create users, generate signed HLS URLs
- Video page: stream MP4 inline, test HLS player input, download link only for roles DOWNLOAD or ADMIN

## Requirements
- Java 17, Maven
- ffmpeg installed and available on PATH (`ffmpeg` command)
  - On macOS: `brew install ffmpeg`
  - On Linux: use your package manager
- Enough disk space for uploaded videos and generated HLS segments

## Run locally
1. unzip and cd into project
2. Build: `mvn clean install`
3. Run: `java -jar target/video-platform-0.0.1-SNAPSHOT.jar`
4. Open: `http://localhost:8080/login`
   - default admin: `admin / adminpass` (auto-created)

## How automated HLS works
- When admin uploads an MP4, the server stores it under `videos/<storedName>.mp4`
- After saving metadata, the server runs `ffmpeg` to create HLS in `videos/hls/<storedNameWithoutExt>/playlist.m3u8`
- Admin then generates a signed HLS URL (`/hls/<videoId>/playlist.m3u8?token=...`) and can paste into the HLS test player on Videos page

## Security notes
- Replace `app.jwt.secret` in `application.properties` with a strong random secret before production
- Use HTTPS in production
- Token expiry is controlled by `app.jwt.expiration-seconds` (default 300s)
admin/adminpass
mvn clean install 
mvn spring-boot:run/java -jar target/java -jar target/video-platform-0.0.1-SNAPSHOT.jar


lsof -i :8080 
kill -9 processID


