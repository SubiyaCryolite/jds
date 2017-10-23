CREATE PROCEDURE procStoreBoolean(@EntityGuid NVARCHAR(48), @FieldId BIGINT, @Value BIT)
AS
BEGIN
	MERGE JdsStoreBoolean AS dest
	USING (VALUES (@EntityGuid,  @FieldId, @Value)) AS src([EntityGuid],  [FieldId], [Value])
	ON (src.EntityGuid = dest.EntityGuid AND src.FieldId = dest.FieldId)
	WHEN MATCHED THEN
		UPDATE SET dest.[Value] = src.[Value]
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid],[FieldId],[Value])   VALUES(src.EntityGuid,  src.FieldId, src.Value);
END