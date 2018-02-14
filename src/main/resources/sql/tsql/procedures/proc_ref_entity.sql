CREATE PROCEDURE proc_ref_entity(@id          BIGINT,
                                 @name        NVARCHAR(256),
                                 @caption     NVARCHAR(256),
                                 @description NVARCHAR(256),
                                 @parent      BIT)
AS
  BEGIN
    MERGE jds_ref_entity AS dest
    USING (VALUES (@id, @name, @caption, @description, @parent)) AS src(id, name, caption, description, parent)
    ON (src.id = dest.id)
    WHEN NOT MATCHED THEN
      INSERT (id, name, caption, description, parent)
      VALUES (src.id, src.name, src.caption, src.description, src.parent)
    WHEN MATCHED THEN
      UPDATE SET dest.name = src.name,
        dest.caption       = src.caption,
        dest.description   = src.description,
        dest.parent        = src.parent;
  END