# 🚀 MultiCloudInfraAI – Quarkus + LangChain4j Tools

**MultiCloudInfraAI** is an open-source Java project that turns natural language prompts into real, provisioned cloud infrastructure using **Terraform**, **OpenAI (or other LLMs)**, and now runs on **Quarkus** with **LangChain4j Tools**.

This update brings full tool-based infrastructure provisioning to a lightweight and fast Quarkus backend — without needing Spring.

### Watch the Video Demo:
https://youtu.be/t7J4fByAFtg?si=032UetLjogcdcIaY

### Follow my Blog:
http://pedrocarrijo.dev

### Want to explore MultiCloudInfraAI with more resource types, specific cloud providers, and learn how to extend what’s already built? Check out our step-by-step guide and get started now.

👉 [How to Set Up MultiCloudInfraAI](docs/guide-setup-expandability.md)

---

## ✅ Prerequisites

- Java 17+
- Maven 3.8+
- Terraform CLI installed and in your PATH
- OpenAI API Key (or Ollama running locally)
- Postman or any REST client for testing

---

## 🔧 Step 1 – Configure `application.properties`

Edit the file:
src/main/resources/application.properties

Fill in the values  with your real API key and absolute paths

```properties
quarkus.http.port=8080
quarkus.langchain4j.openai.api-key=<api-key>
#quarkus.langchain4j.ai.gemini.m1.chat-model.model-id=<model-name>


# Terraform Tool VARs
#your base path where contain mcp client and server folders
mcp.base-path=/home/opc/volume/MultiCloudInfraAI/
#your templates path
mcp.terraform.templates-path=${mcp.base-path}Terraform/templates/
#your compartments.properties path to use compartmentid Oracle Cloud
mcp.compartments-file=${mcp.base-path}MultiCloudInfraAI - QuarkusClientServer/src/main/resources/compartments.properties
#your path to access terraform binary
terraform.binary.path=/usr/bin/terraform
```

## 🔐 Step 2 – Add your Terraform credentials

Go to your Terraform template directory. For example, for Oracle Cloud:


Fill in the file with your actual provider credentials:

```hcl
tenancy_ocid = "<tenancy_ocid>"
user_ocid = "<user_ocid>"
fingerprint = "<fingerprint>"
private_key_path = "<basepath>/Terraform/templates/oracle/key/privatekey.pem"
region = "<region>"
``` 

Edit the template terraform folder according your provider (OCI, GCP, ...)

Make sure:

- private_key_path points to a valid private key file on your machine
- region matches the region where you want to provision resources
- You repeat this setup for each provider you plan to use (e.g., GCP, AWS)
- The .tfvars.template file is used to dynamically generate real .tfvars during provisioning.

## ▶️ Step 3 – Run the Quarkus application

With all configurations in place, start the application in development mode using Maven:

```bash
mvn quarkus:dev
```
This will start the Quarkus server locally on:
```
http://localhost:8080
```

## 💬 Step 4 – Send a prompt using Postman

Now that the application is running, you can interact with it using Postman or any REST client.

1. Open **Postman**
2. Create a **POST** request to:

```
http://localhost:8080/api/ai/chat
```

3. In the **Body** tab:
   - Select **raw**
   - Choose **Text** (not JSON)

4. Enter a natural language prompt like this:
```
"create a VM named dev-instance with 2 OCPUs and 4GB RAM in Oracle Cloud using VCN dev-vcn and subnet dev-subnet under compartment oci-dev-compartment"
```

5. Click **Send**

You should receive a response indicating that the infrastructure provisioning has started.

The backend will:
- Parse the request with the LLM
- Match it to the correct tool method
- Generate Terraform files dynamically
- Run `terraform init`, `plan`, and `apply`
- Store the state in the configured folder for later edits or deletions

## 📂 What happens behind the scenes?

Here’s how **MultiCloudInfraAI** processes your request step by step:

1. 🧠 The prompt is sent to the selected LLM (e.g., OpenAI)
2. 🧩 LangChain4j analyzes the prompt and matches it to the correct `@Tool` method
3. 📁 The application loads the appropriate Terraform templates based on the provider and resource type
4. 🛠️ It fills in the variables using the prompt data and creates a new folder with a complete Terraform project
5. ⚙️ The system executes:
   - `terraform init`
   - `terraform plan`
   - `terraform apply -auto-approve`
6. 📌 The Terraform state is stored in the generated folder for future updates or deletions

Each execution is isolated by:
- Cloud provider (e.g., `oracle`, `gcp`)
- Resource type (e.g., `compute`)
- Instance name

This ensures safe, modular, and repeatable infrastructure management.

Follow my Blog:
http://pedrocarrijo.dev

