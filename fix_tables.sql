USE inventory_management_system_database;

-- Drop existing trigger and constraints
DROP TRIGGER IF EXISTS before_insert_sale_offtake;
ALTER TABLE stock_onhand DROP FOREIGN KEY stock_onhand_ibfk_1;

-- Set DEFINER to current user and disable strict mode
SET GLOBAL log_bin_trust_function_creators = 1;
SET GLOBAL sql_mode='';
SET SESSION sql_mode='';

DELIMITER //

-- Create trigger for sale_offtake that handles both tables
CREATE TRIGGER before_insert_sale_offtake 
BEFORE INSERT ON sale_offtake 
FOR EACH ROW 
BEGIN
    DECLARE random_id INT;
    DECLARE exists_id INT;
    
    -- Generate unique random ID
    REPEAT
        SET random_id = FLOOR(1000000 + RAND() * 9000000);
        SELECT COUNT(*) INTO exists_id FROM sale_offtake WHERE item_code = random_id;
    UNTIL exists_id = 0
    END REPEAT;
    
    -- Set the new item_code
    SET NEW.item_code = random_id;
    
    -- Initialize all monthly columns to 0 if they are NULL
    IF NEW.jan IS NULL THEN SET NEW.jan = 0; END IF;
    IF NEW.feb IS NULL THEN SET NEW.feb = 0; END IF;
    IF NEW.mar IS NULL THEN SET NEW.mar = 0; END IF;
    IF NEW.apr IS NULL THEN SET NEW.apr = 0; END IF;
    IF NEW.may IS NULL THEN SET NEW.may = 0; END IF;
    IF NEW.jun IS NULL THEN SET NEW.jun = 0; END IF;
    IF NEW.jul IS NULL THEN SET NEW.jul = 0; END IF;
    IF NEW.aug IS NULL THEN SET NEW.aug = 0; END IF;
    IF NEW.sep IS NULL THEN SET NEW.sep = 0; END IF;
    IF NEW.oct IS NULL THEN SET NEW.oct = 0; END IF;
    IF NEW.nov IS NULL THEN SET NEW.nov = 0; END IF;
    IF NEW.dec IS NULL THEN SET NEW.dec = 0; END IF;
END //

-- Create trigger to automatically insert into stock_onhand
CREATE TRIGGER after_insert_sale_offtake
AFTER INSERT ON sale_offtake
FOR EACH ROW
BEGIN
    INSERT INTO stock_onhand (
        item_code,
        jan1, feb1, mar1, apr1, may1, jun1,
        jul1, aug1, sep1, oct1, nov1, dec1
    ) VALUES (
        NEW.item_code,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0
    );
END //

DELIMITER ;

-- Recreate the foreign key constraint
ALTER TABLE stock_onhand
ADD CONSTRAINT stock_onhand_ibfk_1 
FOREIGN KEY (item_code) 
REFERENCES sale_offtake(item_code)
ON DELETE CASCADE
ON UPDATE CASCADE; 