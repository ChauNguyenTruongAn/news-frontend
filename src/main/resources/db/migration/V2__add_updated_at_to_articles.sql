ALTER TABLE Articles ADD COLUMN updated_at DATETIME;

UPDATE Articles SET updated_at = created_at WHERE updated_at IS NULL; 