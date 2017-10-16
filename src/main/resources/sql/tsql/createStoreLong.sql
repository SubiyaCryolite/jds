CREATE TABLE JdsStoreLong(
	FieldId     BIGINT,
	EntityGuid  NVARCHAR(48) NOT NULL,
	Value       BIGINT,
	PRIMARY KEY (FieldId,EntityGuid),
	CONSTRAINT fk_JdsStoreLong_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);