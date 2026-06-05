#!/usr/bin/env bash
set -u

strict="${MLP_SOLVER_TOOLCHAIN_STRICT:-false}"
failed=0

version_line() {
  local command_name="$1"
  shift
  local output

  if ! output="$("$@" 2>&1)"; then
    printf 'unavailable: version command failed for %s' "${command_name}"
    return 1
  fi

  printf '%s' "${output}" | awk 'NF {print; exit}'
}

report_command() {
  local solver="$1"
  local command_name="$2"
  shift 2
  local path
  local version

  if ! path="$(command -v "${command_name}" 2>/dev/null)"; then
    printf 'solver=%s command=%s path=unavailable version=unavailable diagnostic="command not found"\n' \
      "${solver}" "${command_name}"
    failed=1
    return
  fi

  if ! version="$(version_line "${command_name}" "$@")"; then
    printf 'solver=%s command=%s path=%s version=unavailable diagnostic="version command failed"\n' \
      "${solver}" "${command_name}" "${path}"
    failed=1
    return
  fi

  printf 'solver=%s command=%s path=%s version="%s" diagnostic=available\n' \
    "${solver}" "${command_name}" "${path}" "${version}"
}

printf 'solver-toolchain strict=%s\n' "${strict}"
report_command highs highs highs --version
report_command clp clp clp -stop
report_command glpk glpsol glpsol --version
printf 'solver=ortools-java dependency=com.google.ortools:ortools-java:9.15.6755 source=gradle\n'
printf 'solver=ojalgo dependency=org.ojalgo:ojalgo:56.2.1 source=gradle\n'
printf 'solver=simple source=in-project\n'
printf 'solver=performance source=in-project\n'

if [[ "${strict}" == "true" && "${failed}" -ne 0 ]]; then
  exit 1
fi
