package com.nivasafinance.common.configs;

import org.springframework.context.annotation.Configuration;

/**
 * Javers configuration placeholder.
 * 
 * The duplicate key violation issue in jv_snapshot table is fixed via
 * Liquibase migration (V1764000000000_1__fix_javers_snapshot_sequence.xml)
 * which removes the sequence multiplier (nextval * 100) from the default value.
 * 
 * The Javers Spring Boot starter auto-configures Javers, so no additional
 * Java configuration is needed. The database migration handles the fix.
 */
@Configuration
public class JaversConfig {
    // Configuration handled by Javers Spring Boot starter
    // Database migration fixes the sequence multiplier issue
}

