CREATE PROCEDURE proc_ref_entity(IN p_id          BIGINT,
                                 IN p_name        VARCHAR(64),
                                 IN p_caption     VARCHAR(64),
                                 IN p_description VARCHAR(256),
                                 IN p_parent      BOOLEAN)
  BEGIN
    INSERT INTO jds_ref_entity (id, name, caption, description, parent)
    VALUES (p_id, p_name, p_caption, p_description, p_parent)
    ON DUPLICATE KEY UPDATE name = p_name, caption = p_caption, description = p_description, parent = p_parent;
  END