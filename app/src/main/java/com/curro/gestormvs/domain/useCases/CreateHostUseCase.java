package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.domain.models.Host;

public class CreateHostUseCase {

    private final HostRepository hostRepository;

    public CreateHostUseCase(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }


    // Validations -------------------------
    private void nonNullOrEmptyFields(Host host) {

        if (host.getName() == null || host.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name field cannot be empty.");
        }

        if (host.getUser() == null || host.getUser().trim().isEmpty()) {
            throw new IllegalArgumentException("User field cannot be empty.");
        }

        if (host.getIp() == null || host.getIp().trim().isEmpty()) {
            throw new IllegalArgumentException("IP field cannot be empty.");
        }

    }

    private void validPort(Host host) {

        if(host.getPort() != null && (host.getPort() < 1 || host.getPort() > 65535)) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

    }

    private void validStrings(Host host) {

        if (host.getName().length() > 20 ) {
            throw new IllegalArgumentException("Name field cannot be longer than 20 characters.");
        }

        if (host.getUser().length() > 20 ) {
            throw new IllegalArgumentException("User field cannot be longer than 20 characters.");
        }

        if (!host.getIp().matches("^([a-zA-Z0-9-]+\\.){3}[a-zA-Z0-9-]+$")) {
            throw new IllegalArgumentException("IP field must be a valid IPv4 or domain address.");
        }

        if (host.getPassword() != null && host.getPassword().length() > 20) {
            throw new IllegalArgumentException("Password field cannot be longer than 20 characters.");
        }

    }


    // Use case -------------------------
    public void execute(Host host) {
        nonNullOrEmptyFields(host);
        validPort(host);
        validStrings(host);

        if (host.getPort() == null) {
            host.setPort(22);
        }

        hostRepository.createHost(host);
    }

}
