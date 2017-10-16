CREATE TABLE JdsStoreBlob(
	FieldId         BIGINT,
	EntityGuid      NVARCHAR(48) NOT NULL,
	Value           VARBINARY(MAX),
	PRIMARY KEY (FieldId,EntityGuid),
	CONSTRAINT fk_JdsStoreBlob_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);