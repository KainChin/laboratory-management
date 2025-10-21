# 🧪 Laboratory Management System – Test Order Service

## 📘 Overview
The **Test Order Service** is one of the core microservices within the **Laboratory Management System (LMS)**.  
It is responsible for managing all operations related to laboratory test orders, including creation, update, synchronization, result review, and reporting.

This service integrates with other components such as **Patient Service**, **Instrument Service**, **Monitoring Service**, and **Warehouse Service** to ensure seamless data flow between laboratory instruments, test records, and patient information.

---

## ⚙️ Key Features

### 🧾 Test Order Management
- Create, update, delete, and retrieve patient test orders.  
- Support searching, sorting, and filtering by patient, date, and status.  
- Maintain full order lifecycle: `Pending → Processing → Completed → Reviewed`.

### 🧪 Test Result Management
- Receive and process HL7 messages from laboratory instruments.  
- Transform raw test data into structured, readable results.  
- Highlight abnormal or critical values.  
- Perform **AI Auto Review** for automated result evaluation.  
- Synchronize data with Monitoring and Warehouse services.

### 💬 Comment & Review System
- Add, edit, and delete comments linked to test orders.  
- Record reviewer information and timestamp for traceability.  
- Support both manual and AI-assisted review modes.

### 📊 Reporting & Export
- Generate PDF reports for completed test results.  
- Export patient test orders and results to Excel format.  
- Enable printing of formatted test reports.

---

## 🧩 System Architecture

**Technology Stack**
| Component | Technology |
|------------|-------------|
| Programming Language | Java 17 |
| Framework | Spring Boot / Spring MVC |
| Database | PostgreSQL or MySQL |
| Messaging Queue | RabbitMQ or Kafka (for HL7 message bus) |
| Authentication | JWT-based (via IAM Service) |
| Build Tool | Maven |
| Version Control | GitLab |

**Service Interactions**
```text
[Patient Service] <--> [Test Order Service] <--> [Instrument Service]
                                   |
                             [Monitoring Service]
                                   |
                             [Warehouse Service]
