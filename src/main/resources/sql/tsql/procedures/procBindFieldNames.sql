CREATE PROCEDURE procBindFieldNames(@FieldId BIGINT, @FieldName NVARCHAR(MAX), @FieldDescription NVARCHAR(MAX))
AS
BEGIN
    MERGE JdsFields AS dest
    USING (VALUES (@FieldId,@FieldName,@FieldDescription)) AS src([FieldId],[FieldName],[FieldDescription])
    ON (src.FieldId = dest.FieldId)
    WHEN NOT MATCHED THEN
        INSERT([FieldId], [FieldName], [FieldDescription]) VALUES(src.FieldId, src.FieldName, src.FieldDescription)
    WHEN MATCHED THEN
        UPDATE SET dest.[FieldName] = src.[FieldName], dest.[FieldDescription] = src.[FieldDescription];
END