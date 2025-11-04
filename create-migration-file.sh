#!/usr/bin/env bash

REL_DIR=`dirname $0`
MIGRATION_DIR="."
echo $REL_DIR
if [ $REL_DIR != "." ]  ; then
  echo "This script should be run from the navigator directory, like: ./create-migration-file.sh '<Description_with_underscore_separator>'"
  exit 1
fi

function generate_migration_name {
  echo "$MIGRATION_DIR/V`date +%s`000_$(( $RANDOM % 99 ))__`echo "$1" | awk '{print tolower($0)}'`.xml"
}

function generate_changeset_id {
  echo "V`date +%s`000_$(( $RANDOM % 99 ))"
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
changeset_id=`generate_changeset_id`

# Create XML file with Liquibase template
cat > "$new_migration" << EOF
<?xml version="1.0" encoding="UTF-8" ?>
<databaseChangeLog
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="$changeset_id" author="system" context="default">
        
        <!-- Add your database changes here -->
        
    </changeSet>

</databaseChangeLog>
EOF

echo
echo "Created migration $new_migration"
echo "Changeset ID: $changeset_id"
echo