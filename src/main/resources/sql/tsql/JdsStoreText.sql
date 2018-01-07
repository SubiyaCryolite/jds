CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	Uuid  NVARCHAR(96),
	Value       NVARCHAR(MAX),
	PRIMARY KEY (FieldId,Uuid)
);
ALTER TABLE JdsStoreText ADD CONSTRAINT fk_JdsStoreText_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE;