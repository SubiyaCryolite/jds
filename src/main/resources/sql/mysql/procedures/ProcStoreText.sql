CREATE PROCEDURE proc_store_text(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE TEXT)
  BEGIN
    INSERT INTO jds_store_text (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END