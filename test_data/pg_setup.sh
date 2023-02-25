#!/usr/bin/env bash
set -e
gunzip -c  /home/user_data_schema.sql.gz | PGPASSWORD=mypassword PGUSER=myuser psql it_data
gunzip -c  /home/user_data.sql.gz | PGPASSWORD=mypassword PGUSER=myuser psql it_data

#load data
PGPASSWORD=password psql -U myuser -d it_data <<-EOSQL
\copy user_details FROM '/home/sample_data/user_data.tsv' HEADER DELIMITER E'\t' CSV;
EOSQL

