#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
download=false
manifest="$repo_root/instances/public/manifest.example.json"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --download)
      download=true
      shift
      ;;
    --help)
      cat <<'MSG'
Usage: tools/fetch-public-benchmarks.sh [--download] [manifest]

Validates the public benchmark manifest. With --download, downloads non-rejected
MPS candidates to their ignored instances/public local paths and verifies
SHA-256 checksums.
MSG
      exit 0
      ;;
    *)
      manifest="$1"
      shift
      ;;
  esac
done

if [[ ! -f "$manifest" ]]; then
  echo "manifest not found: $manifest" >&2
  exit 1
fi

python3 - "$repo_root" "$manifest" "$download" <<'PY'
import hashlib
import json
from pathlib import Path
import sys
import time
from urllib.error import URLError
from urllib.request import urlopen

repo_root = Path(sys.argv[1])
manifest_path = Path(sys.argv[2])
download = sys.argv[3] == "true"

with manifest_path.open("r", encoding="utf-8") as handle:
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
download_attempts = 3
download_timeout_seconds = 30


def validate_instance(index, instance):
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
    checksum = str(instance["sha256"])
    if checksum != "pending-local-download" and len(checksum) != 64:
        raise SystemExit(f"{instance_id} sha256 must be 64 hex characters or pending-local-download")


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def download_bytes(instance):
    instance_id = instance["id"]
    url = instance["upstreamUrl"]
    for attempt in range(1, download_attempts + 1):
        try:
            with urlopen(url, timeout=download_timeout_seconds) as response:
                return response.read()
        except (TimeoutError, URLError) as error:
            if attempt == download_attempts:
                raise SystemExit(
                    f"{instance_id}: download failed after "
                    f"{download_attempts} attempts: {error}"
                ) from error
            print(
                f"{instance_id}: download attempt {attempt} failed: {error}; retrying",
                file=sys.stderr,
            )
            time.sleep(min(2 ** attempt, 10))


for index, instance in enumerate(data["instances"]):
    validate_instance(index, instance)

print(f"validated {len(data['instances'])} public benchmark manifest entries")

for instance in data["instances"]:
    instance_id = instance["id"]
    if instance["status"] == "rejected" or instance["format"] != "MPS":
        print(f"{instance_id}: skipped status={instance['status']} format={instance['format']}")
        continue

    local_path = repo_root / instance["localPath"]
    expected = instance["sha256"]
    if download:
        local_path.parent.mkdir(parents=True, exist_ok=True)
        local_path.write_bytes(download_bytes(instance))
        print(f"{instance_id}: downloaded {instance['upstreamUrl']} -> {local_path}")

    if not local_path.exists():
        print(f"{instance_id}: missing {local_path}")
        continue

    actual = sha256(local_path)
    if expected == "pending-local-download":
        raise SystemExit(f"{instance_id}: manifest sha256 is pending; actual={actual}")
    if actual != expected:
        raise SystemExit(f"{instance_id}: checksum mismatch expected={expected} actual={actual}")
    print(f"{instance_id}: verified sha256={actual}")
PY
