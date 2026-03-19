package com.curro.gestormvs.domain.useCases;

import com.curro.gestormvs.data.repositories.HostRepository;
import com.curro.gestormvs.domain.models.Host;

public class DeleteHostUseCase {

    private final HostRepository hostRepository;

    public DeleteHostUseCase(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public void execute(Host host) {
        hostRepository.deleteHost(host);
    }

}
