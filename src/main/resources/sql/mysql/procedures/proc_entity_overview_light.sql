CREATE PROCEDURE proc_entity_overview_light(IN p_composite_key VARCHAR(128))
  BEGIN
    INSERT IGNORE INTO jds_entity_overview_light (composite_key)
    VALUES (p_composite_key);
  END