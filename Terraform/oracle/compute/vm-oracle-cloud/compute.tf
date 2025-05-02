resource "oci_core_vcn" "vcn-terraform" {
    #Required
    compartment_id = var.compartment_ocid
    cidr_block = var.vcn_cidr_block
    display_name = var.vcn_display_name
}

resource "oci_core_subnet" "subnet-terraform" {

    depends_on = [oci_core_vcn.vcn-terraform]
    #Required
    cidr_block = var.subnet_cidr_block
    compartment_id = var.compartment_ocid
    vcn_id = oci_core_vcn.vcn-terraform.id
    display_name = var.subnet_display_name

}

resource "oci_core_instance" "vm-oracle-cloud" {
    depends_on = [oci_core_subnet.subnet-terraform]
    #Required
    availability_domain = var.availability_domain
    compartment_id = var.compartment_ocid
    display_name = var.instance_display_name
    shape = var.shape
    shape_config {
      ocpus         = var.ocpus_count
      memory_in_gbs = var.memory_count
    }
    preserve_boot_volume = false
    source_details {
        #Required
        source_id = var.source_ocid
        source_type = var.source_type
    }
    create_vnic_details {
      subnet_id = oci_core_subnet.subnet-terraform.id
      assign_public_ip = true
    }
    metadata = {
      ssh_public_keys = file(var.ssh_public_keys)
    }
    
}