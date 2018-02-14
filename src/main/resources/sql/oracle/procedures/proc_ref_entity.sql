CREATE PROCEDURE proc_ref_entity(p_id IN       NUMBER,
                                 p_name        NVARCHAR2,
                                 p_caption     NVARCHAR2,
                                 p_description NVARCHAR2,
                                 p_parent      NUMBER)
AS
  BEGIN
    MERGE INTO jds_ref_entity dest
    USING DUAL
    ON (p_id = id)
    WHEN NOT MATCHED THEN
      INSERT (id, name, caption, description, parent) VALUES (p_id, p_name, p_caption, p_description, p_parent)
    WHEN MATCHED THEN
      UPDATE SET name = p_name, caption = p_caption, description = p_description, parent = p_parent;
  END proc_ref_entity;