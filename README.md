# SentinelASM

Enterprise-grade Attack Surface Management platform for authorized asset discovery, reconnaissance, vulnerability triage, and executive reporting.

SentinelASM is structured like a commercial SaaS product: Spring Boot REST API, JWT authentication, PostgreSQL persistence, async scan orchestration, React TypeScript dashboard, charting, notifications, exports, and a scanner adapter for your `SubMerge.sh` reconnaissance engine plus HTTPX, Nmap, WhatWeb, and Nuclei.

## Architecture

```text
frontend/  React + TypeScript + Tailwind dashboard
backend/   Spring Boot API, auth, persistence, async scan pipeline
infra/     Docker Compose and deployment assets
docs/      Architecture, scanner contract, resume talking points
tools/     Place SubMerge.sh and optional local wrappers here
```

## Quick Start

```powershell
Copy-Item .env.example .env
docker compose -f infra/docker-compose.yml up --build
```

Local development:

```powershell
cd backend
mvn spring-boot:run

cd ..\frontend
npm.cmd install
npm.cmd run dev
```

Frontend: `http://localhost:5173`

Backend: `http://localhost:8080`

Swagger/OpenAPI: `http://localhost:8080/swagger-ui/index.html`

## Scanner Modes

`ASM_SCAN_MODE=mock` is the default. It produces deterministic lab data so the platform can be demoed safely. `demo` is not a real-tool mode; use `tools` to execute SubMerge.

`ASM_SCAN_MODE=tools` executes configured binaries. Use this only in a lab or against assets you own or are explicitly authorized to assess.

Expected pipeline:

```text
SubMerge -> Asset Import -> Nmap -> WhatWeb -> Header/Screenshot/WHOIS/SSL/Directory/API modules -> Nuclei -> Risk Engine -> Reports -> Notifications
```

SubMerge is treated as a black-box reconnaissance engine. The backend does not parse terminal colors or banners. It waits for `recon-engine/submerge.sh` to finish and reads `output/<domain>/final/alive.txt`, `output/<domain>/final/resolved.txt`, and `output/<domain>/run.log`.

For setup details, see [docs/SCANNER_CONTRACT.md](docs/SCANNER_CONTRACT.md).

## Security Scope

This project is for authorized security testing, asset inventory, and lab environments. It does not include stealth, evasion, exploitation, persistence, or unauthorized access features.
