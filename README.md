
# 🚀 MultiCloudInfraAI

Provisioning via CLI is slow? I built a project that turns natural language into real infrastructure.

Watch the Video Demo:
https://youtu.be/t7J4fByAFtg?si=032UetLjogcdcIaY

**MultiCloudInfraAI** is an **open-source project** to bring AI-powered natural language interfaces to Terraform-based **multi-cloud infrastructure provisioning**.

Created by **Pedro Carrijo**, this system lets users — technical or not — create, edit, and destroy cloud infrastructure across providers like **Oracle Cloud, Google Cloud**, and more (AWS, Azure) by simply describing what they want in natural language.

Built with **Spring Boot**, **Terraform**, and **Spring AI**, MultiCloudInfraAI introduces a new era of **Conversational Infrastructure as Code**.

---

## ✅ What It Does

- 💬 Accepts natural language commands (via Spring AI + LLMs).
- ⚙️ Generates Terraform configs on the fly for infrastructure creation.
- ☁️ Supports provisioning, editing, and destroying compute instances.
- 🔄 Automatically applies changes via Terraform CLI.
- 🌐 Works with multiple cloud providers (multi-cloud ready).
- 🧱 Modular architecture for easy extension.

> **Currently supports:**
> - ✅ Create / Edit / Delete **Compute Instances** in **Oracle Cloud (OCI)** and **Google Cloud (GCP)**.

You can easily expand to support other resources (e.g. buckets, databases, etc.) and providers (e.g. AWS, Azure).

![Architecture](https://github.com/pedrocarrijo95/MultiCloudInfraAI/blob/main/docs/project-architecture.png)

---

## 🧠 Powered By

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring AI (MCP Server)](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- [Terraform](https://www.terraform.io/)
- OpenAI / LLMs
- Spring Shell CLI (MCP Client)

---

## 🧪 Example Prompt via CLI

```bash
chat create a VM named dev-instance, with VCN named dev-vcn, subnet named dev-subnet, using 2 OCPUs and 4 GB memory in Oracle Cloud under compartment oci-dev-compartment
```

Or for Google Cloud:

```bash
chat create a VM named gcp-test, using 2 OCPUs and 8 GB memory in Google Cloud
```

---
## 🧪 Test It Yourself

Want to try MultiCloudInfraAI now? You can! Follow our **step-by-step guide** to set it up locally and start creating infrastructure with natural language.

👉 [How to Set Up MultiCloudInfraAI](docs/guide-setup-expandability.md)

## 🖥️ How to Run (Follow the doc above before run that)

### Step 1 — Run MCP Server

Navigate to the `mcp-server` folder and run the backend:

```bash
mvn spring-boot:run
```

This launches the server responsible for generating and applying Terraform infrastructure based on the AI interpretation.

### Step 2 — Run MCP Client

Navigate to the `mcp-client` folder. This CLI interface lets you type natural language prompts.

Edit the file: `src/main/resources/application.properties` and set the connection to your MCP Server instance endpoint: Ex - localhost:8181.

Then run the client:

```bash
mvn spring-boot:run
```

Once running, you'll see a Spring Shell prompt. You can now type natural language infrastructure requests with "chat" prefix.

---

## 🌐 Extra: GitHub Integration
The mcp-client can also connect with the official [GitHub-hosted MCP Server](https://github.com/github/github-mcp-server) recently released.

To use it:

- Open ChatCommands.java

- Set the environment variable GITHUB_PERSONAL_ACCESS_TOKEN with your GitHub token

This allows you to interact with a live MCP Server hosted in the cloud, ideal for testing without local setup.

## 🔄 Expandability

You can add:

- New **providers** (like AWS or Azure)
- New **resource types** (like buckets, load balancers, databases...)

Follow the detailed [implementation & extension tutorial](docs/guide-setup-expandability.md) for guidance on:

- Setting up Terraform templates
- Creating Java methods
- Wiring new tools with Spring AI

---

## 📁 Project Structure

```
/mcp-server       → Core logic for infrastructure generation
/mcp-client       → Spring Shell client to send requests via LLM
/templates/       → Terraform templates organized by provider/resource
/docs/            → Full setup and extension guides
```

---

## 🪪 Author & Credits

Created by [Pedro Carrijo](https://github.com/pedrocarrijo95), this is a project to integrate AI, Terraform, and multi-cloud provisioning into a single, extensible platform.

---

## 📘 Documentation

📖 For setup instructions, adding providers or resources, and template rules, read:

👉 [`docs/guide-setup-expandability.md`](docs/guide-setup-expandability.md)

---

## ⚙️ Do you want try with Quarkus?

📖 For setup instructions, read:

👉 [`(Optional) MultiCloudInfraAI - QuarkusClientServer`](https://github.com/pedrocarrijo95/MultiCloudInfraAI/tree/main/(Optional)%20MultiCloudInfraAI%20-%20QuarkusClientServer)

---

## 📢 License & Contributions

MultiCloudInfraAI is **open-source** to encourage innovation in cloud infrastructure automation.

You’re free to fork, use, adapt, or extend — with or without contributing back.

---
