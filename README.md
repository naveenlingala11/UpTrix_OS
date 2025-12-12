
# **UpTrix OS – Enterprise HROS/ERP Platform**

A scalable, modular Human Resource & Operations Management System built using **Angular**, **Spring Boot**, and **MySQL**, designed for enterprises up to **10,000+ employees**. UpTrix OS unifies HR, Operations, Projects, CRM, and Employee Self-Service into a single, extensible platform.

---

## ⭐ **Features Overview**

### **🔐 Authentication & Security**

* JWT-based authentication
* Role-based access control (Admin, HR, Manager, Employee)
* WebSocket-secured channels for real-time notifications
* Modular interceptor-based authentication flow

---

### **👥 Employee & Organization Management**

* Employee CRUD with role assignment
* Department & designation management
* Shift configuration & employee–shift mapping
* Attendance tracking
* Leave requests with approval hierarchy
* Employee self-service panel (“My Space”)

---

### **📅 Attendance, Shifts & Leave Management**

* Flexible shift creation (fixed, rotational, custom scheduling)
* Auto-detection of shift conflicts
* Attendance logs (manual entry + biometric integration ready)
* Leave workflow: apply → notify → approve/reject
* Notification events for all actions

---

### **🔔 Notifications Module**

* Real-time push notifications using WebSockets
* Polling fallback for environments without sockets
* Triggered by:

  * Leave requests
  * Attendance updates
  * HR actions
  * Shift changes

---

### **📊 Dashboards**

* Role-specific dashboards:

  * **Admin Dashboard** → Organization overview
  * **HR Dashboard** → Workforce insights
  * **Manager Dashboard** → Team KPIs
  * **Employee Dashboard** → Personal activity summary

---

### **⚙️ Settings Module**

* Master data configuration
* System preferences
* Notification settings

---

### **🚀 Architecture Overview**

## **Frontend – Angular**

```
/frontend
 ├── src/app
 │    ├── auth
 │    ├── employees
 │    ├── departments
 │    ├── shifts
 │    ├── attendance
 │    ├── leaves
 │    ├── notifications
 │    ├── dashboard
 │    ├── settings
 │    └── my-space
 └── ...
```

### **Tech Highlights**

* Angular 17
* Standalone components
* Signals for state management
* Interceptor-based API auth
* Reusable UI components

---

## **Backend – Spring Boot**

```
/backend
 ├── controllers
 ├── services
 ├── repositories
 ├── entities
 ├── dto
 ├── websocket
 ├── security
 └── config
```

### **Tech Highlights**

* Spring Boot 3+
* Spring Security (JWT)
* WebSocket + STOMP
* JPA / Hibernate
* Custom exception handling
* Highly modular service → repository pattern

---

## 🧱 **Database – MySQL**

Key Tables:

* `users`, `roles`, `user_roles`
* `employees`, `departments`, `designations`
* `attendance_logs`
* `leaves`, `leave_approvals`
* `shifts`, `employee_shift_mapping`
* `notifications`

Designed with **foreign key consistency, indexing**, and **horizontal scale readiness**.

---

## ⚡ **Scalability**

* Supports 10k+ concurrent users
* Polling + WebSocket hybrid notification strategy
* Optimized queries (indexes, pagination, projections)
* Modular backend → easy microservice transition
* Caching-ready service layer

---

## 🛠️ **Setup Instructions**

### **1️⃣ Clone the repo**

```bash
git clone https://github.com/<your-username>/uptrix-os.git
cd uptrix-os
```

---

## **2️⃣ Backend Setup (Spring Boot)**

### **Configure MySQL**

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/uptrix
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

### **Run backend**

```bash
./mvnw spring-boot:run
```

Backend runs at:

```
http://localhost:8080
```

---

## **3️⃣ Frontend Setup (Angular)**

Install dependencies:

```bash
npm install
```

Run development server:

```bash
ng serve --open
```

Frontend runs at:

```
http://localhost:4200
```

---

## 🔌 API Overview (Sample)

### **Auth**

```
POST /auth/login
POST /auth/register
```

### **Employees**

```
GET /employees
POST /employees
PUT /employees/{id}
DELETE /employees/{id}
```

### **Leaves**

```
POST /leaves/apply
GET /leaves/my
POST /leaves/approve/{id}
POST /leaves/reject/{id}
```

### **Notifications**

```
GET /notifications
POST /notifications/poll
/ws/notifications (WebSocket)
```

---

## 🧰 Technologies Used

### **Frontend**

* Angular 17
* RxJS, Signals
* TailwindCSS
* Angular Material

### **Backend**

* Spring Boot 3
* Spring Security
* Hibernate / JPA
* WebSockets / STOMP
* Lombok

### **Database**

* MySQL 8
* ERD optimized for scaling

---

## 📌 Roadmap (Next Enhancements)

* ⏳ Project management module
* 🧾 Payroll integration
* 📦 CRM integration
* 📈 Advanced analytics (BI dashboards)
* 🤖 AI-based workforce insights
* 📨 Email + SMS gateway
* 👥 Multi-tenancy (SaaS)
* 📱 Mobile app

---

## 🤝 Contributing

1. Fork this repository
2. Create a feature branch
3. Commit changes
4. Create a pull request

Please follow repository coding guidelines.

---

## 📄 License

MIT License (or your preferred license)

---
