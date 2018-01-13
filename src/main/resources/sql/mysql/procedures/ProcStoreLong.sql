CREATE PROCEDURE proc_store_long(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE BIGINT)
  BEGIN
    INSERT INTO jds_store_long (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END