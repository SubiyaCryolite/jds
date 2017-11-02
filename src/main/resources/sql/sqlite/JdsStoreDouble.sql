CREATE TABLE JdsStoreDouble(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           DOUBLE,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);