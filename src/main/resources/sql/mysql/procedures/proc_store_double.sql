CREATE PROCEDURE proc_store_double(IN p_composite_key VARCHAR(128),
                                   IN p_field_id      BIGINT,
                                   IN p_sequence      INT,
                                   IN p_value         DOUBLE)
  BEGIN
    INSERT INTO jds_store_double (composite_key, field_id, sequence, value)
    VALUES (p_composite_key, p_field_id, p_sequence, p_value)
    ON DUPLICATE KEY UPDATE value = p_value, sequence = p_sequence;
  END