package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.domain.models.Host;

import java.util.List;

public class ListHostsUseCase {

    private final HostRepository hostRepository;

    public ListHostsUseCase(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }


    // Use case -------------------------
    public List<Host> execute() {
        return hostRepository.getAllHosts();
    }

}
