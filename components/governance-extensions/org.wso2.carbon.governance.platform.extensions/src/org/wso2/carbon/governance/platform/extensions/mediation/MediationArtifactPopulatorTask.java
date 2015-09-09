/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.platform.extensions.mediation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.platform.extensions.util.MediationUtils;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

public class MediationArtifactPopulatorTask implements Task {

    private static final Log log = LogFactory.getLog(MediationArtifactPopulatorTask.class);

    @Override
    public void setProperties(Map<String, String> properties) {
        MediationUtils.setUserName(properties.get("userName"));
        MediationUtils.setPassword(properties.get("password"));
        MediationUtils.setServerEpr(properties.get("serverUrl"));
        MediationUtils.setProxyArtifactKey(properties.get("proxyArtifactKey"));
        MediationUtils.setEndpointArtifactKey(properties.get("endpointArtifactKey"));
        MediationUtils.setSequenceArtifactKey(properties.get("sequenceArtifactKey"));
    }

    @Override
    public void init() {
       log.info("MediationArtifactPopulatorTask initialized..");
    }

    @Override
    public void execute() {
        try {
            MediationUtils.populateMediationArtifacts();
        } catch (Exception e) {
          log.error("Error while performing MediationArtifactPopulatorTask" + e.getMessage());
        }
    }


}
