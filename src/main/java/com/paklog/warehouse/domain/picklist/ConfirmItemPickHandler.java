package com.paklog.warehouse.domain.picklist;

import com.paklog.warehouse.domain.picklist.PickList;
import com.paklog.warehouse.domain.shared.BinLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmItemPickHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConfirmItemPickHandler.class);
    
    private final PickListRepository pickListRepository;

    public ConfirmItemPickHandler(PickListRepository pickListRepository) {
        this.pickListRepository = pickListRepository;
    }

    public void handle(ConfirmItemPick command) {
        logger.info("Processing item pick confirmation for pick list: {}, SKU: {}, quantity: {}", 
                   command.getPickListId(), command.getSku(), command.getQuantity());
        
        try {
            // Fetch the PickList
            PickList pickList = pickListRepository.findById(command.getPickListId());
            
            if (pickList == null) {
                logger.error("Pick list not found: {}", command.getPickListId());
                throw new IllegalArgumentException("Pick list not found: " + command.getPickListId());
            }
            
            // Confirm the item pick
            pickList.pickItem(
                command.getSku(), 
                command.getQuantity(), 
                command.getBinLocation()
            );
            
            // Save the updated PickList
            pickListRepository.save(pickList);
            
            logger.info("Item pick confirmed successfully for pick list: {}, SKU: {}", 
                       command.getPickListId(), command.getSku());
            
        } catch (Exception e) {
            logger.error("Failed to confirm item pick for pick list: {}, SKU: {}", 
                        command.getPickListId(), command.getSku(), e);
            throw e;
        }
    }
}