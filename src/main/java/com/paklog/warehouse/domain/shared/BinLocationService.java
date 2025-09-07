package com.paklog.warehouse.domain.shared;

import java.util.List;

public interface BinLocationService {
    /**
     * Find the nearest bin location for a given SKU
     * 
     * @param sku The Stock Keeping Unit to locate
     * @return The closest bin location for the given SKU
     */
    BinLocation findNearestBinLocation(SkuCode sku);

    /**
     * Calculate the distance between two bin locations
     * 
     * @param from The starting bin location
     * @param to The destination bin location
     * @return The distance between the two locations
     */
    double calculateDistance(BinLocation from, BinLocation to);

    /**
     * Get all bin locations for a specific SKU
     * 
     * @param sku The Stock Keeping Unit to find locations for
     * @return A list of bin locations containing the specified SKU
     */
    List<BinLocation> getBinLocationsForSku(SkuCode sku);

    /**
     * Check if a bin location is available for storing an item
     * 
     * @param binLocation The bin location to check
     * @return true if the bin location is available, false otherwise
     */
    boolean isBinLocationAvailable(BinLocation binLocation);
}