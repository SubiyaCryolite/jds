CREATE TABLE JdsStoreZonedDateTime(
	FieldId         BIGINT,
	EntityGuid      NVARCHAR(48) NOT NULL,
	Value           DATETIMEOFFSET(7),
	PRIMARY KEY (FieldId,EntityGuid),
	CONSTRAINT fk_JdsStoreZonedDateTime_ParentEntityGuid FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);