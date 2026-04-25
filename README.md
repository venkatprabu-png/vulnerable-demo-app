# 🔓 vulnerable-demo-app

> **⚠️ WARNING: This application is intentionally insecure. It contains known vulnerable
> dependencies. DO NOT deploy to production or any internet-facing environment.**

A medium-sized Spring Boot + Python demo application purposefully using packages with
**real CVEs** (all from 2021–2023) to demonstrate vulnerability scanning, dependency
analysis, and remediation workflows.

All vulnerabilities are fixed by **version bumps only** — no code changes required.

---

## 📋 At a Glance

| Layer | Language | Vulnerable Packages | CVEs |
|-------|----------|---------------------|------|
| API Server | Java (Maven) | 8 direct + transitive | 14 CVEs |
| Tooling/Scripts | Python (PyPI) | 11 packages | 11 CVEs |

**Severity breakdown:**

| Severity | Count |
|----------|-------|
| 🔴 Critical | 5 |
| 🟠 High | 14 |
| 🟡 Medium | 6 |

---

## 🗂️ Project Structure

```
vulnerable-demo-app/
├── pom.xml                          ← Maven build (vulnerable Java deps)
├── requirements.txt                 ← PyPI deps (vulnerable Python packages)
├── requirements-fixed.txt           ← Fixed PyPI deps (demo remediation)
│
├── src/
│   └── main/
│       ├── java/com/demo/
│       │   ├── DemoApplication.java
│       │   ├── controller/
│       │   │   ├── ProductController.java
│       │   │   ├── UserController.java
│       │   │   └── HealthController.java
│       │   ├── service/
│       │   │   ├── ProductService.java    ← commons-io CVE-2021-29425
│       │   │   └── UserService.java
│       │   ├── model/
│       │   │   ├── Product.java
│       │   │   └── User.java
│       │   └── config/
│       │       └── SecurityConfig.java    ← Spring Security CVE-2023-34062
│       └── resources/
│           └── application.yml           ← SnakeYAML / Tomcat config
│
├── scripts/
│   ├── api_client.py                ← requests + PyJWT CVEs
│   └── report_server.py             ← Flask + mistune CVEs
│
└── docs/
    └── CVE-REPORT.md                ← Full CVE catalogue with fix versions
```

---

## 🚀 Running the App

### Prerequisites
- Java 11+
- Maven 3.8+
- Python 3.8+

### Start the Spring Boot API

```bash
mvn spring-boot:run
# API available at http://localhost:8080
# Default credentials: admin / admin123
```

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/health` | Health check (no auth) |
| GET | `/api/products` | List all products |
| POST | `/api/products` | Create product |
| GET | `/api/products/{id}` | Get product by ID |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |
| GET | `/api/products/search?query=X` | Search products |
| GET | `/api/users` | List users |
| GET | `/actuator/health` | Actuator health |

### Start the Python Report Server

```bash
pip install -r requirements.txt
python scripts/report_server.py
# Available at http://localhost:5000
```

### Seed Data via Python Client

```bash
python scripts/api_client.py seed
python scripts/api_client.py list
python scripts/api_client.py health
python scripts/api_client.py token --username admin
```

---

## 🔍 Scanning for Vulnerabilities

### Maven — OWASP Dependency Check

```bash
mvn dependency-check:check
open target/dependency-check-report/dependency-check-report.html
```

### Python — Safety

```bash
pip install safety
safety check -r requirements.txt
```

### Python — pip-audit

```bash
pip install pip-audit
pip-audit -r requirements.txt
```

---

## 🔧 Remediation Demo

### Maven fix — bump versions in `pom.xml`

```xml
<!-- Change these property values: -->
<log4j.version>2.17.1</log4j.version>           <!-- was 2.14.1 -->
<spring.boot.version>2.7.17</spring.boot.version> <!-- was 2.6.3  -->
<jackson.version>2.14.3</jackson.version>         <!-- was 2.13.2 -->
<netty.version>4.1.100.Final</netty.version>      <!-- was 4.1.65.Final -->
<snakeyaml.version>2.0</snakeyaml.version>        <!-- was 1.30   -->
<commons-io.version>2.11.0</commons-io.version>   <!-- was 2.6    -->
<tomcat.version>9.0.81</tomcat.version>           <!-- was 9.0.60 -->
<spring.security.version>5.8.8</spring.security.version> <!-- was 5.6.2 -->
```

Then rebuild and re-scan:
```bash
mvn clean package
mvn dependency-check:check
```

### Python fix — use `requirements-fixed.txt`

```bash
pip install -r requirements-fixed.txt
safety check -r requirements-fixed.txt
# → 0 vulnerabilities found
```

---

## 📖 CVE Details

See **[docs/CVE-REPORT.md](docs/CVE-REPORT.md)** for the full table including CVSS
scores, descriptions, transitive dependency paths, and fix versions.

### Top Highlights

| CVE | Package | Severity | Type |
|-----|---------|----------|------|
| CVE-2021-44228 (Log4Shell) | log4j-core 2.14.1 | 🔴 CRITICAL 10.0 | RCE via JNDI lookup |
| CVE-2022-22965 (Spring4Shell) | spring-webmvc 5.3.15 | 🔴 CRITICAL 9.8 | RCE via data binding |
| CVE-2022-1471 | snakeyaml 1.30 | 🔴 CRITICAL 9.8 | RCE via YAML deserialisation |
| CVE-2023-34034 | spring-security 5.6.2 | 🟠 HIGH 9.1 | Auth bypass |
| CVE-2022-42003 | jackson-databind 2.13.2 | 🟠 HIGH 7.5 | DoS |
| CVE-2023-44487 | tomcat-embed 9.0.60 | 🟠 HIGH 7.5 | HTTP/2 Rapid Reset DoS |
| CVE-2023-30861 | Flask 2.2.5 | 🟠 HIGH 7.5 | Cookie exposure |
| CVE-2022-29217 | PyJWT 2.3.0 | 🟠 HIGH 7.5 | Algorithm confusion |
| CVE-2021-29425 | commons-io 2.6 | 🟡 MEDIUM 4.8 | Path traversal |

---

## 🏷️ Tags

`security-demo` `cve` `vulnerable-by-design` `log4shell` `spring4shell`
`dependency-scanning` `owasp` `devsecops` `java` `python` `maven` `pypi`
