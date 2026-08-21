# EventX — Event Booking & Ticketing Platform

A production-quality full-stack event booking platform built with **Spring Boot 3.5**, **React 18**, **MySQL**, **Redis**, and **Razorpay**. 

## 🚀 Features

- **User Authentication**: Secure JWT-based auth with access and refresh tokens.
- **Role-based Access Control**: Separate flows for Users and Admins.
- **Event Discovery**: Browse, search, and filter upcoming events.
- **Interactive Seat Selection**: Choose seats from different venue categories (VIP, Premium, Regular).
- **Concurrency Handling**: Redis TTL-based distributed locking to prevent double bookings during seat selection.
- **Payment Integration**: End-to-end secure Razorpay payment flow (sandbox).
- **Idempotent Payments**: Protection against duplicate payment processing and redundant webhooks.
- **Digital Tickets**: Unique QR code generation for verified access.
- **Admin Dashboard**: Comprehensive analytics, revenue tracking, and management of events, venues, bookings, and users.
- **Responsive UI**: Premium, dark-mode-first glassmorphism design.

---

## 🏗️ Architecture & Tech Stack

### Frontend
- **React 18** (Vite)
- **React Router v6**
- **Axios** (with interceptors)
- **Recharts** (Analytics)
- **React Toastify** (Notifications)
- **Vanilla CSS** (Custom Design System, CSS Variables)

### Backend
- **Java 17** (LTS)
- **Spring Boot 3.5.16**
- **Spring Security + JWT**
- **Spring Data JPA / Hibernate**
- **MySQL 8** (Primary Data Store)
- **Redis 7** (Caching & Distributed Locks)
- **Razorpay Java SDK**
- **ZXing** (QR Codes)
- **Springdoc OpenAPI** (Swagger)

---

## 💡 Backend Engineering Highlights (Interview Guide)

Be prepared to discuss these key technical decisions in interviews:

### 1. Redis Distributed Locking (Preventing Double Booking)
**Problem:** Two users try to select the same seat simultaneously. If both hit the DB at the exact same millisecond, they might both get it.
**Solution:** We use Redis to implement a temporary distributed lock. When a user selects seats, we attempt to acquire a lock in Redis (`SETNX` / `setIfAbsent`) with a TTL (e.g., 5 minutes). 
- If the lock is acquired, the user has 5 minutes to complete the payment.
- If they don't pay in time, the TTL expires automatically, making the seat available again.
- If payment is successful, the lock is removed and the booking becomes permanently confirmed in the MySQL database.

### 2. Idempotent Payment Processing
**Problem:** What if the frontend sends the "Payment Success" callback multiple times? Or what if Razorpay fires the same webhook twice?
**Solution:** The database uses a `UNIQUE` constraint on `razorpay_payment_id`. Before processing any payment confirmation, the backend checks if a payment with that ID already exists and has a `SUCCESS` status. If it does, the backend safely returns the existing success response without attempting to re-confirm the booking, generate duplicate tickets, or double-count revenue.

### 3. Razorpay Signature Verification
**Problem:** A malicious user alters the frontend JavaScript to send a fake "success" payload with an amount of ₹0 to the backend.
**Solution:** We **never** trust the frontend. The backend recalculates the expected price from the database. When the frontend sends the success callback, the backend cryptographically verifies the `razorpay_signature` using the Razorpay API Secret (which is never exposed to the frontend). We use HMAC-SHA256. If the signature doesn't match, the payment is rejected.

### 4. JWT Authentication with Refresh Tokens
**Problem:** Keeping users logged in securely without exposing long-lived access tokens.
**Solution:** 
- **Access Tokens** are short-lived (15 minutes). If stolen, they quickly become useless.
- **Refresh Tokens** are long-lived (7 days) and stored in the database. When the Access Token expires, the frontend Axios interceptor automatically uses the Refresh Token to request a new Access Token seamlessly.
- **Logout** invalidates the Refresh Token in the database.

---

## 🛠️ Local Setup Instructions

### Easiest way to run (no accounts, MySQL, Redis, or Razorpay needed)

Install **Java 17+** and **Node.js 18+** once. Then open two terminals:

```powershell
cd eventx-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

```powershell
cd eventx-frontend
npm install
npm run dev
```

Open `http://localhost:5173`, create an account, select seats, and use **Complete Demo Booking** at checkout. The `local` profile uses an embedded H2 database, so no database setup is required. Data resets whenever the backend is stopped.

### Full Razorpay / production setup

- Java 17+
- Node.js 18+
- MySQL 8+
- Redis Server
- Maven

### 1. Environment Variables
Copy `.env.example` to `.env` in the root directory and update the variables:
```bash
cp .env.example .env
```
Ensure you add your Razorpay Test API Keys from the [Razorpay Dashboard](https://dashboard.razorpay.com).

### 2. Start Services (MySQL & Redis)
If you have Docker, you can quickly spin up the databases:
```bash
docker compose up -d mysql redis
```

### 3. Start Backend
```bash
cd eventx-backend
./mvnw spring-boot:run
```
The backend will run on `http://localhost:8080`.
**Swagger API Docs:** `http://localhost:8080/swagger-ui/index.html`
*Note: The database schema and initial seed data will be created automatically on the first run.*

### 4. Start Frontend
```bash
cd eventx-frontend
npm install
npm run dev
```
The frontend will run on `http://localhost:5173`.

---

## Docker Deployment

This machine needs **Docker Desktop**. Then from the project root:

```powershell
docker compose up --build
```

Open **http://localhost:3000**. Nginx serves the UI and proxies `/api` to Spring Boot. MySQL and Redis run in the same compose stack.

Razorpay webhooks need a public URL. After you put the stack on a host, set the webhook to `https://<your-domain>/api/payments/webhook`.

---

## 🧪 Testing

The backend includes a comprehensive test suite (Unit and Integration tests).
```bash
cd eventx-backend
./mvnw test
```

## 📚 API Endpoints Summary

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/events` - Get published events (paginated/filtered)
- `GET /api/events/{id}/seats` - Get seat map and availability
- `POST /api/bookings/lock-seats` - Lock selected seats (creates pending booking)
- `POST /api/payments/create-order` - Create Razorpay order
- `POST /api/payments/verify` - Verify frontend payment callback
- `POST /api/payments/webhook` - Razorpay webhook handler
- `GET /api/tickets/{id}` - Get digital ticket with QR code
- `GET /api/admin/analytics/overview` - Admin dashboard stats

---

## 👤 Default Seed Accounts
- **Admin**: `admin@eventx.com` / `Admin@123`
- **User**: `john@example.com` / `User@123`
