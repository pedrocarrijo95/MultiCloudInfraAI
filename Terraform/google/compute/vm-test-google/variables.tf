# provider variables
variable "project" {}
variable "region" {}
variable "credentials_file" {}




#compute instance parameters

variable "instance_display_name" {
  description = "A user-friendly name for the instance. Does not have to be unique, and it's changeable."
  type        = string
  default     = "instance-test"
}

variable "image" {
  description = "The image of the instance."
  type        = string
  default     = "ubuntu-minimal-2210-kinetic-amd64-v20230126"
}

variable "zone" {
  description = "The zone of the instance."
  type        = string
  default     = "us-central1-a"
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
