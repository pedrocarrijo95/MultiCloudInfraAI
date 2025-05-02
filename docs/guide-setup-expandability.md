# MultiCloudInfraAI

**MultiCloudInfraAI** is the first open-source project that combines AI-powered natural language input with multi-cloud infrastructure provisioning via Terraform.

---

## 🚀 Project Setup

### 1. Configure Templates (Oracle or Google Cloud)

* Go to the folder `templates/oracle/compute/` or `templates/google/compute/`.
* Open the `terraform.tfvars.template` file and insert your credentials/config info.
* Make sure static info like region or credentials are filled in `terraform.tfvars.template`.

> If using credentials like keys, ensure they're in a subfolder (e.g., `oracle/keys/`) and referenced properly.

* Fields wrapped with {{variable}} are dynamically filled by the Java code. Do not edit them manually — they are automatically replaced at runtime.
* Fields between < > (e.g., <tenancy-ocid>) must be manually filled in before running the project. These are fixed values such as keys, OCIDs, regions, etc.

---

### 2. Start MCP Server

This is the backend that interprets natural language and triggers Terraform.

```bash
cd mcp-server
mvn spring-boot:run
```

---

### 3. Start MCP Client (Spring Shell)

This is the CLI interface where you type natural language prompts.

1. Go to `mcp-client`
2. Open `src/main/resources/application.properties`
3. Set the connection URL to your MCP Server (e.g., `http://localhost:8181`)

```bash
cd mcp-client
mvn spring-boot:run
```

Once started, you can type:

```bash
chat create a vm with name dev-instance, vcn name dev-vcn, subnet name dev-subnet, with 2 ocpus and 4gb memory in oracle cloud compartment my-compartment
```

---

## ✅ How to add a new **provider** or **resource**

### 1. Create the templates

* Go to the `templates/` folder.
* Create a subfolder named after the **provider** (e.g., `aws`, `google`, `azure`, `oracle`...).
* Inside it, create another folder for the **resource** (e.g., `compute`, `database`).
* Add the required Terraform files:

  * Files that need dynamic values must end with `.template` (e.g., `terraform.tfvars.template`, `compute.tf.template`).
  * Static files (e.g., `variables.tf`, `provider.tf`, `versions.tf`) should be copied directly.
* If the provider requires keys or credentials, create a folder inside the provider folder (e.g., `aws/keys/`) to store them, and make sure the path is referenced in the `.tfvars`.

> 🧠 Use `{{variable}}` inside template files to mark dynamic substitutions.

---

### 2. Map the variables

If `addCommonVariables(...)` is not enough:

* Create a new method following this pattern:

```java
add<Provider><Resource>Variables(Map<String, String> variables, ...)
```

Example:

```java
addOCIComputeVariables(variables, compartmentName, vcnDisplayName, subnetDisplayName);
```

* Fill the map with provider-specific variables.

---

### 3. Update `createComputeVmCloud(...)`

* Add new `@ToolParam`s if needed.
* ⚠️ If removing a param, make sure it’s not used by another provider to avoid breaking things. When in doubt, just keep existing params and add new ones.
* Add a new `if` block based on the resolved provider:

```java
if (provider.equals("aws")) {
    addAWSComputeVariables(variables, ...);
}
```

---

### 4. Configure the paths

Ensure the `application.properties` has:

```properties
mcp.terraform.templates-path=/your/path/templates/
```

The structure will resolve to:

```
templates/<provider>/<resource>/
```

---

### 5. Remove resource

If you're working with a new resource type (not `compute`), duplicate the `deleteComputeVmOracleCloud` method and:

* Rename it (e.g., `deleteDatabaseVmOracleCloud`)
* Change `"compute"` in the path to the name of the new resource.

No additional changes are needed.

---

### 6. Edit resource

* For standard changes (`ocpus`, `memory`, `display_name`), use `editCommonVariables(...)`.
* For provider-specific logic, create:

```java
edit<Provider><Resource>Variables(String tfvarsContent, ...)
```

Call this before writing the file.
```java
Files.write(...)
```

---

### 7. Provider resolution helper

The method `resolveProviderFolder(String providerName)` allows flexibility in naming. For example:

```java
switch (providerName.toLowerCase()) {
  case "oracle", "oci" -> "oracle";
  case "aws", "amazon" -> "aws";
  case "azure", "microsoft" -> "azure";
  case "gcp", "google" -> "google";
}
```

---

### 8. Compartments for Oracle (OCI) ⚠️

Create a file named `compartments.properties` in `resources/`, mapping:

```
<name>=<ocid>
```

Used by the `addOCIComputeVariables(...)` method to resolve compartment OCIDs dynamically.

---

### ✅ Summary: What to do for another resource (e.g., `database`)

* Duplicate `createComputeVmCloud(...)`, `editComputeVmOracleCloud(...)`, and `deleteComputeVmOracleCloud(...)`, renaming `compute` to `database`.
* Update folder paths accordingly.
* Create `add<Provider>DatabaseVariables(...)` and optionally `edit<Provider>DatabaseVariables(...)`.
* Create templates under: `templates/<provider>/database/`

Ready! 🌟 Your new resource is fully supported.
