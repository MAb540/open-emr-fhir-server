#!/usr/bin/env bash
#
# Manage isolated git worktrees for parallel agent sessions.
#
#   ./scripts/worktree.sh add <name>      create branch agent/<name> + worktree
#   ./scripts/worktree.sh list            list worktrees, branch and dirty-file count
#   ./scripts/worktree.sh sync            re-seed .commandcode into every worktree
#   ./scripts/worktree.sh remove <name>   remove worktree + branch
#
# Run this from the main worktree. Every agent session gets its own branch
# (agent/<name>) and its own directory, so source files, the git index and the
# build output never collide.
#
# Environment overrides:
#   WORKTREE_ROOT   where worktrees live
#                   (default: <repo-parent>/basic-fhir-server-wt)
#   BASE_BRANCH     branch new worktrees fork from (default: main)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WT_ROOT="${WORKTREE_ROOT:-$(dirname "$REPO_ROOT")/basic-fhir-server-wt}"
BRANCH_PREFIX="agent"

die() {
  echo "error: $*" >&2
  exit 1
}

usage() {
  sed -n '3,/^$/p' "${BASH_SOURCE[0]}" | sed -e 's/^# \{0,1\}//' -e '/^$/d'
}

# Only the main worktree may manage worktrees. A linked worktree's git dir is
# <repo>/.git/worktrees/<name>, the main one is <repo>/.git.
require_main_worktree() {
  local git_dir
  git_dir="$(git -C "$REPO_ROOT" rev-parse --absolute-git-dir)"
  case "$git_dir" in
    */.git) return 0 ;;
    *) die "run this from the main worktree, not a linked one (git dir: $git_dir)" ;;
  esac
}

validate_name() {
  case "$1" in
    '') die "agent name is required" ;;
    *[!a-z0-9._-]*) die "agent name must be lowercase letters, digits, dot, dash or underscore: $1" ;;
    .*|..) die "invalid agent name: $1" ;;
    -*) die "agent name must not start with a dash: $1" ;;
  esac
}

resolve_base_branch() {
  if [ -n "${BASE_BRANCH:-}" ]; then
    echo "$BASE_BRANCH"
    return
  fi
  if git -C "$REPO_ROOT" show-ref --verify --quiet refs/heads/main; then
    echo "main"
  else
    git -C "$REPO_ROOT" symbolic-ref --short HEAD
  fi
}

# Seed the files that are gitignored, so they are NOT copied into a fresh
# worktree by git but are still needed there:
#   export-job-files/  ExportService writes to a CWD-relative path
#   .commandcode/       holds the learned taste; without it an agent starts blind
seed_worktree() {
  local wt="$1"

  mkdir -p "$wt/export-job-files"

  if [ -d "$REPO_ROOT/.commandcode/taste" ]; then
    mkdir -p "$wt/.commandcode"
    rm -rf "$wt/.commandcode/taste"
    cp -R "$REPO_ROOT/.commandcode/taste" "$wt/.commandcode/taste"
  fi

  if [ -f "$REPO_ROOT/.commandcode/settings.json" ]; then
    mkdir -p "$wt/.commandcode"
    cp "$REPO_ROOT/.commandcode/settings.json" "$wt/.commandcode/settings.json"
  fi
}

cmd_add() {
  validate_name "$1"
  local name="$1"
  local branch="$BRANCH_PREFIX/$name"
  local wt="$WT_ROOT/$name"
  local base
  base="$(resolve_base_branch)"

  if git -C "$REPO_ROOT" show-ref --verify --quiet "refs/heads/$branch"; then
    if git -C "$REPO_ROOT" worktree list --porcelain | grep -q "^worktree $wt\$"; then
      echo "worktree $wt already exists on branch $branch"
      return 0
    fi
    die "branch $branch already exists (and is not checked out in $wt)"
  fi

  if [ -e "$wt" ]; then
    die "path already exists: $wt"
  fi

  mkdir -p "$WT_ROOT"

  # The explicit start point matters: without it git forks the new branch from
  # the CURRENT worktree's HEAD, which silently branches off a stale commit.
  # -b checks out the new branch, not $base, so $base may live in another worktree.
  git -C "$REPO_ROOT" worktree add -b "$branch" "$wt" "$base"

  seed_worktree "$wt"

  echo
  echo "worktree : $wt"
  echo "branch   : $branch"
  echo "base     : $base"
  echo
  echo "launch   : cd $wt && cmd"
}

cmd_remove() {
  validate_name "$1"
  local name="$1"
  local branch="$BRANCH_PREFIX/$name"
  local wt="$WT_ROOT/$name"

  if [ -d "$wt" ]; then
    git -C "$REPO_ROOT" worktree remove --force "$wt"
    echo "removed worktree $wt"
  else
    echo "no worktree at $wt"
  fi

  # The branch must go AFTER the worktree: git refuses to delete a branch that
  # is still checked out somewhere.
  if git -C "$REPO_ROOT" show-ref --verify --quiet "refs/heads/$branch"; then
    git -C "$REPO_ROOT" branch -D "$branch"
    echo "deleted branch $branch"
  fi

  git -C "$REPO_ROOT" worktree prune
}

cmd_sync() {
  local wt=""
  local synced=0

  while IFS= read -r line; do
    case "$line" in
      "worktree "*) wt="${line#worktree }" ;;
      "")
        if [ -n "$wt" ] && [ "$wt" != "$REPO_ROOT" ]; then
          seed_worktree "$wt"
          echo "synced $wt"
          synced=$((synced + 1))
        fi
        wt=""
        ;;
    esac
  done < <(git -C "$REPO_ROOT" worktree list --porcelain)

  if [ -n "$wt" ] && [ "$wt" != "$REPO_ROOT" ]; then
    seed_worktree "$wt"
    echo "synced $wt"
    synced=$((synced + 1))
  fi

  echo "$synced worktree(s) synced"
}

cmd_list() {
  printf '%-46s %-28s %s\n' "WORKTREE" "BRANCH" "DIRTY"

  local wt="" branch="" head=""
  flush() {
    [ -z "$wt" ] && return 0
    local dirty
    dirty="$(git -C "$wt" status --porcelain 2>/dev/null | wc -l | tr -d ' ')"
    printf '%-46s %-28s %s\n' "$wt" "$branch" "$dirty"
  }

  while IFS= read -r line; do
    case "$line" in
      "worktree "*) wt="${line#worktree }"; branch="(detached)"; head="" ;;
      "HEAD "*) head="${line#HEAD }" ;;
      "branch "*) branch="${line#branch refs/heads/}" ;;
      "") flush; wt=""; branch=""; head="" ;;
    esac
  done < <(git -C "$REPO_ROOT" worktree list --porcelain)

  flush
}

case "${1:-}" in
  add) require_main_worktree; cmd_add "${2:-}" ;;
  remove | rm) require_main_worktree; cmd_remove "${2:-}" ;;
  sync) require_main_worktree; cmd_sync ;;
  list | ls) require_main_worktree; cmd_list ;;
  "" | -h | --help | help) usage ;;
  *) usage; die "unknown command: $1" ;;
esac
