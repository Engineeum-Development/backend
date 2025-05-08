package genum.dataset.security;

import genum.dataset.DTO.DatasetUpdateRequest;
import genum.dataset.domain.Collaborator;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatasetPermissionEvaluator {

    private final SecurityUtils securityUtils;

    public boolean isUserPermitted(DatasetUpdateRequest datasetUpdateRequest) {
        String currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        if (datasetUpdateRequest == null || datasetUpdateRequest.collaborators() == null) return false;
        log.info("current user id: {}",securityUtils.getCurrentAuthenticatedUserId());
        log.info("collaborators: {}", datasetUpdateRequest.collaborators());
        return datasetUpdateRequest.collaborators().stream()
                .anyMatch(collaborator -> currentUserId.equals(collaborator.collaboratorId()));
    }
}
