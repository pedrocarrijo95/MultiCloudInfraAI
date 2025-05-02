
resource "google_compute_instance" "default" {
  name         = var.instance_display_name
  machine_type = "custom-${var.ocpus_count}-${var.memory_count}"
  zone         = var.zone

  boot_disk {
    initialize_params {
      image = var.image
    }
  }

  network_interface {
    network = "default"
    access_config {}
  }

  allow_stopping_for_update = true

}