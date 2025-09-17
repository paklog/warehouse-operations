package com.paklog.warehouse.domain.licenseplate;

public interface LicensePlateGenerator {
    LicensePlateId generateLicensePlate();
    
    LicensePlateId generateLicensePlate(String prefix);
}

