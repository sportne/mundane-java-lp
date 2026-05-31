# LP instances

This directory is for local benchmark instances. Public and generated instances
are intentionally git-ignored unless a future ADR approves vendoring a small
fixture.

- `instances/public/`: local public benchmark downloads.
- `instances/generated/`: generated benchmark artifacts.

Committed files under ignored instance directories are metadata only. The
example public manifest at `instances/public/manifest.example.json` defines the
0.1.0 curation schema and candidate Netlib entries; actual downloaded benchmark
files remain local.
