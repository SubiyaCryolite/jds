CREATE PROCEDURE procStoreDouble(pUuid IN NVARCHAR2, pFieldId IN NUMBER, pValue BINARY_DOUBLE)
AS
BEGIN
	MERGE INTO JdsStoreDouble dest
	USING DUAL ON (pUuid = Uuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(Uuid,FieldId,Value)   VALUES(pUuid,  pFieldId, pValue);
END procStoreDouble;