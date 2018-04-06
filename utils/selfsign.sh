#!/bin/bash
openssl req -x509 -nodes -days 365 \
    -subj  "/CN=localhost" \
    -newkey rsa:2048 \
    -keyout "selfsigned.key" \
    -out "selfsigned.crt"
