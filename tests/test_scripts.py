"""
tests/test_scripts.py
Unit tests for the Python scripts layer.
Run with: pytest tests/ -v
"""
import sys
import os
import pytest

# Allow importing from scripts/
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "scripts"))


class TestDependencyVersions:
    """
    Verify that the installed versions of vulnerable packages match
    the pinned versions in requirements.txt (i.e. confirm we're testing
    the vulnerable state, not some upgraded version).
    """

    def test_requests_version_is_vulnerable(self):
        import requests
        major, minor, patch = requests.__version__.split(".")[:3]
        # CVE-2023-32681 fixed in 2.31.0 — we should be on 2.28.x
        assert int(major) == 2
        assert int(minor) < 31, (
            f"requests {requests.__version__} may have CVE-2023-32681 patched. "
            "Expected < 2.31.0 for demo purposes."
        )

    def test_flask_version_is_vulnerable(self):
        import flask
        major, minor = flask.__version__.split(".")[:2]
        # CVE-2023-30861 fixed in 2.3.2 — we should be on 2.2.x
        assert int(major) == 2
        assert int(minor) < 3, (
            f"Flask {flask.__version__} may have CVE-2023-30861 patched."
        )

    def test_pyjwt_version_is_vulnerable(self):
        import jwt
        major, minor = jwt.__version__.split(".")[:2]
        # CVE-2022-29217 fixed in 2.4.0 — we should be on 2.3.x
        assert int(major) == 2
        assert int(minor) < 4, (
            f"PyJWT {jwt.__version__} may have CVE-2022-29217 patched."
        )

    def test_urllib3_version_is_vulnerable(self):
        import urllib3
        parts = urllib3.__version__.split(".")
        major, minor, patch = int(parts[0]), int(parts[1]), int(parts[2])
        # CVE-2023-43804 fixed in 1.26.17
        assert major == 1 and minor == 26 and patch < 17, (
            f"urllib3 {urllib3.__version__} may have CVE-2023-43804 patched."
        )


class TestTokenGeneration:
    """Tests for the api_client token generation (PyJWT CVE-2022-29217 demo)."""

    def test_token_encodes_and_decodes(self):
        import jwt as pyjwt
        payload = {"sub": "testuser", "role": "admin"}
        secret = "test-secret"
        token = pyjwt.encode(payload, secret, algorithm="HS256")
        decoded = pyjwt.decode(token, secret, algorithms=["HS256"])
        assert decoded["sub"] == "testuser"
        assert decoded["role"] == "admin"

    def test_token_is_string(self):
        import jwt as pyjwt
        token = pyjwt.encode({"sub": "user"}, "secret", algorithm="HS256")
        # PyJWT 2.x returns str, 1.x returned bytes
        assert isinstance(token, str)


class TestMarkdownRendering:
    """Tests for mistune rendering (CVE-2022-34749 ReDoS demo)."""

    def test_basic_markdown_renders(self):
        import mistune
        html = mistune.markdown("# Hello\n\nWorld")
        assert "<h1>" in html
        assert "Hello" in html

    def test_table_renders(self):
        import mistune
        md = "| A | B |\n|---|---|\n| 1 | 2 |"
        html = mistune.markdown(md)
        # mistune 0.8.4 doesn't render GFM tables by default — just confirm no crash
        assert html is not None


class TestRequirementsFiles:
    """Validate that both requirements files exist and list expected packages."""

    def _load_req(self, filename):
        base = os.path.join(os.path.dirname(__file__), "..")
        path = os.path.join(base, filename)
        with open(path) as f:
            return f.read()

    def test_vulnerable_requirements_exists(self):
        content = self._load_req("requirements.txt")
        assert "requests==2.28.2" in content
        assert "Flask==2.2.5" in content
        assert "PyJWT==2.3.0" in content

    def test_fixed_requirements_exists(self):
        content = self._load_req("requirements-fixed.txt")
        assert "requests==2.31.0" in content
        assert "Flask==2.3.3" in content
        assert "PyJWT==2.8.0" in content

    def test_fixed_versions_are_higher(self):
        """Sanity check: every fixed version should be >= vulnerable version."""
        from packaging.version import Version

        pairs = [
            ("2.28.2", "2.31.0"),   # requests
            ("65.3.0", "68.0.0"),   # setuptools
            ("3.8.1",  "3.8.6"),    # aiohttp
            ("1.10.0", "1.11.0"),   # py
            ("1.21.0", "1.24.4"),   # numpy
            ("0.8.4",  "2.0.5"),    # mistune
            ("7.29.0", "8.12.3"),   # ipython
            ("2.2.5",  "2.3.3"),    # Flask
            ("2.3.0",  "2.8.0"),    # PyJWT
            ("1.26.14","1.26.18"),  # urllib3
        ]
        for vuln, fixed in pairs:
            assert Version(fixed) > Version(vuln), (
                f"Fixed version {fixed} should be > vulnerable version {vuln}"
            )
