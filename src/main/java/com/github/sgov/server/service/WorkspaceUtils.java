package com.github.sgov.server.service;

import com.github.sgov.server.model.AttachmentContext;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.TrackableContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility functions related to the workspace management.
 */
public class WorkspaceUtils {

    /**
     * Creates a pull request text for the given workspace.
     *
     * @param workspace workspace to publish
     * @return pull request text to display
     */
    public static String createPullRequestBody(final Workspace workspace) {
        String prBody = MessageFormat.format("Changed vocabularies: \n - {0}",
            render(workspace.getVocabularyContexts()));

        if (!workspace.getAllAttachmentContexts().isEmpty()) {
            prBody += "\n\n";
            prBody += MessageFormat.format("Changed attachments: \n - {0}",
                render(workspace.getAllAttachmentContexts()));
        }
        return prBody;
    }

    private static String render(final Collection<? extends TrackableContext> list) {
        return list.stream()
            .map(c -> c.getBasedOnVersion().toString() + " (context " + c.getUri() + ")")
            .collect(Collectors.joining("\n - "));
    }

    /**
     * Creates a branch name for the given workspace.
     *
     * @param workspace workspace to create branch name for
     * @return branch name
     */
    public static String createBranchName(final Workspace workspace) {
        final String uri = workspace.getUri().toString();
        return "PL-publish-" + uri.substring(uri.lastIndexOf("/") + 1);
    }

    /**
     * Creates a stub of a vocabulary context, based on the vocabulary URI.
     *
     * @param uri URI of the vocabulary to create a stub for.
     * @return vocabulary context
     */
    public static VocabularyContext stub(final URI uri) {
        final VocabularyContext context = new VocabularyContext();
        context.setBasedOnVersion(uri);
        URI contextUri = URI.create(uri.toString() + "/verze/" + UUID.randomUUID());
        context.setUri(contextUri);
        final ChangeTrackingContext changeTrackingContext = new ChangeTrackingContext();
        changeTrackingContext.setChangesVocabularyVersion(uri);
        context.setChangeTrackingContext(changeTrackingContext);
        return context;
    }

    /**
     * Creates a stub of a attachment context, based on the attachment URI.
     *
     * @param uri URI of the attachment to create a stub for.
     * @return vocabulary context
     */
    public static AttachmentContext attachmentStub(final URI uri) {
        final AttachmentContext context = new AttachmentContext();
        context.setBasedOnVersion(uri);
        URI contextUri = URI.create(uri.toString() + "/verze/" + UUID.randomUUID());
        context.setUri(contextUri);
        return context;
    }
}
