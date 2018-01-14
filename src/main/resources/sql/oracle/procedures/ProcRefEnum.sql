CREATE PROCEDURE proc_ref_enum(p_field_id IN NUMBER, p_seq NUMBER, p_caption IN NCLOB)
AS
  BEGIN
    MERGE INTO jds_ref_enum dest
    USING DUAL
    ON (p_field_id = field_id AND p_seq = seq)
    WHEN NOT MATCHED THEN
      INSERT (field_id, seq, caption) VALUES (p_field_id, p_seq, p_caption)
    WHEN MATCHED THEN
      UPDATE SET caption = p_caption;
  END proc_ref_enum;