#!/bin/bash
curl -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/policy:tenants" -X GET | json_pp; echo
