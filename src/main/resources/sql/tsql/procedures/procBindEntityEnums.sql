CREATE PROCEDURE procBindEntityEnums(@EntityId BIGINT, @FieldId BIGINT)
AS
BEGIN
	MERGE JdsBindEntityEnums AS dest
	USING (VALUES (@EntityId,@FieldId)) AS src([EntityId],[FieldId])
	ON (src.EntityId = dest.EntityId AND src.FieldId = dest.FieldId)
	WHEN NOT MATCHED THEN
		INSERT([EntityId], [FieldId]) VALUES(src.EntityId, src.FieldId);
END