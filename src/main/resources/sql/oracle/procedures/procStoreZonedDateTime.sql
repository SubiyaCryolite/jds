CREATE PROCEDURE procStoreZonedDateTime(pUuid IN NVARCHAR2, pFieldId IN NUMBER, pValue IN TIMESTAMP WITH TIME ZONE)
AS
BEGIN
	MERGE INTO JdsStoreZonedDateTime dest
	USING DUAL ON (pUuid = Uuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(Uuid,FieldId,Value)   VALUES(pUuid,  pFieldId, pValue);
END procStoreZonedDateTime;