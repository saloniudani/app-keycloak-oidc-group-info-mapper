package com.yt.auth;

import org.keycloak.models.GroupModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class GroupInfoMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, GroupInfoMapper.class);
    }

    public static final String PROVIDER_ID = "app-keycloak-oidc-group-info-mapper";


    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Group information mapper";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Adds tenant, organizations and departments based on the user groups";
    }

    /**
     * Adds tenant, organizations and departments based on the user groups to the {@link IDToken# otherClaims}.
     *
     * @param token
     * @param mappingModel
     * @param userSession
     */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        Map<String, Object> processedGroupInfo = new HashMap<>();
        Set<String> dept = new HashSet<>();
        Set<String> org = new HashSet<>();
        String tenant = "";
        for (GroupModel group : userSession.getUser().getGroups()) {
            String groupPath = ModelToRepresentation.buildGroupPath(group);
            String[] splitGroupHierarchy = groupPath.substring(1).split("/");
            if (splitGroupHierarchy.length >= 3) {
                tenant = splitGroupHierarchy[1];
                org.add(splitGroupHierarchy[2]);
                String groupWithSlashAtEnd = groupPath + "/";
                for (int i = 3; i < splitGroupHierarchy.length; i++) {
                    dept.add(groupWithSlashAtEnd.substring(0,
                            StringUtils.ordinalIndexOf(groupWithSlashAtEnd, "/", i + 2)));
                }
            }
        }

        processedGroupInfo.put("tenant", tenant);
        processedGroupInfo.put("organizations", new ArrayList<>(org));
        processedGroupInfo.put("departments", new ArrayList<>(dept));

        String protocolClaim = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        token.getOtherClaims().put(protocolClaim, processedGroupInfo);
    }

    public static ProtocolMapperModel create(String name,
                                             String tokenClaimName,
                                             boolean accessToken, boolean idToken) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, tokenClaimName);
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        mapper.setConfig(config);

        return mapper;
    }
}
