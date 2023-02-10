package com.github.sgov.server.controller.dto;

import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonLdAttributeOrder({"uri", "label", "basedOnVersion", "changeTrackingContext", "workspaces"})
public class VocabularyWorkspacesDto extends VocabularyDto {

    List<WorkspaceDto> inWorkspaces;


    /**
     *  Adds workspace in which vocabulary is.
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
    public VocabularyWorkspacesDto(VocabularyDto vocabularyDto) {
        this.setUri(vocabularyDto.getUri());
        this.setTypes(vocabularyDto.getTypes());
        this.setLabel(vocabularyDto.getLabel());
        this.setChangeTrackingContext(vocabularyDto.getChangeTrackingContext());
    }
}
