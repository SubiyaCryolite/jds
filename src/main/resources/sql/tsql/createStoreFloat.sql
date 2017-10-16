CREATE TABLE JdsStoreFloat(
	FieldId         BIGINT,
	EntityGuid      NVARCHAR(48) NOT NULL,
	Value           REAL,
	PRIMARY KEY (FieldId,EntityGuid),
	CONSTRAINT fk_JdsStoreFloat_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);