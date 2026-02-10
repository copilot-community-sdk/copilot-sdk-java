---
description: |
  Automatically updates WORKFLOWS.md when workflow files in .github/workflows
  change. Ensures documentation stays in sync with actual workflows.

on:
  push:
    branches: [main]
    paths:
      - '.github/workflows/**'
  workflow_dispatch:

permissions:
  contents: read

network:
  allowed:
    - defaults

tools:
  github:
    toolsets: [repos]
  edit:

safe-outputs:
  create-pull-request:
    title-prefix: "[doc] "
    labels: [documentation]
    draft: false
---
# Sync WORKFLOWS.md with Actual Workflows

This workflow ensures that `WORKFLOWS.md` in the root directory stays synchronized with the actual workflow files in `.github/workflows/`.

## Your Task

You are responsible for keeping the `WORKFLOWS.md` file up to date with the workflows defined in `.github/workflows/`. This documentation file is critical for developers to understand what automation exists in this repository.

## Instructions

### 1. Read Current Workflow Files

List and read all workflow files in `.github/workflows/`:
- Regular GitHub Actions workflows (`.yml` files)
- Agentic workflow source files (`.md` files)
- Note: Skip `.lock.yml` files as they are compiled outputs

For each workflow file, extract:
- **Name/Title**: From the workflow name or file description
- **Description**: What the workflow does
- **Triggers**: When the workflow runs (push, pull_request, schedule, workflow_dispatch, etc.)
- **Schedule**: If it has a cron schedule, note the timing

### 2. Read Current WORKFLOWS.md Structure

Read the existing `WORKFLOWS.md` file in the repository root carefully to understand:
- The overall document structure (sections, table format, etc.)
- How workflows are currently documented
- The writing style and level of detail
- Any special formatting or conventions used

**Important**: Respect and maintain the current structure and style of WORKFLOWS.md. Do not completely rewrite it—only update the content to reflect current workflows.

### 3. Update WORKFLOWS.md

Update `WORKFLOWS.md` to match the current state of workflows in `.github/workflows/`:

**Add new workflows** that appear in `.github/workflows/` but are missing from WORKFLOWS.md:
- Add them to the overview table
- Add a dedicated section with description following the existing format
- Place them in a logical position (grouped with similar workflows if applicable)

**Update existing workflows** if their details have changed:
- Update trigger information
- Update descriptions
- Update schedule information
- Preserve the existing writing style and detail level

**Remove obsolete workflows** that are documented in WORKFLOWS.md but no longer exist in `.github/workflows/`

**Maintain consistency**:
- Keep the same table format in the overview section
- Use the same heading levels and structure
- Maintain the same level of detail in descriptions
- Keep any special notes or formatting conventions
- Preserve links to workflow files relative to the WORKFLOWS.md location

### 4. Verify Your Changes

Before submitting:
- Ensure all workflows in `.github/workflows/` are documented
- Verify no obsolete workflows remain documented
- Check that the document structure and style are preserved
- Ensure all internal links work correctly
- Make sure the overview table is complete and accurate

### 5. Create a Pull Request

After making the updates to `WORKFLOWS.md`:

1. Use the `edit` tool to write the updated content to `WORKFLOWS.md`
2. Use the `create-pull-request` safe output to create a PR with:
   - A clear title describing the update (e.g., "Update WORKFLOWS.md to reflect current workflows")
   - A detailed description listing:
     - Workflows added (if any)
     - Workflows updated (if any) 
     - Workflows removed from docs (if any)
   - The changes to `WORKFLOWS.md`

The PR will be automatically labeled with `documentation` and have the title prefix `[doc]`.

## Important Notes

- **Respect the existing structure**: Do not completely rewrite WORKFLOWS.md. Only update what needs to be updated.
- **Match the style**: Follow the existing writing style, formatting, and level of detail.
- **Be thorough**: Make sure no workflows are missing from the documentation.
- **Handle both types**: Document both regular GitHub Actions workflows (.yml) and agentic workflows (.md/.lock.yml).
- **Relative links**: Use relative paths from WORKFLOWS.md to workflow files (e.g., `.github/workflows/build-test.yml`). Since WORKFLOWS.md is in the root directory, all workflow links should use `.github/workflows/` prefix.
- **Agentic workflows**: For workflows with both `.md` and `.lock.yml` files, document them as agentic workflows and note that the `.lock.yml` is auto-generated.

## Example Output Summary

After execution, you should report:
- ✅ How many workflows were documented
- 📝 Which workflows were added (if any)
- 🔄 Which workflows were updated (if any)
- 🗑️ Which workflows were removed from docs (if any)
- 📋 Confirmation that WORKFLOWS.md structure was preserved
