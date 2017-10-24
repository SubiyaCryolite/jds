--Based on https://www.mssqltips.com/sqlservertip/2733/solving-the-sql-server-multiple-cascade-path-issue-with-a-trigger/
CREATE TRIGGER [triggerEntityBindingCascade]
	ON [JdsEntityOverview]
	INSTEAD OF DELETE
AS
BEGIN
 SET NOCOUNT ON;
 DELETE FROM [JdsEntityBinding] WHERE [ChildUuid] IN		(SELECT [Uuid] FROM DELETED)
 DELETE FROM [JdsEntityBinding] WHERE [ParentUuid] IN	(SELECT [Uuid] FROM DELETED)
 DELETE FROM [JdsEntityOverview] WHERE [Uuid] IN			(SELECT [Uuid] FROM DELETED)
END