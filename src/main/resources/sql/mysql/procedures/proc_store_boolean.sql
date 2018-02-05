CREATE PROCEDURE proc_store_boolean(IN p_composite_key VARCHAR(128), IN p_field_id BIGINT, IN p_value BOOLEAN)
  BEGIN
    INSERT INTO jds_store_boolean (composite_key, field_id, value)
    VALUES (p_composite_key, p_field_id, p_value)
    ON DUPLICATE KEY UPDATE value = p_value;
  END