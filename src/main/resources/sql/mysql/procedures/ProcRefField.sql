CREATE PROCEDURE proc_ref_field(IN p_id           BIGINT,
                                IN p_caption      VARCHAR(128),
                                IN p_description  VARCHAR(256),
                                IN p_type_ordinal INT)
  BEGIN
    INSERT INTO jds_ref_field (id, caption, description, type_ordinal)
    VALUES (p_id, p_caption, p_description, p_type_ordinal)
    ON DUPLICATE KEY UPDATE caption = p_caption, description = p_description, type_ordinal = p_type_ordinal;
  END