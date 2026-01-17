# ☕ Cafe Management Web Application
---

## 🧠 Overview

This web application supports cafe managers in managing the cafe, employees, ingredients, and sales processes more effectively. At the same time, it allows customers to view the menu, track order history and loyalty points, and book tables online with ease.

**Target users:** Cafe managers, staff, and customers.

---

## 🎯 Project Objectives

* Simplify cafe management processes through a centralized system.
* Improve customer experience with online services such as table booking and loyalty points.
* Apply web development knowledge in a **student group project (6 members)**.

---

## 🧩 Core Features

### 🔐 Authentication & User Management

* User registration and login
* Google OAuth2 integration
* Role-based Access Control (RBAC)
* Forgot password functionality
* User profile management

### 📅 Table Booking Management

* Online table booking
* Check table availability
* Filter tables by seating capacity
* Update and manage booking status

### 🧾 Sales & Order Management

* Create orders at the counter
* Order history management
* Loyalty points accumulation
* Reward redemption system

### 🍽️ Product & Menu Management

* CRUD operations for products and categories
* Inventory status management
* Smart search and product filtering

### 🎁 Promotions & Customer Feedback

* Voucher management (create, update, apply voucher codes)
* Customer feedback collection and tracking

### 📊 Reports & Administration

* Staff management
* Revenue statistics by time period
* Automatic sales report generation

---

## 🏗️ Tech Stack

### Frontend

* Thymeleaf
* Bootstrap
* CSS

### Backend

* Java Spring Boot
* Spring MVC

### Authentication & Security

* Spring Security
* OAuth2 (Google Login)

### Database & ORM

* SQL Server
* Spring Data JPA

### Testing

* Unit Testing

---

## ⚙️ Installation & Run

### Requirements

* Java JDK 8+
* SQL Server
* Maven
* IDE (IntelliJ IDEA / Eclipse / NetBeans)

### Steps

```bash
git clone <repo-url>
cd cafe-management-web-app
```

```bash
# configure database connection in application.properties
# then run the Spring Boot application
```

---

## 📂 Project Structure

```text
project-root/
├── src/main/java/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── security/
├── src/main/resources/
│   ├── templates/
│   ├── static/
│   └── application.properties
└── README.md
```

---

## 🧪 Testing

* Unit tests for core business logic
* Validation of authentication, booking, and order workflows

---

## 👥 Team

* Project developed by a **6-member student team**

---

## 📜 License

This project is intended **for educational purposes only**.

---

## 📧 Contact

* **Email:** [nhutlm.ce190737@gmail.com](mailto:nhutlm.ce190737@gmail.com)
