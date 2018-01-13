CREATE PROCEDURE proc_store_time(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE TIME)
  BEGIN
    INSERT INTO jds_store_time (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END