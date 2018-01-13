CREATE PROCEDURE proc_store_integer(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE INT)
  BEGIN
    INSERT INTO jds_store_integer (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END