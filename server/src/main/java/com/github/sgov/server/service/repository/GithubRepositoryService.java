package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.service.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;
    private final CredentialsProvider credentialsProvider;

    /**
     * Creates a new Github repository service.
     */
    @Autowired
    public GithubRepositoryService(final RepositoryConf repositoryConf,
                                   final SecurityUtils securityUtils) {
        this.repositoryConf = repositoryConf;
        this.securityUtils = securityUtils;
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(
            repositoryConf.getGithubUserToken(), "");
    }

    /**
     * Checks out the given branch {@param branchName} from SSP repo into {@param dir}.
     *
     * @param branchName name of the branch to be checked out
     * @param dir        directory to checkout the repo into
     * @return JGit object to work with
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
     * @param git     GIT repository to commit into
     * @param message commit message
     * @throws PublicationException if an error during commit occurs
     */
    public void commit(Git git, String message) {
        try {
            git.add()
                .addFilepattern(".")
                .call();

            git.commit()
                .setAll(true)
                .setAuthor(securityUtils.getCurrentUser().getFirstName()
                        + " " + securityUtils
                        .getCurrentUser().getLastName(),
                    securityUtils.getCurrentUser().getUsername())
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
        final HttpResponse<JsonNode> prGet =
            Unirest
                .get("https://api.github.com/repos/" + repositoryConf.getGithubOrganization() + "/"
                    + repositoryConf.getGithubRepo() + "/pulls")
                .header("Authorization", "Bearer " + repositoryConf.getGithubUserToken())
                .queryString("state", "open")
                .queryString("head", repositoryConf.getGithubOrganization() + ":" + fromBranch)
                .queryString("base", "master")
                .queryString("sort", "updated")
                .queryString("direction", "desc")
                .asJson();
        if (prGet.isSuccess() && prGet.getBody().getArray().length() > 0) {
            return prGet.getBody().getArray().getJSONObject(0).get("html_url").toString();
        } else {
            log.error("Pull request cannot be obtained, reason {}, {}", prGet.getStatus(),
                prGet.getBody().toPrettyString());
            return null;
        }
    }

    private String createPullRequest(String fromBranch, String title, String body) {
        final HttpResponse<JsonNode> prResponse =
            Unirest
                .post("https://api.github.com/repos/" + repositoryConf.getGithubOrganization() + "/"
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
            log.error("Pull request cannot be obtained, reason {}, {}", prResponse.getStatus(),
                prResponse.getBody().toPrettyString());
            throw new PublicationException(
                "An error occured during opening PR: " + prResponse.getStatus(), null);
        }
    }
}