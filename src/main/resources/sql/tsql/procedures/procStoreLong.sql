CREATE PROCEDURE procStoreLong(@Uuid NVARCHAR(96), @FieldId BIGINT, @Value BIGINT)
AS
BEGIN
	MERGE JdsStoreLong AS dest
	USING (VALUES (@Uuid,  @FieldId, @Value)) AS src([Uuid],  [FieldId], [Value])
	ON (src.Uuid = dest.Uuid AND src.FieldId = dest.FieldId)
	WHEN MATCHED THEN
		UPDATE SET dest.[Value] = src.[Value]
	WHEN NOT MATCHED THEN
		INSERT([Uuid],[FieldId],[Value])   VALUES(src.Uuid,  src.FieldId, src.Value);
END