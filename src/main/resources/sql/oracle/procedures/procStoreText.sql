CREATE PROCEDURE procStoreText(pUuid IN NVARCHAR2, pFieldId IN NUMBER, pValue IN NCLOB)
AS
BEGIN
	MERGE INTO JdsStoreText dest
	USING DUAL ON (pUuid = Uuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(Uuid,FieldId,Value)   VALUES(pUuid,  pFieldId, pValue);
END procStoreText;