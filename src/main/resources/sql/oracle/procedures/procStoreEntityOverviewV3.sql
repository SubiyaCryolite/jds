CREATE PROCEDURE procStoreEntityOverviewV3(pUuid IN NVARCHAR2, pDateCreated IN TIMESTAMP, pDateModified IN TIMESTAMP, pLive IN NUMBER, pVersion IN NUMBER)
AS
BEGIN
	MERGE INTO JdsEntityOverview dest
	USING DUAL ON (pUuid = Uuid)
	WHEN MATCHED THEN
		UPDATE SET DateModified = pDateModified, Live = pLive, Version = pVersion
	WHEN NOT MATCHED THEN
		INSERT(Uuid, DateCreated, DateModified, Live, Version) VALUES(pUuid, pDateCreated, pDateModified, pLive, pVersion);
END procStoreEntityOverviewV3;