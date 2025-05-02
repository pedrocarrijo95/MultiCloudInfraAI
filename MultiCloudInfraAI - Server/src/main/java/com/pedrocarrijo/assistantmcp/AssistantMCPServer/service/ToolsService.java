package com.pedrocarrijo.assistantmcp.AssistantMCPServer.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ToolsService {

    private static final Logger logger = Logger.getLogger(ToolsService.class.getName());

    @Value("${mcp.base-path}")
    private String basePath;

    @Value("${mcp.terraform.templates-path}")
    private String templatesPath;

    @Value("${mcp.compartments-file}")
    private String compartmentsFilePath;

    @Value("${terraform.binary.path}")
    private String terraformBinaryPath;

    @Tool(name = "get_current_datetime", description = "Get the current date and time in the user's timezone")
    public String getCurrentDatetime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(name = "create_computevm_cloud", description = "Create/provision a compute VM in Cloud")
    public void createComputeVmCloud(
            @ToolParam(description = "Provider name (oci/oracle,aws/amazon,gcp/google,azure/microsoft)") String providerName,
            @ToolParam(description = "Compartment name") String compartmentName,
            @ToolParam(description = "OCPU count") int ocpusCount,
            @ToolParam(description = "Memory in GB") int memoryCount,
            @ToolParam(description = "VCN display name") String vcnDisplayName,
            @ToolParam(description = "Subnet display name") String subnetDisplayName,
            @ToolParam(description = "Instance display name") String instanceDisplayName) throws IOException {

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
    }

    @Tool(name = "delete_computevm_cloud", description = "Delete/destroy a compute VM in Cloud")
    public void deleteComputeVmOracleCloud(
            @ToolParam(description = "Provider name (oci/oracle,aws/amazon,gcp/google,azure/microsoft)") String providerName,
            @ToolParam(description = "Instance display name") String instanceDisplayName) {

        String provider = resolveProviderFolder(providerName);
        String terraformPath = basePath + "Terraform/" + provider + "/compute/" + instanceDisplayName;

        boolean success = runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "destroy", "-auto-approve"));
        if (success) {
            try {
                Files.walk(Paths.get(terraformPath))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                logger.warning("Terraform destroy success, but folder not deleted: " + e.getMessage());
            }
            logger.info("Terraform destroy completed and folder deleted.");
        } else {
            logger.warning("Terraform destroy failed — folder not deleted.");
        }
    }

    @Tool(name = "edit_computevm_cloud", description = "Edit an existing compute VM in Cloud")
    public void editComputeVmOracleCloud(
            @ToolParam(description = "Provider name (oracle,aws,gcp,azure)") String providerName,
            @ToolParam(description = "Instance display name") String instanceDisplayName,
            @ToolParam(description = "New Instance display name") String newInstanceDisplayName,
            @ToolParam(description = "New OCPU count") int newOcpusCount,
            @ToolParam(description = "New memory in GB") int newMemoryCount) throws IOException {

        String provider = resolveProviderFolder(providerName);
        String terraformPath = basePath + "Terraform/" + provider + "/compute/" + instanceDisplayName;

        Path tfvarsPath = Paths.get(terraformPath + "/terraform.tfvars");
        String tfvarsContent = Files.readString(tfvarsPath);
        tfvarsContent = editCommonVariables(tfvarsContent, newOcpusCount, newMemoryCount, newInstanceDisplayName, provider);
        Files.writeString(tfvarsPath, tfvarsContent);

        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "plan"));
        runTerraformCommand(terraformPath, List.of(terraformBinaryPath, "apply", "-auto-approve"));
    }

    private Properties getPropertiesFromFile(String filePath) {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(filePath)) {
            props.load(input);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Properties file not found: " + filePath, e);
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
            logger.log(Level.SEVERE, "Failed to run terraform command: " + command, e);
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