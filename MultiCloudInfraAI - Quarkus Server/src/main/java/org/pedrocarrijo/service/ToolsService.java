package org.pedrocarrijo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.nio.file.StandardCopyOption;


import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;



public class ToolsService {
    
    private static final Logger logger = Logger.getLogger(ToolsService.class);
    
    @ConfigProperty(name = "mcp.base-path")
    private String basePath;

    @ConfigProperty(name = "mcp.terraform.templates-path")
    private String templatesPath;

    @ConfigProperty(name = "mcp.compartments-file")
    private String compartmentsFilePath;

    @ConfigProperty(name = "terraform.binary.path")
    private String terraformBinaryPath;

    @Prompt(description = "Put you description here.") 
    PromptMessage foo(@PromptArg(description = "The name") String name) { 
        return PromptMessage.withUserRole("Hello " + name + "!");
    }

    @PostConstruct
    void init() {
        System.out.println(">>> ToolsService bean carregado.");
    }

    @Tool(name = "create_computevm_cloud", description = "Create/provision a compute VM in Cloud")
    String createComputeVmCloud(
            @ToolArg(description = "Provider name (oci/oracle,aws/amazon,gcp/google,azure/microsoft)") String providerName,
            @ToolArg(description = "Compartment name") String compartmentName,
            @ToolArg(description = "OCPU count") int ocpusCount,
            @ToolArg(description = "Memory in GB") int memoryCount,
            @ToolArg(description = "VCN display name") String vcnDisplayName,
            @ToolArg(description = "Subnet display name") String subnetDisplayName,
            @ToolArg(description = "Instance display name") String instanceDisplayName) throws IOException {

        Map<String, String> variables = new HashMap<>();
        String provider = resolveProviderFolder(providerName);
        addCommonVariables(variables, ocpusCount, memoryCount, instanceDisplayName, provider);

        if (provider.equals("oracle")) {
            addOCIComputeVariables(variables, compartmentName, vcnDisplayName, subnetDisplayName);
        }

        String resolvedTemplatesPath = templatesPath + provider + "/compute/";
        String terraformPath = basePath + "/Terraform/" + provider + "/compute/" + instanceDisplayName;

        processTemplates(terraformPath, resolvedTemplatesPath, variables);
        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "init"));
        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "plan"));
        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "apply", "-auto-approve"));

        return "Compute VM created successfully in " + providerName + " cloud.";
    }

    @Tool(name = "delete_computevm_cloud", description = "Delete/destroy a compute VM in Cloud")
    String deleteComputeVmOracleCloud(
            @ToolArg(description = "Provider name (oci/oracle,aws/amazon,gcp/google,azure/microsoft)") String providerName,
            @ToolArg(description = "Instance display name") String instanceDisplayName) {

        String provider = resolveProviderFolder(providerName);
        String terraformPath = basePath + "Terraform/" + provider + "/compute/" + instanceDisplayName;

        boolean success = runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "destroy", "-auto-approve"));
        if (success) {
            try {
                Files.walk(Paths.get(terraformPath))
                        .sorted(Comparator.reverseOrder())
                        .map(path -> path.toFile())
                        .forEach(File::delete);
            } catch (IOException e) {
                logger.warn("Terraform destroy success, but folder not deleted: " + e.getMessage());
            }
            logger.info("Terraform destroy completed and folder deleted.");
        } else {
            logger.warn("Terraform destroy failed — folder not deleted.");
        }

        return "Compute VM deleted successfully in " + providerName + " cloud.";
    }

    @Tool(name = "edit_computevm_cloud", description = "Edit an existing compute VM in Cloud")
    String editComputeVmOracleCloud(
            @ToolArg(description = "Provider name (oracle,aws,gcp,azure)") String providerName,
            @ToolArg(description = "Instance display name") String instanceDisplayName,
            @ToolArg(description = "New Instance display name") String newInstanceDisplayName,
            @ToolArg(description = "New OCPU count") int newOcpusCount,
            @ToolArg(description = "New memory in GB") int newMemoryCount) throws IOException {

        String provider = resolveProviderFolder(providerName);
        String terraformPath = basePath + "Terraform/" + provider + "/compute/" + instanceDisplayName;

        Path tfvarsPath = Paths.get(terraformPath + "/terraform.tfvars");
        String tfvarsContent = Files.readString(tfvarsPath);
        tfvarsContent = editCommonVariables(tfvarsContent, newOcpusCount, newMemoryCount, newInstanceDisplayName, provider);
        Files.writeString(tfvarsPath, tfvarsContent);

        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "plan"));
        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "apply", "-auto-approve"));
    
        return "Compute VM edited successfully in " + providerName + " cloud.";
    }

    private Properties getPropertiesFromFile(String filePath) {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(filePath)) {
            props.load(input);
        } catch (Exception e) {
            logger.log(Level.FATAL, "Properties file not found: " + filePath, e);
        }
        return props;
    }

    private void processTemplates(String outputPath, String templatesPath, Map<String, String> variables) throws IOException {
        Map<String, String> templates = Map.of(
                "terraform.tfvars", "terraform.tfvars.template",
                "compute.tf", "compute.tf.template"
        );

        for (var entry : templates.entrySet()) {
            String outputFile = outputPath + "/" + entry.getKey();
            String templateFile = templatesPath + entry.getValue();
            String content = Files.readString(Paths.get(templateFile));

            for (var variable : variables.entrySet()) {
                content = content.replace("{{" + variable.getKey() + "}}", variable.getValue());
            }

            Files.createDirectories(Paths.get(outputFile).getParent());
            Files.writeString(Paths.get(outputFile), content);
        }

        List<String> otherFiles = List.of("variables.tf", "provider.tf", "versions.tf");
        for (String file : otherFiles) {
            copyFile(templatesPath + file, outputPath + "/" + file);
        }
    }

    private boolean runTerraformCommand(String directory, List<String> command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(directory));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(logger::info);
            }

            return process.waitFor() == 0;
        } catch (Exception e) {
            logger.log(Level.FATAL, "Failed to run terraform command: " + command, e);
            return false;
        }
    }

    private void copyFile(String sourcePath, String destinationPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);
        Files.createDirectories(destination.getParent());
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private String resolveProviderFolder(String providerName) {
        return switch (providerName.toLowerCase()) {
            case "oracle", "oci" -> "oracle";
            case "aws", "amazon" -> "aws";
            case "azure", "microsoft" -> "azure";
            case "gcp", "google" -> "google";
            default -> throw new IllegalArgumentException("Unsupported provider: " + providerName);
        };
    }

    private void addCommonVariables(Map<String, String> variables, int ocpusCount, int memoryCount, String instanceDisplayName, String provider) {
        variables.put("ocpus_count", String.valueOf(ocpusCount));
        if (provider.equals("google")) {
            memoryCount *= 1024; // Convert GB to MB for Google
        }
        variables.put("memory_count", String.valueOf(memoryCount));
        variables.put("instance_display_name", instanceDisplayName);
    }

    private String editCommonVariables(String tfvarsContent, int ocpus, int memory, String newName, String provider) {
        if (ocpus > 0) {
            tfvarsContent = tfvarsContent.replaceAll("ocpus_count\\s*=\\s*\"?\\d+\"?", "ocpus_count = \"" + ocpus + "\"");
        }
        if (memory > 0) {
            System.out.println("Memory: " + memory);
            int memoryValue = provider.equalsIgnoreCase("google") ? memory * 1024 : memory;
            tfvarsContent = tfvarsContent.replaceAll("memory_count\\s*=\\s*\"?\\d+\"?", "memory_count = \"" + memoryValue + "\"");
        }
        if (newName != null && !newName.isBlank()) {
            tfvarsContent = tfvarsContent.replaceAll("instance_display_name\\s*=\\s*\"[^\"]*\"", "instance_display_name = \"" + newName + "\"");
        }
        return tfvarsContent;
    }

    private void addOCIComputeVariables(Map<String, String> variables, String compartmentName, String vcnDisplayName, String subnetDisplayName) {
        Properties propsCompartments = getPropertiesFromFile(compartmentsFilePath);
        String compartmentOcid = propsCompartments.getProperty(compartmentName);
        variables.put("compartment_ocid", compartmentOcid);
        variables.put("vcn_display_name", vcnDisplayName);
        variables.put("subnet_display_name", subnetDisplayName);
        if (compartmentOcid == null) {
            throw new IllegalArgumentException("Compartment not found in properties file: " + compartmentName);
        }
    }

}
