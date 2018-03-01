CREATE PROCEDURE proc_store_time(IN p_composite_key VARCHAR(128),
                                 IN p_field_id      BIGINT,
                                 IN p_sequence      INT,
                                 IN p_value         TIME)
  BEGIN
    INSERT INTO jds_store_time (composite_key, field_id, sequence, value)
    VALUES (p_composite_key, p_field_id, p_sequence, p_value)
    ON DUPLICATE KEY UPDATE value = p_value;
  END