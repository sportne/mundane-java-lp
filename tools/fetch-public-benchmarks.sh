#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
manifest="${1:-"$repo_root/instances/public/manifest.example.json"}"

if [[ ! -f "$manifest" ]]; then
  echo "manifest not found: $manifest" >&2
  exit 1
fi

python3 - "$manifest" <<'PY'
import json
import sys

manifest_path = sys.argv[1]
with open(manifest_path, "r", encoding="utf-8") as handle:
    data = json.load(handle)

required_top = {"schemaVersion", "sourceSet", "instances"}
missing_top = sorted(required_top - set(data))
if missing_top:
    raise SystemExit(f"missing manifest keys: {', '.join(missing_top)}")
if data["schemaVersion"] != 1:
    raise SystemExit("schemaVersion must be 1")
if not isinstance(data["instances"], list) or not data["instances"]:
    raise SystemExit("instances must be a non-empty list")

required_instance = {
    "id",
    "family",
    "upstreamUrl",
    "licenseOrTerms",
    "downloadDate",
    "sha256",
    "format",
    "localPath",
    "normalization",
    "status",
}
ids = set()
for index, instance in enumerate(data["instances"]):
    missing = sorted(required_instance - set(instance))
    if missing:
        raise SystemExit(f"instance {index} missing keys: {', '.join(missing)}")
    instance_id = instance["id"]
    if not isinstance(instance_id, str) or not instance_id:
        raise SystemExit(f"instance {index} id must be a non-empty string")
    if instance_id in ids:
        raise SystemExit(f"duplicate instance id: {instance_id}")
    ids.add(instance_id)
    if not str(instance["upstreamUrl"]).startswith("https://"):
        raise SystemExit(f"{instance_id} upstreamUrl must be https")
    if not str(instance["localPath"]).startswith("instances/public/"):
        raise SystemExit(f"{instance_id} localPath must be under instances/public/")
    if instance["status"] not in {"candidate", "approved-local", "rejected"}:
        raise SystemExit(f"{instance_id} has unsupported status: {instance['status']}")

print(f"validated {len(data['instances'])} public benchmark manifest entries")
PY
