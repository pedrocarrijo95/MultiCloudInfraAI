#provider

tenancy_ocid = "ocid1.tenancy.oc1..aaaaaaaaobeqxob5en7zpyjq3red676cqvgr74alea4y7dhegtzgt36cicoa"
user_ocid = "ocid1.user.oc1..aaaaaaaawkohfxuzfxgtc2reixo7x6g4jnh7dipivc6wd3l44sgp4sxtm75q"
fingerprint = "90:c3:b0:ef:56:d5:ff:d8:06:10:a8:06:61:35:43:91"
private_key_path = "/home/opc/block/MCP/Terraform/templates/oracle/key/privatekey.pem"
region = "sa-saopaulo-1"


#compute

compartment_ocid = "ocid1.tenancy.oc1..aaaaaaaaobeqxob5en7zpyjq3red676cqvgr74alea4y7dhegtzgt36cicoa"
shape = "VM.Standard.E3.Flex"
source_ocid = "ocid1.image.oc1.sa-saopaulo-1.aaaaaaaa3meu4uqtlhrpxebhog45gcpmbjfuf3ewcgemxiqneacpqzrv7blq"
source_type = "image"
ocpus_count = "3"
memory_count = "2"
ssh_public_keys = "/home/opc/block/MCP/Terraform/templates/oracle/key/cloudshellkey.pub"

#network

vcn_cidr_block = "10.0.0.0/16"
subnet_cidr_block = "10.0.1.0/24"
availability_domain ="cWdu:SA-SAOPAULO-1-AD-1"

#names

vcn_display_name = "vcn-terraform"
subnet_display_name = "subnet-terraform"
instance_display_name = "vm-oracle-cloud"

#oci compute image list --compartment-id ocid1.tenancy.oc1..aaaaaaaaobeqxob5en7zpyjq3red676cqvgr74alea4y7dhegtzgt36cicoa --region sa-saopaulo-1
#oci iam availability-domain list --compartment-id ocid1.tenancy.oc1..aaaaaaaaobeqxob5en7zpyjq3red676cqvgr74alea4y7dhegtzgt36cicoa --region sa-saopaulo-1
