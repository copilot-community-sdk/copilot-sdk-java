import { Octokit } from '@octokit/rest';

const GITHUB_TOKEN = process.env.GITHUB_TOKEN;
const GITHUB_REPO_OWNER = process.env.GITHUB_REPO_OWNER;
const GITHUB_REPO_NAME = process.env.GITHUB_REPO_NAME;

// Known Copilot Coding Agent bot identity
const COPILOT_BOT_LOGIN = 'Copilot';
const COPILOT_BOT_NODE_ID = 'BOT_kgDOC9w8XQ';

/**
 * Creates a GitHub issue and assigns it to the Copilot Coding Agent (@Copilot).
 * Uses GraphQL to create the issue with the bot's known node ID as assignee.
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
    // Fetch repo node ID
    const repoInfo: any = await octokit.graphql(`
      query($owner: String!, $name: String!) {
        repository(owner: $owner, name: $name) { id }
      }
    `, {
      owner: GITHUB_REPO_OWNER,
      name: GITHUB_REPO_NAME,
    });

    const repoId = repoInfo?.repository?.id;
    if (!repoId) {
      return null;
    }

    const title = description.split('\n')[0].slice(0, 100);

    // Create issue with Copilot bot assigned via known node ID
    const response: any = await octokit.graphql(`
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
    `, {
      repoId,
      title,
      body: description,
      assigneeIds: [COPILOT_BOT_NODE_ID],
    });

    const issue = response?.createIssue?.issue;
    if (!issue) {
      return null;
    }

    console.log(`Assigned to: ${issue.assignees.nodes.map((a: any) => a.login).join(', ')}`);
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