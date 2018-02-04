CREATE PROCEDURE proc_ref_entity(IN p_id BIGINT, IN p_caption TEXT)
  BEGIN
    INSERT INTO jds_ref_entity (id, caption)
    VALUES (p_id, p_caption)
    ON DUPLICATE KEY UPDATE caption = p_caption;
  END