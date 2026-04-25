#!/usr/bin/env python3
"""
scripts/api_client.py
---------------------
Simple API client and data-seeder for the vulnerable-demo-app.

Uses `requests` 2.28.2 — vulnerable to CVE-2023-32681 (Proxy-Authorization header
leak on cross-origin redirect).  Fix: upgrade to requests >= 2.31.0.

Uses `PyJWT` 2.3.0 — vulnerable to CVE-2022-29217 (algorithm confusion attack).
Fix: upgrade to PyJWT >= 2.4.0.
"""

import sys
import json
import argparse
import requests          # CVE-2023-32681 — Proxy-Authorization header leak
import jwt               # CVE-2022-29217 — algorithm confusion
import urllib3           # CVE-2023-43804 — cookie header leak

# Suppress only the InsecureRequestWarning for demo clarity
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

BASE_URL = "http://localhost:8080/api"
AUTH = ("admin", "admin123")


def health_check():
    """Check application health endpoint."""
    resp = requests.get(f"{BASE_URL}/health", auth=AUTH)
    print(f"Health: {resp.status_code} — {resp.json()}")


def list_products():
    """Fetch and print all products."""
    resp = requests.get(f"{BASE_URL}/products", auth=AUTH)
    products = resp.json()
    print(f"\n{'ID':<5} {'Name':<20} {'Category':<12} {'Price':>8}")
    print("-" * 50)
    for p in products:
        print(f"{p['id']:<5} {p['name']:<20} {p['category']:<12} {p['price']:>8.2f}")


def create_product(name: str, description: str, price: float, category: str):
    """Create a new product."""
    payload = {
        "name": name,
        "description": description,
        "price": price,
        "category": category,
    }
    resp = requests.post(f"{BASE_URL}/products", json=payload, auth=AUTH)
    print(f"Created: {resp.status_code} — {resp.json()}")


def generate_demo_token(username: str, secret: str = "demo-secret") -> str:
    """
    Generate a demo JWT.
    PyJWT 2.3.0 — CVE-2022-29217: a token signed with HS256 can be accepted
    as RS256 if the verifying code passes algorithms=None or uses the public
    key as the HMAC secret.  Fix: PyJWT >= 2.4.0 enforces strict algorithm.
    """
    payload = {"sub": username, "role": "admin"}
    token = jwt.encode(payload, secret, algorithm="HS256")
    print(f"Generated token for '{username}': {token}")
    return token


def seed_data():
    """Seed some extra products for demo."""
    products = [
        ("Sensor A",  "IoT temperature sensor",  29.99, "sensors"),
        ("Sensor B",  "IoT humidity sensor",      34.99, "sensors"),
        ("Bridge X",  "Network bridge device",   149.99, "network"),
        ("Controller","Industrial controller",   299.99, "industrial"),
    ]
    for name, desc, price, cat in products:
        create_product(name, desc, price, cat)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Demo App API Client")
    parser.add_argument("command", choices=["health", "list", "seed", "token"],
                        help="Command to run")
    parser.add_argument("--username", default="admin")
    args = parser.parse_args()

    if args.command == "health":
        health_check()
    elif args.command == "list":
        list_products()
    elif args.command == "seed":
        seed_data()
    elif args.command == "token":
        generate_demo_token(args.username)
