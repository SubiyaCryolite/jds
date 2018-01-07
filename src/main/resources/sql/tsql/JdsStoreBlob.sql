CREATE TABLE JdsStoreBlob(
	FieldId         BIGINT,
	Uuid      NVARCHAR(96) NOT NULL,
	Value           VARBINARY(MAX),
	PRIMARY KEY (FieldId,Uuid),
	CONSTRAINT fk_JdsStoreBlob_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);