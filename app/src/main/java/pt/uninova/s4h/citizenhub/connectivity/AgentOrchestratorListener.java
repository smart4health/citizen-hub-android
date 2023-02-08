package pt.uninova.s4h.citizenhub.connectivity;

import pt.uninova.s4h.citizenhub.data.Device;

public interface AgentOrchestratorListener {

    default void onAgentStateChanged(Agent agent) {
    }

    default void onAgentAttached(Device device, Agent agent) {
    }

    default void onAgentRemoved(Device device, Agent agent) {
    }

    default void onDeviceAdded(Device device) {
    }

    default void onDeviceRemoved(Device device) {
    }

}
