# Smart Campus Operations Hub – Backend

**IT3030 PAF Assignment 2026 | Spring Boot REST API**

## Tech Stack
- Java 17 + Spring Boot 3.2
- Spring Security + OAuth2 (Google) + JWT
- Spring Data JPA + MySQL
- Lombok, Bean Validation

## Project Structure
```
src/main/java/com/smartcampus/
├── config/          SecurityConfig
├── controller/      REST controllers (5 modules)
├── dto/             Request & Response DTOs
├── entity/          JPA entities
├── enums/           Status, Role, Type enums
├── exception/       GlobalExceptionHandler + custom exceptions
├── repository/      JPA repositories
├── security/        JWT provider, filter, OAuth2 handler
└── service/         Service interfaces + implementations
```

## Modules
| Module | Controller | Description |
|--------|-----------|-------------|
| A | ResourceController | Facilities & Assets Catalogue |
| B | BookingController | Booking Management + conflict check |
| C | TicketController | Maintenance & Incident Ticketing |
| D | NotificationController | Notifications |
| E | AuthController + SecurityConfig | OAuth2 + JWT + RBAC |

## Quick Start

### 1. Prerequisites
- Java 17+, Maven 3.8+, MySQL 8+

### 2. Database
```sql
CREATE DATABASE smart_campus;
```

### 3. Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.security.oauth2.client.registration.google.client-id=YOUR_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_SECRET
app.jwt.secret=your-very-long-secret-key-here
```

### 4. Run
```bash
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### 5. Test
```bash
./mvnw test
```

## Key API Endpoints

### Auth (Module E)
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | /oauth2/authorize/google | Public |
| GET | /api/auth/me | Authenticated |
| POST | /api/auth/token/validate | Public |

### Resources (Module A)
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | /api/resources | Public |
| GET | /api/resources/{id} | Public |
| POST | /api/resources | ADMIN |
| PUT | /api/resources/{id} | ADMIN |
| DELETE | /api/resources/{id} | ADMIN |

### Bookings (Module B)
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/bookings | USER |
| GET | /api/bookings/my | USER |
| GET | /api/bookings | ADMIN |
| PATCH | /api/bookings/{id}/review | ADMIN |
| PATCH | /api/bookings/{id}/cancel | USER |

### Tickets (Module C)
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/tickets | USER |
| GET | /api/tickets/my | USER |
| GET | /api/tickets | ADMIN/TECH |
| GET | /api/tickets/{id} | Authenticated |
| PATCH | /api/tickets/{id}/status | ADMIN/TECH |
| POST | /api/tickets/{id}/attachments | USER |
| POST | /api/tickets/{id}/comments | Authenticated |
| PUT | /api/tickets/comments/{id} | Owner |
| DELETE | /api/tickets/comments/{id} | Owner/ADMIN |

### Notifications (Module D)
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | /api/notifications | USER |
| GET | /api/notifications/unread-count | USER |
| PATCH | /api/notifications/{id}/read | USER |
| PATCH | /api/notifications/read-all | USER |

### Admin (Module E)
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | /api/admin/users | ADMIN |
| PATCH | /api/admin/users/{id}/role | ADMIN |
| PATCH | /api/admin/users/{id}/toggle-active | ADMIN |

## Roles
- `USER` – Can create bookings and tickets, view own data
- `ADMIN` – Full access, can approve/reject, manage users
- `TECHNICIAN` – Can update ticket status, add resolution notes

## OAuth2 Flow
1. Frontend redirects to `GET /oauth2/authorize/google`
2. User signs in with Google
3. Backend issues JWT, redirects to `{frontend}/oauth2/redirect?token=JWT`
4. Frontend stores token and sends as `Authorization: Bearer <token>`
