package nettunit.StateMachine;

import nettunit.StateMachine.states.GestionnaireInformOrgsState;

public class CurrentProcessContext {
    private ProcessState state;

    public CurrentProcessContext() {
        state = new GestionnaireInformOrgsState();
    }

    public void previousState() {
        state.prev(this);

    }

    public void nextState() {
        state.next(this);
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }
}
