CREATE PROCEDURE proc_store_double(IN p_composite_key VARCHAR(128), IN p_field_id BIGINT, IN p_value DOUBLE)
  BEGIN
    INSERT INTO jds_store_double (composite_key, field_id, value)
    VALUES (p_composite_key, p_field_id, p_value)
    ON DUPLICATE KEY UPDATE value = p_value;
  END