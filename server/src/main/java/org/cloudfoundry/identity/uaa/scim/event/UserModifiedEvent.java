/*
 * ******************************************************************************
 *      Cloud Foundry
 *      Copyright (c) [2009-2016] Pivotal Software, Inc. All Rights Reserved.
 *
 *      This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *      You may not use this product except in compliance with the License.
 *
 *      This product includes a number of subcomponents with
 *      separate copyright notices and license terms. Your use of these
 *      subcomponents is subject to the terms and conditions of the
 *      subcomponent's license, as noted in the LICENSE file.
 * ******************************************************************************
 */

package org.cloudfoundry.identity.uaa.scim.event;

import org.cloudfoundry.identity.uaa.audit.AuditEvent;
import org.cloudfoundry.identity.uaa.audit.AuditEventType;
import org.cloudfoundry.identity.uaa.audit.event.AbstractUaaEvent;
import org.cloudfoundry.identity.uaa.authentication.UaaPrincipal;
import org.cloudfoundry.identity.uaa.scim.ScimUser;
import org.cloudfoundry.identity.uaa.util.JsonUtils;
import org.cloudfoundry.identity.uaa.zone.IdentityZoneHolder;

public class UserModifiedEvent extends AbstractUaaEvent {

    private static final long serialVersionUID = 8139998613071093676L;
    private final ScimUser scimUser;
    private final AuditEventType eventType;

    public UserModifiedEvent(ScimUser scimUser, AuditEventType eventType) {
        super(getContextAuthentication(), IdentityZoneHolder.getCurrentZoneId());
        this.scimUser = scimUser;
        this.eventType = eventType;
    }

    public static UserModifiedEvent userCreated(ScimUser scimUser) {
        return new UserModifiedEvent(scimUser, AuditEventType.UserCreatedEvent);
    }

    public static UserModifiedEvent userModified(ScimUser scimUser) {
        return new UserModifiedEvent(scimUser, AuditEventType.UserModifiedEvent);
    }

    public static UserModifiedEvent userDeleted(ScimUser scimUser) {
        return new UserModifiedEvent(scimUser, AuditEventType.UserDeletedEvent);
    }

    public static UserModifiedEvent userVerified(ScimUser scimUser) {
        return new UserModifiedEvent(scimUser, AuditEventType.UserVerifiedEvent);
    }

    public static UserModifiedEvent emailChanged(ScimUser scimUser) {
        return new UserModifiedEvent(scimUser, AuditEventType.EmailChangedEvent);
    }

    @Override
    public AuditEvent getAuditEvent() {
        String data = JsonUtils.writeValueAsString(buildDetails());
        return createAuditRecord(
                scimUser.getId(),
                eventType,
                getOrigin(getAuthentication()),
                data);
    }

    private String[] buildDetails() {
        if (AuditEventType.UserCreatedEvent.equals(this.eventType)) {

            // Not authenticated, e.g. when saml login creates a shadow user
            if (!getContextAuthentication().isAuthenticated()) {
                return new String[]{
                        "user_id=" + scimUser.getId(),
                        "username=" + scimUser.getUserName(),
                        "user_origin=" + scimUser.getOrigin()
                };
            }

            // Authenticated as a user
            if (getContextAuthentication().getPrincipal() instanceof UaaPrincipal) {
                UaaPrincipal uaaPrincipal = (UaaPrincipal) getContextAuthentication().getPrincipal();

                return new String[]{
                        "user_id=" + scimUser.getId(),
                        "username=" + scimUser.getUserName(),
                        "user_origin=" + scimUser.getOrigin(),
                        "created_by_user_id=" + uaaPrincipal.getId(),
                        "created_by_username=" + uaaPrincipal.getName()
                };
            }

            // Authenticated as a client
            return new String[]{
                    "user_id=" + scimUser.getId(),
                    "username=" + scimUser.getUserName(),
                    "user_origin=" + scimUser.getOrigin(),
                    "created_by_client_id=" + getContextAuthentication().getPrincipal()
            };
        }
        return new String[]{
                "user_id=" + scimUser.getId(),
                "username=" + scimUser.getUserName()
        };
    }

    public String getUserId() {
        return scimUser.getId();
    }

    public String getUsername() {
        return scimUser.getUserName();
    }

    public String getEmail() {
        return scimUser.getPrimaryEmail();
    }

}
