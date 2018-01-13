CREATE FUNCTION proc_bind_parent_to_child(pparent_entity_id BIGINT, pchild_entity_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity_inheritance (parent_entity_id, child_entity_id)
  VALUES (pparent_entity_id, pchild_entity_id)
  ON CONFLICT (parent_entity_id, child_entity_id)
    DO UPDATE SET parent_entity_id = pparent_entity_id, child_entity_id = pchild_entity_id;
END;
$$ LANGUAGE plpgsql;