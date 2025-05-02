# provider variables
variable "tenancy_ocid" {}
variable "user_ocid" {}
variable "fingerprint" {}
variable "private_key_path" {}
variable "region" {}

# general oci parameters

variable "compartment_ocid" {
  description = "compartment ocid where to create all resources"
  type        = string
  # no default value, asking user to explicitly set this variable's value. see codingconventions.adoc
}

variable "vcn_display_name" {
  description = "A user-friendly name for the vcn. Does not have to be unique, and it's changeable."
  type        = string
  default     = "vcn-test"
}

variable "subnet_display_name" {
  description = "A user-friendly name for the subnet. Does not have to be unique, and it's changeable."
  type        = string
  default     = "subnet-test"
}

variable "instance_display_name" {
  description = "A user-friendly name for the instance. Does not have to be unique, and it's changeable."
  type        = string
  default     = "instance-test"
}

# compute instance parameters

variable "shape" {
  description = "The shape of an instance."
  type        = string
  default     = "VM.Standard.E3.Flex"
}

variable "source_ocid" {
  description = "The OCID of an image or a boot volume to use, depending on the value of source_type."
  type        = string
}

variable "source_type" {
  description = "The source type for the instance."
  type        = string
  default     = "image"
}

variable "ocpus_count" {
  description = "OCPUs Count."
  type        = number
  default     = 1
}

variable "memory_count" {
  description = "Memory Count."
  type        = number
  default     = 4
}

variable "ssh_public_keys" {
  description = "Public SSH keys to be included in the ~/.ssh/authorized_keys file for the default user on the instance. To provide multiple keys, see docs/instance_ssh_keys.adoc."
  type        = string
  default     = null
}

#network

variable "vcn_cidr_block" {
  description = "CIDR block for the VCN"
  type        = string
}

variable "subnet_cidr_block" {
  description = "CIDR block for the subnet"
  type        = string
}

variable "availability_domain" {
  description = "Availability domain to launch the resources in"
  type        = string
}
