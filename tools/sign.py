#!/usr/bin/env python3
"""
Signs extensions/index.json with the store's private key.

    python3 tools/sign.py /path/to/nijatva-store-signing-key.pem

Writes extensions/index.json.sig. Run this after ANY change to index.json,
or the browser will refuse the list. Needs `pip install cryptography`.
The private key never goes into the repository.
"""
import base64, pathlib, sys

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec

if len(sys.argv) < 2:
    print(__doc__)
    sys.exit(2)

root = pathlib.Path(__file__).resolve().parent.parent
index = root / "extensions" / "index.json"
sig_path = root / "extensions" / "index.json.sig"

key = serialization.load_pem_private_key(pathlib.Path(sys.argv[1]).read_bytes(), password=None)
body = index.read_bytes()
sig = key.sign(body, ec.ECDSA(hashes.SHA256()))
sig_path.write_text(base64.b64encode(sig).decode() + "\n")
print(f"signed {index.name}: {len(body)} bytes -> {sig_path.name}")
