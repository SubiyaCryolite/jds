CREATE TABLE JdsStoreDateTime(
	FieldId         BIGINT,
	EntityGuid      NVARCHAR(48) NOT NULL,
	Value           DATETIME,
	PRIMARY KEY (FieldId,EntityGuid),
	CONSTRAINT fk_JdsStoreDateTime_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);