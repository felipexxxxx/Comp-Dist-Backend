SELECT 'CREATE DATABASE healthsys_identity'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'healthsys_identity')\gexec

SELECT 'CREATE DATABASE healthsys_patient'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'healthsys_patient')\gexec

SELECT 'CREATE DATABASE healthsys_triage'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'healthsys_triage')\gexec

SELECT 'CREATE DATABASE healthsys_notification'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'healthsys_notification')\gexec
