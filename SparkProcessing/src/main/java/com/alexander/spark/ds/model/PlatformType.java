package com.alexander.spark.ds.model;

public enum PlatformType {

    // Message brokers / event streaming systems
    STREAMING,

    // Filesystem-based ingestion
    FILE_SYSTEM,

    // Lakehouse table formats
    TABLE,

    // Testing / development sources
    UTILITY
}