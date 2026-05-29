-- Campus Trade Database Migration
-- Add phone column to user table
-- Date: 2026-05-27

USE campus_trade;

-- Add phone column to user table if it doesn't exist
SET @dbname = DATABASE();
SET @tablename = 'user';
SET @columnname = 'phone';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(20) DEFAULT NULL COMMENT "Phone Number" AFTER `college`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Verify the column exists
SELECT 'Phone column added successfully' AS status;
DESCRIBE `user`;