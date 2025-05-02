#provider

project = "pcarrijomcp-458616"
region = "us-central1"
zone = "us-central1-c"
credentials_file = "/home/opc/block/MCP/Terraform/templates/google/svcaccount/pcarrijomcp.json"

#compute

instance_display_name = "vm-test-google"
image = "ubuntu-minimal-2210-kinetic-amd64-v20230126"
#machine_type = "n1-standard-1"
ocpus_count = "1"
memory_count = "1024"
