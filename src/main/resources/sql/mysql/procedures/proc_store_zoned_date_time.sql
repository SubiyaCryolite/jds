CREATE PROCEDURE proc_store_zoned_date_time(IN p_composite_key VARCHAR(195), IN p_field_id BIGINT, IN p_value TIMESTAMP)
  BEGIN
    INSERT INTO jds_store_zoned_date_time (composite_key, field_id, value)
    VALUES (p_composite_key, p_field_id, p_value)
    ON DUPLICATE KEY UPDATE value = p_value;
  END