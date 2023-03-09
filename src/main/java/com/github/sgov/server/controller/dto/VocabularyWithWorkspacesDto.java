package com.github.sgov.server.controller.dto;

import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonLdAttributeOrder({"uri", "label", "basedOnVersion", "changeTrackingContext", "inWorkspaces"})
public class VocabularyWithWorkspacesDto extends VocabularyDto {

    List<WorkspaceDto> inWorkspaces;


    /**
     * Adds workspace in which vocabulary is.
     *
     * @param workspaceDto workspace DTO
     */
    public void addInWorkspace(WorkspaceDto workspaceDto) {
        if (inWorkspaces == null) {
            inWorkspaces = new ArrayList<>();
        }
        inWorkspaces.add(workspaceDto);
    }

    /**
     * Creates VocabularyWorkspacesDto from VocabularyDto.
     *
     * @param vocabularyDto reference DTO
     */
    public VocabularyWithWorkspacesDto(VocabularyDto vocabularyDto) {
        this.setUri(vocabularyDto.getUri());
        this.setTypes(vocabularyDto.getTypes());
        this.setLabel(vocabularyDto.getLabel());
        this.setBasedOnVersion(vocabularyDto.getBasedOnVersion());
        this.setChangeTrackingContext(vocabularyDto.getChangeTrackingContext());
    }
}
