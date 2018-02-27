CREATE PROCEDURE proc_store_text(p_composite_key IN NVARCHAR2,
                                 p_field_id      IN NUMBER,
                                 p_sequence      IN NUMBER,
                                 p_value         IN NCLOB)
AS
  BEGIN
    MERGE INTO jds_store_text dest
    USING DUAL
    ON (p_composite_key = composite_key AND p_field_id = field_id)
    WHEN MATCHED THEN
      UPDATE SET value = p_value, sequence = p_sequence
    WHEN NOT MATCHED THEN
      INSERT (composite_key, field_id, sequence, value)   VALUES (p_composite_key, p_field_id, p_sequence, p_value);
  END proc_store_text;