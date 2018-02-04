CREATE PROCEDURE proc_ref_enum(IN p_field_id BIGINT, IN p_seq INT, IN p_caption TEXT)
  BEGIN
    INSERT INTO jds_ref_enum (field_id, seq, caption)
    VALUES (p_field_id, p_seq, p_caption)
    ON DUPLICATE KEY UPDATE caption = p_caption;
  END