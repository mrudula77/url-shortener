# URL Shortener Service

REST API to shorten URLs built with Spring Boot, MySQL and Redis.

## Features
- Shorten long URLs using Base62 encoding
- Redis caching for fast redirects (~4ms vs ~40ms DB)
- URL expiry with 410 Gone response
- Click count analytics
- Duplicate URL detection
- Global exception handling

## Tech Stack
Java 21 · Spring Boot 3.5 · MySQL · Redis · Spring Data JPA · Lombok

## How to Run

1. Start Redis:
   docker run -d -p 6379:6379 --name redis redis

2. Create MySQL database:
   CREATE DATABASE urlshortener;

3. Copy example properties:
   cp src/main/resources/application.properties.example src/main/resources/application.properties

4. Update application.properties with your MySQL password

5. Run UrlShortenerApplication.java

6. Test APIs: http://localhost:8080/swagger-ui/index.html

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /shorten | Create short URL |
| GET | /{shortCode} | Redirect to original URL |
| GET | /analytics/{shortCode} | View click stats |

## Sample Request
POST /shorten
{
"originalUrl": "https://www.youtube.com/watch?v=example",
"expiryDays": "30"
}

Response:
{
"shortUrl": "http://localhost:8080/b"
}