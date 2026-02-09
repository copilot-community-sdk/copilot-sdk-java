import { Octokit } from '@octokit/rest';

const GITHUB_TOKEN = process.env.GITHUB_TOKEN;
const GITHUB_REPO_OWNER = process.env.GITHUB_REPO_OWNER;
const GITHUB_REPO_NAME = process.env.GITHUB_REPO_NAME;

const GRAPHQL_FEATURES_HEADER = 'issues_copilot_assignment_api_support,coding_agent_model_selection';

/**
 * Creates a GitHub issue and assigns it to the Copilot Coding Agent.
 * Follows the official GitHub API docs:
 * 1. Query suggestedActors to find copilot-swe-agent and get its node ID
 * 2. Get the repository node ID
 * 3. Create issue with assigneeIds + agentAssignment, including required GraphQL-Features header
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
    // Step 1: Fetch repo ID and find copilot-swe-agent in suggestedActors
    const repoInfo: any = await octokit.graphql(`
      query($owner: String!, $name: String!) {
        repository(owner: $owner, name: $name) {
          id
          suggestedActors(capabilities: [CAN_BE_ASSIGNED], first: 100) {
            nodes {
              login
              __typename
              ... on Bot { id }
              ... on User { id }
            }
          }
        }
      }
    `, {
      owner: GITHUB_REPO_OWNER,
      name: GITHUB_REPO_NAME,
      headers: {
        'GraphQL-Features': GRAPHQL_FEATURES_HEADER,
      },
    });

    const repoId = repoInfo?.repository?.id;
    if (!repoId) {
      console.error('Could not fetch repository ID');
      return null;
    }

    const copilotBot = repoInfo.repository.suggestedActors.nodes.find(
      (node: any) => node.login === 'copilot-swe-agent'
    );

    let botId: string;
    if (copilotBot) {
      botId = copilotBot.id;
      console.log(`Found Copilot bot: login=${copilotBot.login}, id=${botId}, type=${copilotBot.__typename}`);
    } else {
      // Fallback: the GITHUB_TOKEN in Actions may lack permission to see suggestedActors.
      // Use the known node ID for copilot-swe-agent.
      botId = 'BOT_kgDOC9w8XQ';
      console.log(`copilot-swe-agent not found in suggestedActors, using known bot ID: ${botId}`);
    }

    const title = description.split('\n')[0].slice(0, 100);

    // Step 2: Create issue with agentAssignment and required GraphQL-Features header
    const response: any = await octokit.graphql(`
      mutation($repoId: ID!, $title: String!, $body: String!, $assigneeIds: [ID!]) {
        createIssue(input: {
          repositoryId: $repoId,
          title: $title,
          body: $body,
          assigneeIds: $assigneeIds,
          agentAssignment: {
            targetRepositoryId: $repoId,
            baseRef: "main",
            customInstructions: "",
            customAgent: "",
            model: ""
          }
        }) {
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
      assigneeIds: [botId],
      headers: {
        'GraphQL-Features': GRAPHQL_FEATURES_HEADER,
      },
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