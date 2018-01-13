CREATE PROCEDURE proc_ref_field(p_id           IN NUMBER,
                                p_caption      IN NVARCHAR2,
                                p_description  IN NVARCHAR2,
                                p_type_ordinal IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_ref_field dest
    USING DUAL
    ON (p_id = id)
    WHEN NOT MATCHED THEN
      INSERT (id, caption, description, type_ordinal) VALUES (p_id, p_caption, p_description, p_type_ordinal)
    WHEN MATCHED THEN
      UPDATE SET caption = p_caption, description = p_description, type_ordinal = p_type_ordinal;
  END proc_ref_field;