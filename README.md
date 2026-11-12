# AttackGrid

AttackGrid is an attack surface management platform for startups and small companies that don't have a security team. You add your company's domain, and AttackGrid finds everything exposed to the internet, scans it for problems, and gives you a security report you can actually read and act on.

## Why I built it

Most startups have no idea how much of their infrastructure is public. Old subdomains, forgotten staging servers, open ports, outdated software. Hiring a pentest firm is expensive, and enterprise ASM tools are priced for big companies. I wanted to build something simple: put in your domain, get a clear picture of your exposure and what to fix first.

## What it does

You enter a domain you own. AttackGrid then:

- Discovers subdomains and finds which ones are live
- Scans for open ports and running services
- Identifies the technologies each web asset uses
- Checks for known vulnerabilities
- Scores risk per asset and for the company overall
- Produces a report for both technical and non-technical readers

Under the hood:

- Scans run asynchronously through a worker queue (Redis Streams), so the API never blocks
- Work is spread across multiple workers that can scale horizontally
- Worker heartbeats are monitored, and jobs are retried if a worker crashes mid-scan
- Live scan progress is pushed to the dashboard over WebSockets
- Every scan is saved, so you can see what changed since the last one
- Multi-tenant: each company only sees its own data
- JWT authentication, reports and artifacts stored in MinIO, API docs through Swagger

## Architecture

```text
React dashboard
      |
Spring Boot API  <---->  PostgreSQL
      |
Redis Streams (job queue)
      |
Scan workers  --->  SubMerge, HTTPX, Nmap, WhatWeb, Nuclei
      |
MinIO (reports and artifacts)
```

<!-- Replace this with a proper diagram image if you can (draw.io or excalidraw works fine). -->

The API takes a scan request, saves it, and puts a job on the Redis stream. Workers pick it up, run the pipeline, and report progress back. If a worker stops sending heartbeats, its job goes back in the queue. The dashboard listens over a WebSocket, so you see progress as it happens.

## Scan pipeline

```text
SubMerge -> import assets -> HTTPX -> Nmap -> WhatWeb -> Nuclei -> risk scoring -> report
```

1. **SubMerge** discovers subdomains
2. **HTTPX** checks which hosts are alive
3. **Nmap** finds open ports and service versions
4. **WhatWeb** fingerprints web technologies
5. **Nuclei** checks for known vulnerabilities
6. The **risk engine** scores everything and the report is generated

SubMerge is treated as a black box. The backend doesn't parse its terminal output. It waits for the script to finish, then reads:

```text
output/<domain>/final/alive.txt
output/<domain>/final/resolved.txt
output/<domain>/run.log
```

Details are in [docs/SCANNER_CONTRACT.md](docs/SCANNER_CONTRACT.md).

## Tech stack

| Part | Tech |
|---|---|
| Frontend | React, TypeScript, Tailwind |
| Backend | Spring Boot, JWT auth |
| Database | PostgreSQL |
| Queue | Redis Streams |
| Storage | MinIO |
| Live updates | WebSocket |
| Scanning | SubMerge, HTTPX, Nmap, WhatWeb, Nuclei |
| Deployment | Docker, Docker Compose |

## Getting started

### Docker (easiest)

```bash
cp .env.example .env
docker compose -f infra/docker-compose.yml up --build
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
docker compose -f infra/docker-compose.yml up --build
```

### Local development

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Once it's running:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html

### Scan modes

Set `ASM_SCAN_MODE` in your `.env`:

- `mock` (default): returns fake but consistent data, so you can try the dashboard without scanning anything.
- `tools`: runs the real tools. SubMerge, HTTPX, Nmap, WhatWeb, and Nuclei need to be installed and on your path, and `recon-engine/submerge.sh` needs to exist.

## SubMerge

SubMerge is a separate project I originally wrote for my own bug bounty recon. It's a Bash script that combines several passive subdomain tools (Amass, Subfinder, Assetfinder, Findomain) into one pipeline. It runs them in parallel, handles timeouts, removes duplicates, expands results with DNSGen and MassDNS, and uses HTTPX to find live hosts. AttackGrid reuses it as its asset discovery engine, and it still works fine on its own.

Repo: [github.com/umang-mishra23/SubMerge](https://github.com/umang-mishra23/SubMerge)

## Limitations and roadmap

Where this stands today:

- It's a solo project, tested on my own lab setup and domains I own, not at real production scale.
- Risk scoring is rule-based and fairly simple.
- Planned but not built yet: domain ownership verification, web asset screenshots, WHOIS and SSL checks, directory discovery, API endpoint discovery, and notifications.
- Automated test coverage is still thin.

## Security and legal

Only scan domains you own or have written permission to test. AttackGrid is built for authorized asset inventory and security assessment. It has no exploitation, evasion, or persistence features, and that's on purpose. You are responsible for how you use it.

## Contact

Umang Mishra
[LinkedIn](https://www.linkedin.com/in/-umangmishra/) | [GitHub](https://github.com/umang-mishra23) | umangmishra2327@gmail.com
