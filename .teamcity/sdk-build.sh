#!/usr/bin/env bash
set -euo pipefail

checkout_dir="$PWD"
temp_dir="$(mktemp -d "$1/sdk-build.XXXXXXXX")"
dist_dir="$checkout_dir/sdk-dist/$2"
mkdir -p "$dist_dir"

uv build --package deltares-ai-platform-sdk --out-dir "$dist_dir"
uv export --frozen --package deltares-ai-platform-sdk --no-default-groups \
    --no-emit-workspace --no-hashes --output-file "$temp_dir/constraints.txt" > /dev/null
uv venv --python 3.12 "$temp_dir/venv"
uv pip install --python "$temp_dir/venv/bin/python" --constraint "$temp_dir/constraints.txt" \
    "$dist_dir"/*.whl "pytest>=8,<9"

cd "$temp_dir"
"$temp_dir/venv/bin/python" - <<'PY'
import importlib
import pkgutil
from pathlib import Path

import deltares_ai_platform_sdk
import optuna

package_path = Path(deltares_ai_platform_sdk.__file__).resolve()
assert package_path.is_relative_to(Path.cwd()), package_path
for module in pkgutil.iter_modules(deltares_ai_platform_sdk.__path__):
    importlib.import_module(f"{deltares_ai_platform_sdk.__name__}.{module.name}")
    print(f"Imported {module.name}")
PY
"$temp_dir/venv/bin/python" -m pytest --import-mode=importlib \
    "$checkout_dir/packages/deltares_ai_platform_sdk/tests" \
    --junitxml="$dist_dir/test-results.xml"
