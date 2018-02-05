CREATE PROCEDURE proc_store_float(IN p_composite_key VARCHAR(128), IN p_field_id BIGINT, IN p_value FLOAT)
  BEGIN
    INSERT INTO jds_store_float (composite_key, field_id, value)
    VALUES (p_composite_key, p_field_id, p_value)
    ON DUPLICATE KEY UPDATE value = p_value;
  END