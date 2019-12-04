1) Build the project
2) Create a directory <KEYCLOAK_HOME>/modules/system/layers/base/com/yt/auth/app-keycloak-oidc-group-info-mapper/main
3) Place the generated app-keycloak-oidc-group-info-mapper.jar and src/main/module/module.xml in the above created directory
4) Create a directory <KEYCLOAK_HOME>/modules/system/layers/base/org/apache/commons/commons-lang3/main
5) Place src/main/config/org.apache.commons.commons-lang3.main/* in the above created directory
6) Add following new provider in standalone.xml file

     ````
    <providers>
            <provider>module:com.yt.auth.app-keycloak-oidc-group-info-mapper</provider>
    </providers>
    ````
7) Restart keycloak app
8) Go to clients --> mappers --> create . In the dropdown you will find new mapper type "Group information mapper"