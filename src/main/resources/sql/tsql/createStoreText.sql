CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	EntityGuid  NVARCHAR(48),
	Value       NVARCHAR(MAX),
	PRIMARY KEY (FieldId,EntityGuid)
);
ALTER TABLE JdsStoreText ADD CONSTRAINT fk_JdsStoreText_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE;