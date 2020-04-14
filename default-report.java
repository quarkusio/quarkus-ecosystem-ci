//usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.kohsuke:github-api:1.101

import org.kohsuke.github.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

class Report {

	public static void main(String[] args) throws IOException {
		final String token = args[0];
		final String status = args[1];
		final String issueRepo = args[2];
		final Integer issueNumber = Integer.valueOf(args[3]);
		final String thisRepo = args[4];

		final boolean succeed = "success".equalsIgnoreCase(status);
		if ("cancelled".equalsIgnoreCase(status)) {
			System.out.println("Job status is `cancelled` - exiting");
			System.exit(0);
		}

		System.out.println(String.format("The CI build had status %s.", status));

		final GitHub github = new GitHubBuilder().withOAuthToken(token).build();
		final GHRepository repository = github.getRepository(issueRepo);

		final GHIssue issue = repository.getIssue(issueNumber);
		if (issue == null) {
			System.out.println(String.format("Unable to find the issue %s in project %s", issueNumber, issueRepo));
			System.exit(-1);
		} else {
			System.out.println(String.format("Report issue found: %s - %s", issue.getTitle(), issue.getHtmlUrl().toString()));
			System.out.println(String.format("The issue is currently %s", issue.getState().toString()));
		}

		if (succeed) {
			if (issue != null  && isOpen(issue)) {
				// close issue with a comment
				final GHIssueComment comment = issue.comment(String.format("Build fixed:\n* Link to latest CI run: https://github.com/%s/actions", thisRepo));
				issue.close();
				System.out.println(String.format("Comment added on issue %s - %s, the issue has also been closed", issue.getHtmlUrl().toString(), comment.getHtmlUrl().toString()));
			} else {
				System.out.println("Nothing to do - the build passed and the issue is already closed");
			}
		} else  {
			if (isOpen(issue)) {
				final GHIssueComment comment = issue.comment(String.format("The build is still failing:\n* Link to latest CI run: https://github.com/%s/actions", thisRepo));
				System.out.println(String.format("Comment added on issue %s - %s", issue.getHtmlUrl().toString(), comment.getHtmlUrl().toString()));
			} else {
				issue.reopen();
				final GHIssueComment comment = issue.comment(String.format("Unfortunately, the build failed:\n* Link to latest CI run: https://github.com/%s/actions", thisRepo));
				System.out.println(String.format("Comment added on issue %s - %s, the issue has been re-opened", issue.getHtmlUrl().toString(), comment.getHtmlUrl().toString()));
			}
		}

	}

	private static boolean isOpen(GHIssue issue) {
		return (issue.getState() == GHIssueState.OPEN);
	}
}
