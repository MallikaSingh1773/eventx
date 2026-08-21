# EventX

Event booking and ticketing platform: discover events, lock seats, pay with Razorpay, and receive a QR ticket.

Built as a full-stack product with a **React** client and a **Spring Boot** API, JWT auth, role-based access (attendee, organizer, admin), and webhook-aware payments.

---

## Architecture

```
Browser (React 18 + Vite)
        │  HTTPS / JSON
        ▼
Spring Boot 3.3 (Java 17)
  Spring Security + JWT
  Spring Data JPA
  Razorpay SDK · ZXing QR
        │
        ├── Local:     H2 file database  (profile `local`)
        ├── Docker:    MySQL 8 + Redis 7
        └── Render:    PostgreSQL 16 + Redis-compatible Key Value
```

| Layer | Stack |
| --- | --- |
| Client | React 18, Vite 5, React Router 6, Axios, Recharts, Lucide |
| API | Spring Boot 3.3.5, Spring Security, JJWT, Validation |
| Data | JPA / Hibernate · H2 (local) · MySQL (Docker) · PostgreSQL (Render) |
| Locks | Redis `SET NX` + TTL; in-memory fallback if Redis is down |
| Payments | Razorpay Checkout + HMAC verify + `payment.captured` webhook |
| Tickets | ZXing QR codes |
| Docs | Springdoc OpenAPI (`/swagger-ui.html`) |

---

## Features

- Register / login with access (15 min) and refresh (7 day) tokens  
- Roles: **Attendee**, **Organizer**, **Admin**  
- Search and filter published events  
- Seat map with a timed hold before checkout  
- Razorpay test payments; booking confirms from webhook or captured-status sync  
- Unique `razorpay_payment_id`; amount checked against the booking total  
- QR tickets after confirmation  
- Organizer studio: venues, events, publish / cancel  
- Admin analytics (revenue, bookings)

---

## Repository layout

```
eventx-backend/     Spring Boot API
eventx-frontend/    Vite React app
render.yaml         Render Blueprint (Java API + static site + Postgres + Redis)
docker-compose.yml  Local/prod-style MySQL + Redis + API + nginx UI
```

---

## Local development

**Requirements:** Java 17+, Maven, Node.js 18+.

### Quick start (H2, no MySQL)

```powershell
cd eventx-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

```powershell
cd eventx-frontend
npm install
npm run dev
```

- UI: [http://localhost:5173](http://localhost:5173)  
- API: [http://localhost:8080](http://localhost:8080)  
- Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The `local` profile uses a file H2 database under `eventx-backend/data/` (survives restarts). Redis is optional; seat locks fall back to process memory if Redis is not running.

Copy `.env.example` to `.env` and add Razorpay **test** keys for real Checkout. Without keys, order creation is rejected until they are set.

### Seed accounts

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@eventx.com` | `Admin@123` |
| Organizer | `organizer@eventx.com` | `Organizer@123` |
| Attendee | `john@example.com` | `User@123` |

---

## Docker

From the repo root (Docker Desktop required):

```powershell
docker compose up --build
```

App: [http://localhost:3000](http://localhost:3000). Nginx serves the UI and proxies `/api` to Spring Boot.

---

## Render

Blueprint file: `render.yaml`.

| Service | Purpose |
| --- | --- |
| `eventx-java-web` | Static frontend |
| `eventx-java-api` | Spring Boot (Docker) |
| `eventx-java-db` | PostgreSQL |
| `eventx-java-redis` | Key Value (Redis protocol) |

Typical URLs after a successful deploy:

- Site: `https://eventx-java-web.onrender.com`  
- API: `https://eventx-java-api.onrender.com`  
- Webhook: `https://eventx-java-api.onrender.com/api/payments/webhook`

Free Render allows **one** free Postgres and **one** free Key Value instance per account. The first Docker API build often takes 10–20 minutes. Free web services sleep; the first request after idle can take 30–60 seconds.

Do not attach this Blueprint to an existing Node/Express service named `eventx-api`. Use **Create all as new services**.

---

## Payments

1. Client creates a Razorpay order from a pending booking.  
2. Checkout returns order / payment ids; the API stores them after signature check.  
3. Booking is confirmed only when Razorpay reports **captured** (webhook `payment.captured`, or server-side fetch while the client polls status).  
4. Duplicate payment ids and ticket generation are idempotent.

Set the Razorpay webhook URL to the public `/api/payments/webhook` endpoint and use a dedicated webhook secret in production.

---

## Selected API routes

| Method | Path | Notes |
| --- | --- | --- |
| `POST` | `/api/auth/register` | `accountType`: `USER` or `ORGANIZER` |
| `POST` | `/api/auth/login` | JWT pair |
| `GET` | `/api/events` | Published events |
| `POST` | `/api/bookings/lock-seats` | Timed seat hold |
| `POST` | `/api/payments/create-order` | Razorpay order |
| `POST` | `/api/payments/verify` | Record Checkout ids (does not confirm alone) |
| `GET` | `/api/payments/booking/{id}` | Poll confirmation |
| `POST` | `/api/payments/webhook` | Razorpay (public) |
| `GET/POST` | `/api/organizer/**` | Organizer or admin |
| `GET` | `/api/admin/**` | Admin |

---

## Environment

See `.env.example`. Important variables:

- `JWT_SECRET`  
- `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` / `RAZORPAY_WEBHOOK_SECRET`  
- `VITE_API_URL` (frontend build; production should be the public API `…/api`)  
- `VITE_RAZORPAY_KEY_ID` (public key only)

Never commit `.env`.

---


