CREATE TABLE JdsStoreDouble(
	FieldId         BIGINT,
	EntityGuid      NVARCHAR(48) NOT NULL,
	Value           FLOAT,
	PRIMARY KEY (FieldId,EntityGuid),
	CONSTRAINT fk_JdsStoreDouble_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);