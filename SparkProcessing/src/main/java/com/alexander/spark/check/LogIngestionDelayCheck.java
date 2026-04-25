package com.alexander.spark.check;

public record LogIngestionDelayCheck(long maxDelayMillis) implements StreamCheck{
}
