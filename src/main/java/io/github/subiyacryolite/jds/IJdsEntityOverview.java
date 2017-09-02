package io.github.subiyacryolite.jds;

import java.time.LocalDateTime;

public interface IJdsEntityOverview {
    /**
     * @return
     */
    public LocalDateTime getDateCreated();

    /**
     * @param dateCreated
     */
    public void setDateCreated(LocalDateTime dateCreated);

    /**
     * @return
     */
    public LocalDateTime getDateModified();

    /**
     * @param dateModified
     */
    public void setDateModified(LocalDateTime dateModified);

    /**
     * @return
     */
    public long getEntityId();

    /**
     * @param entityId
     */
    public void setEntityId(long entityId);

    /**
     * @return
     */
    public String getEntityGuid();

    /**
     * @param entityGuid
     */
    public void setEntityGuid(String entityGuid);
}
