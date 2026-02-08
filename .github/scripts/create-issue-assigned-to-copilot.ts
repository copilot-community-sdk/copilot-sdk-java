import { Octokit } from '@octokit/rest';

const GITHUB_TOKEN = process.env.GITHUB_TOKEN;
const GITHUB_REPO_OWNER = process.env.GITHUB_REPO_OWNER;
const GITHUB_REPO_NAME = process.env.GITHUB_REPO_NAME;

/**
 * Creates a GitHub issue from the provided description and assigns it to @copilot-swe-agent if available.
 * Uses GraphQL for efficient repo/actor queries and issue creation with assignment.
 * Returns the issue URL on success, null on failure.
 */
export async function createIssueWithCopilot(description: string): Promise<string | null> {
  if (!GITHUB_TOKEN || !GITHUB_REPO_OWNER || !GITHUB_REPO_NAME) {
    return null;
  }

  if (!description.trim()) {
    return null;
  }

  const octokit = new Octokit({ auth: GITHUB_TOKEN });

  try {
    // Fetch repo ID and check for @copilot-swe-agent
    const repoInfoQuery = `
      query($owner: String!, $name: String!) {
        repository(owner: $owner, name: $name) {
          id
          suggestedActors(capabilities: [CAN_BE_ASSIGNED], first: 100) {
            nodes {
              login
              id
            }
          }
        }
      }
    `;

    const repoInfo: any = await octokit.graphql(repoInfoQuery, {
      owner: GITHUB_REPO_OWNER,
      name: GITHUB_REPO_NAME,
    });

    const repoId = repoInfo?.repository?.id;
    if (!repoId) {
      return null;
    }

    const copilotBot = repoInfo.repository.suggestedActors.nodes.find(
      (node: any) => node.login === 'copilot-swe-agent'
    );

    const title = description.split('\n')[0].slice(0, 100);

    if (!copilotBot) {
      // Fallback: Create issue without assignment via REST
      const issue = await octokit.issues.create({
        owner: GITHUB_REPO_OWNER,
        repo: GITHUB_REPO_NAME,
        title,
        body: description,
      });

      return issue.data.html_url;
    }

    // Create issue with assignment via GraphQL
    const createIssueMutation = `
      mutation($repoId: ID!, $title: String!, $body: String!, $assigneeIds: [ID!]) {
        createIssue(input: { repositoryId: $repoId, title: $title, body: $body, assigneeIds: $assigneeIds }) {
          issue {
            number
            title
            url
            assignees(first: 10) { nodes { login } }
          }
        }
      }
    `;

    const response: any = await octokit.graphql(createIssueMutation, {
      repoId,
      title,
      body: description,
      assigneeIds: [copilotBot.id],
    });

    const issue = response?.createIssue?.issue;
    if (!issue) {
      return null;
    }

    return issue.url;
  } catch (error) {
    console.error('Error creating issue:', error);
    return null;
  }
}

// CLI entry point
const description = process.argv[2];
if (!description) {
  console.error('Usage: npx tsx create-issue-assigned-to-copilot.py <description>');
  process.exit(1);
}
createIssueWithCopilot(description).then((url) => {
  if (url) {
    console.log(`Issue created: ${url}`);
  } else {
    console.error('Failed to create issue');
    process.exit(1);
  }
});