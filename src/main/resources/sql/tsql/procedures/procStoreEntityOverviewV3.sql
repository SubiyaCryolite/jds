CREATE PROCEDURE procStoreEntityOverviewV3(@Uuid NVARCHAR(96), @DateCreated DATETIME, @DateModified DATETIME, @Live BIT, @Version BIGINT)
AS
BEGIN
	MERGE JdsEntityOverview AS dest
	USING (VALUES (@Uuid, @DateCreated, @DateModified, @Live, @Version)) AS src([Uuid], [DateCreated], [DateModified], [Live], [Version])
	ON (src.Uuid = dest.Uuid)
	WHEN MATCHED THEN
		UPDATE SET dest.[DateModified] = src.[DateModified], dest.[Live] = src.[Live], dest.[Version] = src.[Version]
	WHEN NOT MATCHED THEN
		INSERT([Uuid], [DateCreated], [DateModified], [Live], [Version]) VALUES(src.Uuid, src.DateCreated, src.DateModified, src.Live, src.Version);
END