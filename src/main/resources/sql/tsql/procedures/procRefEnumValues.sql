CREATE PROCEDURE procRefEnumValues(@FieldId BIGINT, @EnumSeq INTEGER, @EnumValue NVARCHAR(MAX))
AS
BEGIN
	MERGE JdsEnums AS dest
	USING (VALUES (@FieldId,@EnumSeq,@EnumValue)) AS src([FieldId],[EnumSeq],[EnumValue])
	ON (src.FieldId = dest.FieldId AND src.EnumSeq = dest.EnumSeq)
	WHEN NOT MATCHED THEN
		INSERT([FieldId], [EnumSeq], [EnumValue]) VALUES(src.FieldId, src.EnumSeq, src.EnumValue)
	WHEN MATCHED THEN
		UPDATE SET dest.[EnumValue] = src.[EnumValue];
END