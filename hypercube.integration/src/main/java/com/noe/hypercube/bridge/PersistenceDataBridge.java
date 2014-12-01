package com.noe.hypercube.bridge;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.persistence.domain.MappingEntity;
import com.noe.hypercube.ui.bundle.PathBundle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

@Named
public class PersistenceDataBridge {

    @Inject
    private IPersistenceController persistenceController;

    @PostConstruct
    public void transferData() {
        final Collection<MappingEntity> allMappings = persistenceController.getAllMappings();
        Map<String, DualHashBidiMap<String, String>> mappings = new HashMap<>(allMappings.size());
        for (MappingEntity mapping : allMappings) {
            final String accountName = mapping.getAccountName();
            if(!mappings.containsKey(accountName)) {
                mappings.put(accountName, new DualHashBidiMap<>());
            }
            mappings.get(accountName).put(mapping.getLocalDir(), mapping.getRemoteDir());
        }
        PathBundle.setInitialMappings(mappings);
    }
}
