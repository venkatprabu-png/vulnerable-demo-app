# 🎤 Live Demo Guide — vulnerable-demo-app

A step-by-step script for presenting this project as a **DevSecOps / SCA demo**.
Total demo time: ~15 minutes.

---

## Prerequisites

```bash
# Java 11+, Maven 3.8+, Python 3.8+
java -version && mvn -version && python3 --version

# Python tools
pip install safety pip-audit packaging pytest

# Clone the repo
git clone https://github.com/<your-org>/vulnerable-demo-app.git
cd vulnerable-demo-app
```

---

## Act 1 — Show the Project (2 min)

> "This is a small Spring Boot REST API with a Python tooling layer.
> It manages a simple product catalogue. Nothing special on the surface."

```bash
# Show the structure
ls -1
cat README.md | head -40
```

> "But look at what's pinned in the dependency files..."

```bash
# Point to the version properties block in pom.xml
grep -A5 "VULNERABLE" pom.xml | head -30

# And requirements.txt
cat requirements.txt
```

**Key talking points:**
- These look like ordinary, slightly old versions
- Nothing in the application code itself is wrong
- The vulnerabilities are entirely in the dependency manifest

---

## Act 2 — Scan the Vulnerable State (5 min)

### Maven scan

```bash
mvn dependency-check:check \
    -Ddependency-check.failBuildOnCVSS=7 \
    --batch-mode 2>&1 | tail -40
```

> "The build fails. Let's look at the report."

```bash
open target/dependency-check-report/dependency-check-report.html
# (or: python3 -m http.server 9000 and browse to localhost:9000/target/...)
```

**Show in the browser:**
- Log4Shell (CVSS 10.0) — explain JNDI RCE briefly
- Spring4Shell (CVSS 9.8) — explain data-binding RCE
- SnakeYAML (CVSS 9.8) — YAML constructor deserialization
- The transitive chain: `spring-boot-starter-web → snakeyaml 1.30`

### Python scan

```bash
safety check -r requirements.txt
```

```
pip-audit -r requirements.txt
```

**Show:** Flask, requests, PyJWT all flagged.

---

## Act 3 — Explain the Fix (2 min)

> "Every single CVE here is resolved by bumping a version number.
> No code changes. No refactoring. No application logic to touch."

```bash
diff pom.xml pom-fixed.xml
```

> "Eight property values in pom.xml. That's it."

```bash
diff requirements.txt requirements-fixed.txt
```

> "Eleven package versions in requirements.txt."

---

## Act 4 — Apply and Verify (4 min)

```bash
# Apply the fix
cp pom-fixed.xml pom.xml

# Rebuild
mvn clean verify --batch-mode -q
```

> "Tests still pass. The application behaviour is unchanged."

```bash
# Re-scan
mvn dependency-check:check \
    -Ddependency-check.failBuildOnCVSS=7 \
    --batch-mode 2>&1 | tail -10
```

> "Build passes now — zero HIGH or CRITICAL CVEs."

```bash
# Python
pip install -r requirements-fixed.txt -q
safety check -r requirements-fixed.txt
pip-audit -r requirements-fixed.txt
```

> "Zero vulnerabilities on the Python side too."

---

## Act 5 — CI/CD Integration (2 min)

```bash
cat .github/workflows/ci.yml
```

> "The pipeline has four jobs:"

| Job | What it does |
|-----|-------------|
| `build-java` | Compile + unit tests |
| `owasp-maven` | CVE scan, uploads HTML report as artifact |
| `scan-python` | `safety` + `pip-audit`, compares vulnerable vs fixed |
| `verify-remediation` | Patches pom.xml on-the-fly and proves the fixed scan passes |

> "If you merge a PR that re-introduces a vulnerable version, `owasp-maven`
> catches it immediately and the report is attached to the run."

---

## One-command Demo

```bash
# Runs all 5 acts automatically
chmod +x scripts/run-demo.sh
./scripts/run-demo.sh
```

---

## CVE Quick-Reference Card

| CVE | Package | CVSS | Type | Fix |
|-----|---------|------|------|-----|
| CVE-2021-44228 | log4j-core 2.14.1 | **10.0** | RCE (JNDI) | → 2.17.1 |
| CVE-2022-22965 | spring-webmvc 5.3.15 | **9.8** | RCE (data binding) | → spring-boot 2.7.17 |
| CVE-2022-1471 | snakeyaml 1.30 | **9.8** | RCE (YAML) | → 2.0 |
| CVE-2021-45046 | log4j-core 2.14.1 | 9.0 | RCE | → 2.17.1 |
| CVE-2023-34034 | spring-security 5.6.2 | 9.1 | Auth bypass | → 5.8.8 |
| CVE-2023-37276 | aiohttp 3.8.1 | 8.6 | HTTP smuggling | → 3.8.5 |
| CVE-2022-21699 | ipython 7.29.0 | 8.8 | Code exec | → 7.31.1 |
| CVE-2023-44487 | tomcat-embed 9.0.60 | 7.5 | DoS (HTTP/2) | → 9.0.81 |
| CVE-2022-42003 | jackson-databind 2.13.2 | 7.5 | DoS | → 2.14.0 |
| CVE-2023-30861 | Flask 2.2.5 | 7.5 | Cookie leak | → 2.3.2 |
| CVE-2022-29217 | PyJWT 2.3.0 | 7.5 | Alg confusion | → 2.4.0 |
| CVE-2021-37137 | netty-codec 4.1.65 | 7.5 | DoS | → 4.1.68 |
| CVE-2021-29425 | commons-io 2.6 | 4.8 | Path traversal | → 2.7 |
| CVE-2023-43804 | urllib3 1.26.14 | 5.9 | Cookie leak | → 1.26.17 |

---

*Tip: keep this file open on a second monitor during the live demo.*
