CREATE PROCEDURE proc_ref_entity(IN pid BIGINT, IN pcaption TEXT)
  BEGIN
    INSERT INTO jds_ref_entity (id, caption)
    VALUES (pid, pcaption)
    ON DUPLICATE KEY UPDATE caption = pcaption;
  END