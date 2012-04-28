/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.karaf.cellar.bundle;

import org.apache.karaf.cellar.core.control.BasicSwitch;
import org.apache.karaf.cellar.core.control.Switch;
import org.apache.karaf.cellar.core.control.SwitchStatus;
import org.apache.karaf.cellar.core.event.EventHandler;
import org.apache.karaf.cellar.core.event.EventType;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleEventHandler extends BundleSupport implements EventHandler<RemoteBundleEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleEventHandler.class);

    public static final String SWITCH_ID = "org.apache.karaf.cellar.bundle.handler";

    private final Switch eventSwitch = new BasicSwitch(SWITCH_ID);

    /**
     * Handles remote bundle events.
     *
     * @param event
     */
    public void handle(RemoteBundleEvent event) {
        if (eventSwitch.getStatus().equals(SwitchStatus.OFF)) {
            LOGGER.warn("CELLAR BUNDLE: {} switch is OFF, cluster event is not handled", SWITCH_ID);
            return;
        }
        try {
            //Check if the pid is marked as local.
            if (isAllowed(event.getSourceGroup(), Constants.CATEGORY, event.getLocation(), EventType.INBOUND)) {
                BundleState state = new BundleState();
                state.setStatus(event.getType());

                if (event.getType() == BundleEvent.INSTALLED) {
                    LOGGER.debug("CELLAR BUNDLE: installing bundle {} from {}", event.getId(), event.getLocation());
                    installBundleFromLocation(event.getLocation());
                } else if (event.getType() == BundleEvent.UNINSTALLED) {
                    LOGGER.debug("CELLAR BUNDLE: un-installing bundle {}/{}", event.getSymbolicName(), event.getVersion());
                    uninstallBundle(event.getSymbolicName(), event.getVersion());
                } else if (event.getType() == BundleEvent.STARTED) {
                    LOGGER.debug("CELLAR BUNDLE: starting bundle {}/{}", event.getSymbolicName(), event.getVersion());
                    startBundle(event.getSymbolicName(), event.getVersion());
                } else if (event.getType() == BundleEvent.STOPPED) {
                    LOGGER.debug("CELLAR BUNDLE: stopping bundle {}/{}", event.getSymbolicName(), event.getVersion());
                    stopBundle(event.getSymbolicName(), event.getVersion());
                } else if (event.getType() == BundleEvent.UPDATED) {
                    LOGGER.debug("CELLAR BUNDLE: updating bundle {}/{}", event.getSymbolicName(), event.getVersion());
                    updateBundle(event.getSymbolicName(), event.getVersion());
                }
            } else LOGGER.warn("CELLAR BUNDLE: bundle {} is marked as BLOCKED INBOUND", event.getLocation());
        } catch (BundleException e) {
            LOGGER.error("CELLAR BUNDLE: failed to handle bundle event", e);
        }
    }

    /**
     * Initialization Method.
     */
    public void init() {

    }

    /**
     * Destruction Method.
     */
    public void destroy() {

    }

    public Switch getSwitch() {
        return eventSwitch;
    }

    public Class<RemoteBundleEvent> getType() {
        return RemoteBundleEvent.class;
    }

}
