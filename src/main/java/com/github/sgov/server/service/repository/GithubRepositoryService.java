package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.service.security.SecurityUtils;
import com.github.sgov.server.util.Constants;
import com.github.sgov.server.util.Folder;
import java.io.File;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to managed workspaces.
 */
@Service
@Slf4j
public class GithubRepositoryService {

    private final RepositoryConf repositoryConf;
    private final CredentialsProvider credentialsProvider;

    /**
     * Creates a new GitHub repository service.
     */
    @Autowired
    public GithubRepositoryService(final RepositoryConf repositoryConf) {
        this.repositoryConf = repositoryConf;
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(
            repositoryConf.getGithubUserToken(), "");
    }

    /**
     * Checks out the given branch {@param branchName} from SSP repo into {@param dir}.
     *
     * @param branchName name of the branch to be checked out
     * @param dir        directory to check out the repo into
     * @return a JGit object to work with
     * @throws PublicationException if an error during cloning/checking out occurs
     */
    public Git checkout(String branchName, File dir) {
        try {
            final Git git = Git.cloneRepository()
                .setURI(repositoryConf.getRemoteUrl())
                .setDirectory(dir)
                .call();
            if (git
                .branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call().stream().anyMatch(x -> x.getName().endsWith(branchName))) {
                git.checkout()
                    .setCreateBranch(true)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .setStartPoint("origin/" + branchName)
                    .setName(branchName).call();
            } else {
                git.checkout()
                    .setCreateBranch(true)
                    .setName(branchName).call();
            }

            return git;
        } catch (GitAPIException e) {
            throw new PublicationException("Error during checking-out SSP repo.", e);
        }
    }

    /**
     * Deletes a file from Git repository.
     *
     * @param git GIT repository to commit into
     * @param f   File to delete
     */
    public void delete(Git git, File f) {
        try {
            git.rm()
                .addFilepattern(f.getAbsolutePath()
                    .substring(git.getRepository().getDirectory().getAbsolutePath().length()
                        + 1 - "/.git".length()))
                .call();
        } catch (GitAPIException e) {
            throw new PublicationException("Error during deleting SSP repo.", e);
        }
    }

    /**
     * Creates a new commit for the given GIT repository with custom message.
     *
     * @param git           GIT repository to commit into
     * @param changedFolder Folder with changes
     * @param message       commit message
     * @throws PublicationException if an error during commit occurs
     */
    public void commit(Git git, Folder changedFolder, String message) {
        try {
            git.add()
                .addFilepattern(changedFolder.getFolder().getAbsolutePath())
                .call();

            git.commit()
                .setAll(true)
                .setAuthor(SecurityUtils.getCurrentUser().getFirstName()
                        + " " + SecurityUtils
                        .getCurrentUser().getLastName(),
                    SecurityUtils.getCurrentUser().getUsername())
                .setMessage(
                    message)
                .call();
        } catch (GitAPIException e) {
            throw new PublicationException("Error during committing SSP repo.", e);
        }
    }


    /**
     * Pushes the changes for the given GIT repository to SSP repo.
     *
     * @param git GIT repository to push changes from
     */
    public void push(Git git) {
        final String remoteUrl = repositoryConf.getRemoteUrl();
        try {
            git.push()
                .setCredentialsProvider(credentialsProvider)
                .setRemote(remoteUrl)
                .call();
        } catch (GitAPIException e) {
            throw new PublicationException("Error during pushing changes to remote.", e);
        }
    }

    /**
     * Creates a PR to master, unless there is an existing one for the same branch - in that case
     * the existing one is reused.
     *
     * @param fromBranch branch to create PR from
     * @param title      title of the PR
     * @param body       body of the PR
     * @return PR URL
     */
    public String createOrUpdatePullRequestToMaster(String fromBranch, String title, String body) {
        String pr = getLatestPullRequestForBranch(fromBranch);

        if (pr == null) {
            pr = createPullRequest(fromBranch, title, body);
        }

        return pr;
    }

    private String getLatestPullRequestForBranch(String fromBranch) {
        final HttpResponse<JsonNode> prResponse =
            Unirest
                .get(Constants.GITHUB_API_REPOS + repositoryConf.getGithubOrganization() + "/"
                    + repositoryConf.getGithubRepo() + "/pulls")
                .header("Authorization", "Bearer " + repositoryConf.getGithubUserToken())
                .queryString("state", "open")
                .queryString("head", repositoryConf.getGithubOrganization() + ":" + fromBranch)
                .queryString("base", "master")
                .queryString("sort", "updated")
                .queryString("direction", "desc")
                .asJson();
        if (prResponse.isSuccess() && prResponse.getBody().getArray().length() > 0) {
            return prResponse.getBody().getArray().getJSONObject(0).get("html_url").toString();
        } else {
            log.debug("Pull request cannot be obtained, returning '{}' ({}) with body {}",
                prResponse.getStatusText(), prResponse.getStatus(),
                prResponse.getBody().toPrettyString());
            return null;
        }
    }

    private String createPullRequest(String fromBranch, String title, String body) {
        log.debug("Trying to create a pull request from branch {}, with title {}", fromBranch,
            title);
        final HttpResponse<JsonNode> prResponse =
            Unirest
                .post(Constants.GITHUB_API_REPOS + repositoryConf.getGithubOrganization() + "/"
                    + repositoryConf.getGithubRepo() + "/pulls")
                .header("Authorization", "Bearer " + repositoryConf.getGithubUserToken())
                .body(new JSONObject()
                    .put("title", title)
                    .put("body", body)
                    .put("head", fromBranch)
                    .put("base", "master")
                )
                .asJson();
        if (prResponse.isSuccess()) {
            return prResponse.getBody().getArray().getJSONObject(0).get("html_url").toString();
        } else {
            log.error("Pull request cannot be created, returning '{}' ({}) with body {}, "
                    + "for branch {} with title {} and body {}",
                prResponse.getStatusText(), prResponse.getStatus(),
                prResponse.getBody().toPrettyString(), fromBranch, title, body);
            throw new PublicationException(
                "An error occurred during opening PR: " + prResponse.getStatus(), null);
        }
    }
}