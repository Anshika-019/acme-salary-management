package com.acme.salary.insights;

/** Optional scope for every insight; null or blank values are ignored. */
public record InsightsFilter(String countryCode, String department, String jobTitle) {

    public static InsightsFilter none() {
        return new InsightsFilter(null, null, null);
    }
}
