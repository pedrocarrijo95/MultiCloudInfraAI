# 🚀 MultiCloudInfraAI – Quarkus + LangChain4j Tools

**MultiCloudInfraAI** is an open-source Java project that turns natural language prompts into real, provisioned cloud infrastructure using **Terraform**, **OpenAI (or other LLMs)**, and now runs on **Quarkus** with **LangChain4j Tools**.

This update brings full tool-based infrastructure provisioning to a lightweight and fast Quarkus backend — without needing Spring.

### Watch the Quarkus Video Demo:
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
- OpenAI API Key
- Postman or any REST client for testing

---

## 🔧 Step 1 – Configure `application.properties`

Edit the file:
`src/main/resources/application.properties`

Fill in the values  with your real API key and absolute paths

```properties
quarkus.http.port=8080
quarkus.langchain4j.openai.api-key=<api-key>
#quarkus.langchain4j.ai.gemini.m1.chat-model.model-id=<model-name>


# Terraform Tool VARs
#your base path where contain mcp client and server folders
mcp.base-path=<prefix-path>/MultiCloudInfraAI/ # > (e.g, /home/opc/volume/MultiCloudInfraAI/) edit just this <prefix-path>
#your templates path
terraform.binary.path=/usr/bin/terraform #keep or change the path to your terraform executable
```

## 🔐 Step 2 – Add your Terraform credentials

Go to your Terraform template directory. For example, for Oracle Cloud:


Fill in the file with your actual provider credentials:

* Fields wrapped with `{{variable}}` are dynamically filled by the Java code. Do not edit them manually — they are automatically replaced at runtime.
* Fields between `< >` (e.g., `<tenancy-ocid>`) must be manually filled in before running the project. These are fixed values such as keys, `OCIDs, regions, etc.`

```hcl
tenancy_ocid = "<tenancy_ocid>"
user_ocid = "<user_ocid>"
fingerprint = "<fingerprint>"
private_key_path = "<prefixpath>/MulticloudInfraAI/Terraform/templates/oracle/key/privatekey.pem"
region = "<region>"

ssh_public_keys = "<prefix-path>/MultiCloudInfraAI/Terraform/templates/oracle/key/ssh_key_compute.pub"
``` 

Edit the template terraform folder according your provider (OCI, GCP, ...)

Make sure:

- private_key_path points to a valid private key file on your machine
- region matches the region where you want to provision resources
- You repeat this setup for each provider you plan to use (e.g., GCP, AWS)
- The .tfvars.template file is used to dynamically generate real .tfvars during provisioning.

## 🔐 Step 3 – Only for Oracle Cloud

* (Only for Oracle Cloud) Configure the file `/src/main/resources/compartments.properties` with the `compartment_name=compartment_ocid`

> If using credentials like keys to authenticate (Oracle Cloud) with a `privatekey.pem` for example, ensure they're in a subfolder (e.g., `oracle/keys/`) and referenced properly.

> To provision your compute VMs (Oracle Cloud) using SSH keys, ensure they're in a subfolder (e.g., `oracle/keys/`) and referenced properly (e.g., `ssh_key_compute.pub`)


## ▶️ Step 4 – Run the Quarkus application

With all configurations in place, start the application in development mode using Maven:

```bash
mvn quarkus:dev
```
This will start the Quarkus server locally on:
```
http://localhost:8080
```

## 💬 Step 5 – Send a prompt using Postman

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

You should receive a response in your MCP Server console logs indicating that the infrastructure provisioning has started.
Open the MCP Server console to see the terraform logs and debug if some errors occurs !

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

### Follow my Blog:
http://pedrocarrijo.dev
