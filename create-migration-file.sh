#!/usr/bin/env bash

REL_DIR=`dirname $0`
MIGRATION_DIR="common/src/main/resources/db/changelog/tables"
echo $REL_DIR
if [ $REL_DIR != "." ]  ; then
  echo "This script should be run from the navigator directory, like: ./create-migration-file.sh '<Description_with_underscore_separator>'"
  exit 1
fi

function generate_migration_name {
  echo "$MIGRATION_DIR/V`date +%s`000_$(( $RANDOM % 99 ))__`echo "$1" | awk '{print tolower($0)}'`.sql"
}

if [ "$#" -ne 1 ]; then
  echo
  echo "Usage: $0 <Description_with_underscore_separator>"
  echo
  echo "For example,"
  echo " $0 add_new_table"
  echo
  echo "would create the migration:"
  echo "  `generate_migration_name add_new_table`"
  exit 1
fi

new_migration=`generate_migration_name $1`
touch $new_migration

echo
echo "Created migration $new_migration"
echo