USE inventory_management_system_database;

-- Drop existing trigger
DROP TRIGGER IF EXISTS before_insert_sale_offtake;

-- Set DEFINER to current user
SET GLOBAL log_bin_trust_function_creators = 1;
SET GLOBAL sql_mode='';
SET SESSION sql_mode='';

DELIMITER //

-- Recreate trigger
CREATE TRIGGER before_insert_sale_offtake 
BEFORE INSERT ON sale_offtake 
FOR EACH ROW 
BEGIN
    DECLARE random_id INT;
    DECLARE exists_id INT;
    
    REPEAT
        SET random_id = FLOOR(1000000 + RAND() * 9000000); -- generates 7-digit number
        SELECT COUNT(*) INTO exists_id FROM sale_offtake WHERE item_code = random_id;
    UNTIL exists_id = 0
    END REPEAT;
    
    SET NEW.item_code = random_id;
END //

DELIMITER ; 